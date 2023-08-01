package ru.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.exception.ObjectNotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CompilationService {

    private final CompilationRepository compilationRepository;

    private final EventRepository eventRepository;

    private final StatsClient statsClient;

    @Autowired
    public CompilationService(CompilationRepository compilationRepository, EventRepository eventRepository,
                              StatsClient statsClient) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
        this.statsClient = statsClient;
    }

    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        // Используем Optional для обработки потенциально нулевого значения
        Set<Integer> events = Optional.ofNullable(newCompilationDto.getEvents()).orElse(new HashSet<>());

        List<Event> eventList = eventRepository.findAllById(events);

        Compilation compilation = new Compilation();
        compilation.setEventSet(new HashSet<>(eventList));

        // Используем оператор тернарного условия для более компактного присваивания значения
        compilation.setPinned(Optional.ofNullable(newCompilationDto.getPinned()).orElse(false));

        compilation.setTitle(newCompilationDto.getTitle());

        Compilation saveCompilation = compilationRepository.save(compilation);

        if (events.isEmpty()) {
            return CompilationMapper.toCompilationDto(saveCompilation, new ArrayList<>());
        }

        // Извлекаем статистику только в случае, если список событий не пуст
        List<ViewStatsDto> stat = statsClient.getStat(
                LocalDateTime.now().minusYears(20),
                LocalDateTime.now().plusYears(100),
                EventMapper.toUriCollection(saveCompilation.getEventSet()),
                Boolean.TRUE
        );

        return CompilationMapper.toCompilationDto(compilation, stat);
    }

    @Transactional
    public CompilationDto updateCompilation(int compId, UpdateCompilationDto updateCompilationDto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectNotFoundException("Подборка с compId = " + compId + " не найден"));

        // Убираем повторяющийся код с использованием Optional
        Optional.ofNullable(updateCompilationDto.getEvents()).ifPresent(events -> {
            List<Event> eventList = eventRepository.findAllById(events);
            compilation.setEventSet(new HashSet<>(eventList));
        });

        Optional.ofNullable(updateCompilationDto.getPinned()).ifPresent(compilation::setPinned);

        Optional.ofNullable(updateCompilationDto.getTitle()).ifPresent(compilation::setTitle);

        Compilation saveCompilation = compilationRepository.save(compilation);

        List<ViewStatsDto> stat = statsClient.getStat(LocalDateTime.now().minusYears(20), LocalDateTime.now().plusYears(100),
                EventMapper.toUriCollection(saveCompilation.getEventSet()), Boolean.TRUE);

        return CompilationMapper.toCompilationDto(saveCompilation, stat);
    }

    @Transactional
    public void delete(int compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectNotFoundException("Подборка с compId = " + compId + " не найден"));

        compilationRepository.delete(compilation);
    }

    @Transactional(readOnly = true)
    public List<CompilationDto> findAll(Boolean pinned, Integer from, Integer size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        List<Compilation> compilationList = (pinned != null) ?
                compilationRepository.findAllByPinned(pinned, pageable) :
                compilationRepository.findAll(pageable).getContent();

        Set<Event> eventSet = compilationList.stream()
                .flatMap(c -> c.getEventSet().stream())
                .collect(Collectors.toSet());

        List<ViewStatsDto> stat = statsClient.getStat(
                LocalDateTime.now().minusYears(20),
                LocalDateTime.now().plusYears(100),
                EventMapper.toUriCollection(eventSet),
                Boolean.FALSE
        );

        return CompilationMapper.toCompilationDtoList(compilationList, stat);
    }

    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(int compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectNotFoundException("Подборка с compId = " + compId + " не найден"));

        List<ViewStatsDto> stat = statsClient.getStat(
                LocalDateTime.now().minusYears(20),
                LocalDateTime.now().plusYears(100),
                EventMapper.toUriCollection(compilation.getEventSet()),
                Boolean.TRUE
        );

        return CompilationMapper.toCompilationDto(compilation, stat);
    }


}
