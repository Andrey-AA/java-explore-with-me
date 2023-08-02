package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.*;
import ru.practicum.exception.ObjectNotFoundException;
import ru.practicum.exception.RequestConflictException;
import ru.practicum.exception.RequestNotValidException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.*;
import ru.practicum.repository.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;
    private final DateTimeFormatter formatter;

    private static final int MIN_HOURS_BEFORE_EVENT = 2;

    @Autowired
    public EventService(EventRepository eventRepository, UserRepository userRepository,
                        CategoryRepository categoryRepository, LocationRepository locationRepository,
                        RequestRepository requestRepository, StatsClient statsClient) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
        this.requestRepository = requestRepository;
        this.statsClient = statsClient;
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    @Transactional
    public EventFullDto createEvent(int userId, NewEventDto newEventDto) {

        LocalDateTime minEventDateTime = LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_EVENT);
        if (newEventDto.getEventDate().isBefore(minEventDateTime)) {
            String errorMessage = String.format(
                    "Обратите внимание: дата и время на которые намечено событие не может быть раньше, " +
                            "чем через %d часа(ов) от текущего момента: %s", MIN_HOURS_BEFORE_EVENT, newEventDto.getEventDate());
            log.info(errorMessage);
            throw new RequestNotValidException(errorMessage);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь с userId = " + userId + " не найден"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new ObjectNotFoundException("Категория с Id = " + newEventDto.getCategory() + " не найдена"));

        Event event = new Event()
                .setInitiator(user)
                .setAnnotation(newEventDto.getAnnotation())
                .setCreatedOn(LocalDateTime.now())
                .setCategory(category)
                .setDescription(newEventDto.getDescription())
                .setEventDate(newEventDto.getEventDate())
                .setLocation(saveNewLocation(newEventDto.getLocation()))
                .setPaid(newEventDto.getPaid() != null && newEventDto.getPaid())
                .setParticipantLimit(newEventDto.getParticipantLimit() == null ? 0 : newEventDto.getParticipantLimit())
                .setPublishedOn(null)
                .setRequestModeration(newEventDto.getRequestModeration() == null || newEventDto.getRequestModeration())
                .setState(State.PENDING)
                .setTitle(newEventDto.getTitle());

        eventRepository.save(event);

        return EventMapper.toEventFullDto(event, user, 0, 0);
    }

    @Transactional
    public Location saveNewLocation(LocationDto locationDto) {
        Location location = new Location();
        location.setLat(locationDto.getLat());
        location.setLon(locationDto.getLon());
        return locationRepository.save(location);
    }

    @Transactional
    public EventFullDto updateEventByInitiator(int userId, int eventId, UpdateEventUserRequest updateEventUserRequest) {
        LocalDateTime updatedEventDate = updateEventUserRequest.getEventDate();

        if (updatedEventDate != null && updatedEventDate.isBefore(LocalDateTime.now())) {
            String errorMessage = String.format(
                    "Обратите внимание: дата и время на которые намечено событие не может быть раньше текущего момента: %s",
                    updatedEventDate);
            log.info(errorMessage);
            throw new RequestNotValidException(errorMessage);
        }

        User user = getUserById(userId);
        Event event = getEventById(eventId);

        validateEventDate(event);
        validateEventState(event);

        updateEventProperties(updateEventUserRequest, event);

        Event updatedEvent = eventRepository.save(event);
        return prepareEventFullDto(userId, eventId, updatedEvent);
    }

    private User getUserById(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь с userId = " + userId + " не найден"));
    }

    private Event getEventById(int eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Событие с eventId = " + eventId + " не найдено"));
    }

    private void validateEventDate(Event event) {
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            log.info("Обратите внимание: дата и время на которые намечено событие не может быть раньше, " +
                    "чем через два часа от текущего момента: {}", event.getEventDate());
            throw new RequestNotValidException("Обратите внимание: дата и время на которые намечено событие не может быть раньше, " +
                    "чем через два часа от текущего момента");
        }
    }

    private void validateEventState(Event event) {
        if (event.getState() != State.REJECTED && event.getState() != State.PENDING) {
            throw new RequestConflictException("изменить можно только отмененные события или события в состоянии ожидания модерации");
        }
    }

    private void updateEventProperties(UpdateEventUserRequest updateEventUserRequest, Event event) {

        Optional.ofNullable(updateEventUserRequest.getAnnotation())
                .ifPresent(event::setAnnotation);

        Optional.ofNullable(updateEventUserRequest.getCategory()).ifPresent(categoryId -> {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ObjectNotFoundException("Категория с Id = " + categoryId + " не найдена"));
            event.setCategory(category);
        });

        Optional.ofNullable(updateEventUserRequest.getDescription())
                .ifPresent(event::setDescription);

        Optional.ofNullable(updateEventUserRequest.getEventDate())
                .ifPresent(event::setEventDate);

        Optional.ofNullable(updateEventUserRequest.getLocation()).ifPresent(locationRequest -> {
            Location location = saveNewLocation(locationRequest);
            event.setLocation(location);
        });

        Optional.ofNullable(updateEventUserRequest.getPaid())
                .ifPresent(event::setPaid);

        Optional.ofNullable(updateEventUserRequest.getParticipantLimit())
                .ifPresent(event::setParticipantLimit);

        Optional.ofNullable(updateEventUserRequest.getRequestModeration())
                .ifPresent(event::setRequestModeration);

        Optional.ofNullable(updateEventUserRequest.getStateAction()).ifPresent(stateAction -> {
            if (stateAction == StateAction.SEND_TO_REVIEW) {
                event.setState(State.PENDING);
            }
            if (stateAction == StateAction.CANCEL_REVIEW) {
                event.setState(State.CANCELED);
            }
        });

        Optional.ofNullable(updateEventUserRequest.getTitle())
                .ifPresent(event::setTitle);
    }


    private EventFullDto prepareEventFullDto(int userId, int eventId, Event updatedEvent) {
        RequestCountDto requestCountDto = getRequestCountDto(eventId, RequestStatus.CONFIRMED);
        List<ViewStatsDto> stat = statsClient.getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
                Collections.singleton("/events/" + eventId), Boolean.TRUE);

        long statsHits = (stat.isEmpty()) ? 0 : stat.get(0).getHits();
        return EventMapper.toEventFullDto(updatedEvent, getUserById(userId), requestCountDto.getRequestCount(), statsHits);
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getEventShortListByInitiator(int userId, Integer from, Integer size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь с userId = " + userId + " не найден"));
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        List<EventShortDto> eventShortDtoList = new ArrayList<>();
        List<Event> eventList = eventRepository.findByInitiatorId(userId, pageable);

        List<ViewStatsDto> stat = getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
                EventMapper.toUriCollection(eventList), Boolean.TRUE);

        eventShortDtoList = EventMapper.toEventShortDtoList(eventList, stat);

        return eventShortDtoList;
    }


    @Transactional(readOnly = true)
    public EventFullDto getEventFullByInitiator(int userId, int eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь с userId = " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Событие с eventId = " + eventId +
                        " не найдено"));

        RequestCountDto requestCountDto = getRequestCountDto(eventId, RequestStatus.CONFIRMED);

        List<ViewStatsDto> stat = getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
                Collections.singleton("/events/" + eventId), Boolean.TRUE);

        return stat.isEmpty() ? EventMapper.toEventFullDto(event, user, requestCountDto.getRequestCount(), 0) : EventMapper.toEventFullDto(event, user, requestCountDto.getRequestCount(), stat.get(0).getHits());

    }

    @Transactional(readOnly = true)
    public List<EventFullDto> getEventFullByAdmin(AdminEventSearchParametersDto params) {
        List<EventFullDto> eventFullDtoList = new ArrayList<>();
        LocalDateTime start = LocalDateTime.now().minusYears(20);
        LocalDateTime end = LocalDateTime.now().plusYears(100);
        if (params.getRangeStart() != null && params.getRangeEnd() != null) {
            start = LocalDateTime.parse(params.getRangeStart(), formatter);
            end = LocalDateTime.parse(params.getRangeEnd(), formatter);
        }
        int page = params.getFrom() / params.getSize();
        Pageable pageable = PageRequest.of(page, params.getSize());
        List<State> stateList = new ArrayList<>();
        if (params.getStates() != null) {
            for (String s : params.getStates()) {
                stateList.add(Enum.valueOf(State.class, s));
            }
        } else {
            stateList = null;
        }
        List<Event> eventList = eventRepository.findByUserStateCategoryStartEndOrderByIdDesc(params.getUsers(), stateList, params.getCategories(), start, end, pageable);

        List<Integer> eventIdList = new ArrayList<>();
        for (Event event : eventList) {
            eventIdList.add(event.getId());
        }

        List<RequestCountDto> requestCountDtoList =
                requestRepository.findRequestCountDtoListByEventId(eventIdList, RequestStatus.CONFIRMED);

        List<ViewStatsDto> stat = getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
                EventMapper.toUriCollection(eventList), Boolean.TRUE);

        eventFullDtoList = EventMapper.toEventFullDtoList(eventList, requestCountDtoList, stat);
        return eventFullDtoList;
    }

    @Transactional
    public EventFullDto updateEventByAdmin(int eventId, UpdateEventAdminDto updateEventAdminDto) {
        if (updateEventAdminDto.getEventDate() != null && updateEventAdminDto.getEventDate().isBefore(LocalDateTime.now())) {
            log.info("Обратите внимание: дата и время на которые намечено событие не может быть раньше текущего момента: {}", updateEventAdminDto.getEventDate());

            throw new RequestNotValidException("Обратите внимание: дата и время на которые намечено событие не может быть раньше текущего момента");
        }

        Event event = fetchEventById(eventId);
        validateEventPublishTime(event);
        updateEventBasedOnAdminAction(updateEventAdminDto, event);
        setEventFieldsFromDto(event, updateEventAdminDto);
        Event updatedEvent = eventRepository.save(event);
        return createEventFullDto(eventId, updatedEvent);
    }

    private Event fetchEventById(int eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Событие с eventId = " + eventId + " не найдено"));
    }

    private void validateEventPublishTime(Event event) {
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            log.info("дата начала изменяемого события должна быть не ранее чем за час от даты публикации: {}", event.getEventDate());
            throw new RequestConflictException("дата начала изменяемого события должна быть не ранее чем за час от даты публикации.");
        }

        if (!event.getState().equals(State.PENDING)) {
            throw new RequestConflictException("событие можно публиковать, только если оно в состоянии ожидания публикации");
        }
    }

    private void updateEventBasedOnAdminAction(UpdateEventAdminDto updateEventAdminDto, Event event) {
        if (updateEventAdminDto.getStateAction() == null) {
            return;
        }

        switch (updateEventAdminDto.getStateAction()) {
            case PUBLISH_EVENT:
                event.setState(State.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
                break;
            case REJECT_EVENT:
                event.setState(State.REJECTED);
                break;
            default:
                throw new IllegalArgumentException("Invalid State Action");
        }
    }

    private void setEventFieldsFromDto(Event event, UpdateEventAdminDto updateEventAdminDto) {
        Optional.ofNullable(updateEventAdminDto.getAnnotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(updateEventAdminDto.getCategory()).ifPresent(categoryId -> setEventCategory(event, categoryId));
        Optional.ofNullable(updateEventAdminDto.getDescription()).ifPresent(event::setDescription);
        Optional.ofNullable(updateEventAdminDto.getEventDate()).ifPresent(event::setEventDate);
        Optional.ofNullable(updateEventAdminDto.getLocation()).map(this::saveNewLocation).ifPresent(event::setLocation);
        Optional.ofNullable(updateEventAdminDto.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(updateEventAdminDto.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(updateEventAdminDto.getRequestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(updateEventAdminDto.getTitle()).ifPresent(event::setTitle);
    }

    private void setEventCategory(Event event, int categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ObjectNotFoundException("Категория с Id = " + categoryId + " не найдена"));
        event.setCategory(category);
    }

    private EventFullDto createEventFullDto(int eventId, Event updatedEvent) {
        RequestCountDto requestCountDto = getRequestCountDto(eventId, RequestStatus.CONFIRMED);

        List<ViewStatsDto> stat = getStat(
                LocalDateTime.now().minusYears(20),
                LocalDateTime.now().plusYears(100),
                Collections.singleton("/events/" + eventId),
                Boolean.TRUE
        );

        long hits = stat.isEmpty() ? 0 : stat.get(0).getHits();

        return EventMapper.toEventFullDto(updatedEvent, updatedEvent.getInitiator(), requestCountDto.getRequestCount(), hits);
    }


    @Transactional(readOnly = true)
    public List<EventFullDto> getEventFullWithFilter(EventFilterParametersDto params) {
        validateEventDate(params);
        List<Event> eventList = fetchEventsBasedOnParams(params);
        List<RequestCountDto> requestCountDtoList = fetchAllRequestCountDtoByEventIdInAndStatus(eventList);

        if (params.getOnlyAvailable()) {
            removeFullEvents(eventList, requestCountDtoList);
        }

        List<ViewStatsDto> stats = getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
                EventMapper.toUriCollection(eventList), Boolean.TRUE);

        return sortEvents(params, EventMapper.toEventFullDtoList(eventList, requestCountDtoList, stats));
    }

    private void validateEventDate(EventFilterParametersDto params) {
        if (params.getRangeStart() != null && params.getRangeEnd() != null) {
            LocalDateTime start = LocalDateTime.parse(params.getRangeStart(), formatter);
            LocalDateTime end = LocalDateTime.parse(params.getRangeEnd(), formatter);
            if (end.isBefore(start) || start.isEqual(end)) {
                throw new RequestNotValidException("Обратите внимание: дата и время не корректны");
            }
        }
    }

    private List<Event> fetchEventsBasedOnParams(EventFilterParametersDto params) {
        int page = params.getFrom() / params.getSize();
        Pageable pageable = PageRequest.of(page, params.getSize());
        if (params.getRangeStart() == null || params.getRangeEnd() == null) {
            return eventRepository.findByTextCategoriesPaidEventDateAfter(params.getText(), params.getCategories(), params.getPaid(),
                    LocalDateTime.now(), pageable);
        } else {
            return eventRepository.findByTextCategoriesPaidStartEndSortByEventDate(params.getText(), params.getCategories(), params.getPaid(), LocalDateTime.parse(params.getRangeStart(), formatter),
                    LocalDateTime.parse(params.getRangeEnd(), formatter), pageable);
        }
    }

    private List<RequestCountDto> fetchAllRequestCountDtoByEventIdInAndStatus(List<Event> eventList) {
        List<Integer> eventIdList = eventList.stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        return requestRepository.findAllRequestCountDtoByEventIdInAndStatus(eventIdList, RequestStatus.CONFIRMED);
    }

    private void removeFullEvents(List<Event> eventList, List<RequestCountDto> requestCountDtoList) {
        Iterator<Event> iterator = eventList.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            requestCountDtoList.stream()
                    .filter(rc -> rc.getEventId().equals(event.getId()))
                    .findAny()
                    .ifPresent(rc -> {
                        if (Objects.equals(event.getParticipantLimit().longValue(), rc.getRequestCount())) {
                            iterator.remove();
                        }
                    });
        }
    }

    private List<EventFullDto> sortEvents(EventFilterParametersDto params, List<EventFullDto> eventFullDtoList) {
        if (params.getSort() == null) {
            return eventFullDtoList;
        }
        switch (params.getSort()) {
            case "EVENT_DATE":
                return eventFullDtoList.stream()
                        .sorted(Comparator.comparing(EventFullDto::getEventDate))
                        .collect(Collectors.toList());
            case "VIEWS":
                return eventFullDtoList.stream()
                        .sorted(Comparator.comparingLong(EventFullDto::getViews))
                        .collect(Collectors.toList());
            default:
                return eventFullDtoList;
        }
    }


    @Transactional(readOnly = true)
    public RequestCountDto getRequestCountDto(int eventId, RequestStatus status) {
        RequestCountDto requestCountDto = requestRepository.findRequestCountDtoByEventIdAndStatus(eventId, status);
        if (requestCountDto == null) {
            return new RequestCountDto(eventId, 0L);
        } else {
            return requestCountDto;
        }
    }

    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStat(LocalDateTime start, LocalDateTime end, Collection<String> uris, Boolean unique) {
        return statsClient.getStat(start, end, uris, unique);
    }

    @Transactional(readOnly = true)
    public EventFullDto getEventFullById(int eventId) {

        Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new ObjectNotFoundException("Событие с eventId = " + eventId + "и статусом" + State.PUBLISHED + " не найдено"));

        RequestCountDto requestCountDto = getRequestCountDto(eventId, RequestStatus.CONFIRMED);

        List<ViewStatsDto> stat = getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
                Collections.singleton("/events/" + eventId), Boolean.TRUE);

        return stat.isEmpty() ? EventMapper.toEventFullDto(event, event.getInitiator(),
                requestCountDto.getRequestCount(), 0)
                : EventMapper.toEventFullDto(event, event.getInitiator(),
                requestCountDto.getRequestCount(), stat.get(0).getHits());

    }
}
