package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.RequestCountDto;
import ru.practicum.dto.RequestDto;
import ru.practicum.dto.RequestStatusUpdateDto;
import ru.practicum.dto.RequestStatusUpdateResultDto;
import ru.practicum.exception.ObjectNotFoundException;
import ru.practicum.exception.RequestConflictException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class RequestService {

    private final RequestRepository requestRepository;

    private final UserRepository userRepository;

    private final EventRepository eventRepository;

    @Autowired
    public RequestService(RequestRepository requestRepository, UserRepository userRepository,
                          EventRepository eventRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public RequestDto createRequest(int userId, int eventId) {
        log.info("Создание запроса для пользователя с ID: {} и события с ID: {}", userId, eventId);

        User user = findUserOrThrow(userId);
        Event event = findEventOrThrow(eventId);

        preventDoubleRequest(userId);
        preventEventInitiatorRequest(user, event);
        validateEventState(event);
        checkRequestLimit(event);

        Request request = createNewRequest(user, event);
        Request savedRequest = requestRepository.save(request);

        log.info("Запрос успешно создан и сохранен с ID: {}", savedRequest.getId());

        return RequestMapper.toRequestDto(savedRequest);
    }

    private User findUserOrThrow(int userId) {
        log.info("Поиск пользователя с ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID: {} не найден", userId);
                    return new ObjectNotFoundException(String.format("Пользователь с userId = %d не найден", userId));
                });
    }

    private Event findEventOrThrow(int eventId) {
        log.info("Поиск события с ID: {}", eventId);
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Событие с ID: {} не найдено", eventId);
                    return new ObjectNotFoundException(String.format("Событие с eventId = %d не найдено", eventId));
                });
    }

    private void preventDoubleRequest(int userId) {
        log.info("Проверка на повторный запрос от пользователя с ID: {}", userId);
        List<Request> doubleRequest = requestRepository.findByRequesterId(userId);

        if (!doubleRequest.isEmpty()) {
            log.warn("Попытка добавить повторный запрос от пользователя с ID: {}", userId);
            throw new RequestConflictException("нельзя добавить повторный запрос");
        }
    }

    private void preventEventInitiatorRequest(User user, Event event) {
        if (user.getId().equals(event.getInitiator().getId())) {
            log.warn("Инициатор события с ID: {} пытается добавить запрос на участие в своём событии", event.getId());
            throw new RequestConflictException("инициатор события не может добавить запрос на участие в своём событии");
        }
    }

    private void validateEventState(Event event) {
        if (!event.getState().equals(State.PUBLISHED)) {
            log.warn("Попытка участия в неопубликованном событии с ID: {}", event.getId());
            throw new RequestConflictException("нельзя участвовать в неопубликованном событии");
        }
    }

    private void checkRequestLimit(Event event) {
        log.info("Проверка лимита запросов для события с ID: {}", event.getId());
        RequestCountDto requestCountDto = getRequestCountDto(event.getId(), RequestStatus.CONFIRMED);

        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= requestCountDto.getRequestCount()) {
            log.warn("Достигнут лимит запросов на участие в событии с ID: {}", event.getId());
            throw new RequestConflictException("у события достигнут лимит запросов на участие");
        }
    }

    private Request createNewRequest(User user, Event event) {
        Request request = new Request();
        request.setRequester(user);
        request.setEvent(event);

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        log.info("Создан новый запрос для пользователя с ID: {} и события с ID: {}", user.getId(), event.getId());
        return request;
    }

    @Transactional(readOnly = true)
    public List<RequestDto> getRequests(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь с userId = " + userId + " не найден"));

        List<Request> requestList = requestRepository.findByRequesterId(userId);
        return RequestMapper.toRequestDtoList(requestList);
    }

    @Transactional
    public RequestDto patch(int userId, int requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь с userId = " + userId + " не найден"));

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ObjectNotFoundException("Запрос с requestId = " + requestId + " не найден"));

        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Transactional
    public RequestStatusUpdateResultDto patchByUser(int userId, int eventId, RequestStatusUpdateDto requestStatusUpdateDto) {
        List<RequestDto> confirmedRequests = new ArrayList<>();
        List<RequestDto> rejectedRequests = new ArrayList<>();

        User user = findUserOrThrow(userId);
        Event event = findEventOrThrow(eventId);
        checkEventParticipantLimit(event);

        RequestCountDto requestCountDto = getRequestCountDto(eventId, RequestStatus.CONFIRMED);

        if (event.getParticipantLimit() <= requestCountDto.getRequestCount()) {
            throw new RequestConflictException("Достигнут лимит заявок на участие в событии");
        }

        List<Request> requestList = requestRepository.findByIdIn(requestStatusUpdateDto.getRequestIds());
        for (Request request : requestList) {
            validateRequestStatus(request);
            updateRequestStatus(requestStatusUpdateDto, event, requestCountDto, request, confirmedRequests, rejectedRequests);
        }

        return new RequestStatusUpdateResultDto(confirmedRequests, rejectedRequests);
    }

    private void validateRequestStatus(Request request) {
        if (!request.getStatus().equals(RequestStatus.PENDING)) {
            throw new RequestConflictException("Статус заявки может быть изменен только в состоянии ожидания");
        }
    }

    private void updateRequestStatus(RequestStatusUpdateDto requestStatusUpdateDto, Event event, RequestCountDto requestCountDto, Request request,
                                     List<RequestDto> confirmedRequests, List<RequestDto> rejectedRequests) {
        if (requestStatusUpdateDto.getStatus().equals(RequestStatus.CONFIRMED) && (event.getParticipantLimit() > requestCountDto.getRequestCount())) {
            request.setStatus(RequestStatus.CONFIRMED);
            requestRepository.save(request);
            confirmedRequests.add(RequestMapper.toRequestDto(request));
        } else {
            request.setStatus(RequestStatus.REJECTED);
            requestRepository.save(request);
            rejectedRequests.add(RequestMapper.toRequestDto(request));
        }
    }

    private void checkEventParticipantLimit(Event event) {
        if (event.getParticipantLimit() == 0 || event.getRequestModeration().equals(false)) {
            throw new RuntimeException("Модерация заявок или лимит участников для события не заданы");
        }
    }


    @Transactional(readOnly = true)
    public RequestCountDto getRequestCountDto(int eventId, RequestStatus status) {
        RequestCountDto requestCountDto = requestRepository.findRequestCountDtoByEventIdAndStatus(eventId, status);
        return Objects.requireNonNullElseGet(requestCountDto, () -> new RequestCountDto(eventId, 0L));
    }

    @Transactional(readOnly = true)
    public List<RequestDto> getRequestsByUser(int userId, int eventId) {
        User user = findUserByIdOrThrow(userId);
        Event event = findEventByIdOrThrow(eventId);

        checkEventOwnership(user, event);
        List<Request> requestList = requestRepository.findByEventId(eventId);

        return RequestMapper.toRequestDtoList(requestList);
    }

    private User findUserByIdOrThrow(int userId) {
        log.info("Поиск пользователя с ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Не удалось найти пользователя с ID: {}", userId);
                    return new ObjectNotFoundException(String.format("Не удалось найти пользователя с ID = %d", userId));
                });
    }

    private Event findEventByIdOrThrow(int eventId) {
        log.info("Поиск события с ID: {}", eventId);
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Событие с ID: {} не найдено", eventId);
                    return new ObjectNotFoundException(String.format("Не удалось найти событие с ID = %d", eventId));
                });
    }

    private void checkEventOwnership(User user, Event event) {
        log.info("Проверка владения событием для пользователя с ID: {}", user.getId());
        if (!event.getInitiator().getId().equals(user.getId())) {
            log.error("Пользователь с ID: {} не является владельцем события с ID: {}", user.getId(), event.getId());
            throw new RequestConflictException("Пользователь не владеет этим событием");
        }
    }

}
