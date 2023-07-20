package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.exception.InvalidDateFormatException;
import ru.practicum.exception.InvalidTimeParameterException;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;
import ru.practicum.repository.StatsRepository;

import java.security.InvalidParameterException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;
    private final EndpointHitMapper endpointHitMapper;
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Transactional
    @Override
    public EndpointHitDto save(EndpointHitDto endpointHitDto) {
        log.info("Received hit input: [{}]", endpointHitDto);
        EndpointHit endpointHit = endpointHitMapper.toEndpointHit(endpointHitDto);
        EndpointHit savedEntry = statsRepository.save(endpointHit);
        log.info("Saved hit object in repository: [{}]", savedEntry);
        return endpointHitMapper.toEndpointHitDto(savedEntry);
    }

    @Override
    public List<ViewStatsDto> getStatistics(String startDate, String endDate, List<String> uris, Boolean unique) {
        LocalDateTime start = parseTime(startDate);
        LocalDateTime end = parseTime(endDate);
        if (end.isBefore(start)) {
            log.info("Error detected, start time {}, end time {}", start, end);
            throw new InvalidTimeParameterException("End time cannot be before start time");
        }

        uris = uris == null ? new ArrayList<>() : uris; // Обработка uris == null

        return Boolean.TRUE.equals(unique) ?
                getStatisticsWithUniqueIp(start, end, uris).stream()
                        .map(endpointHitMapper::toViewStatsDto)
                        .collect(Collectors.toList()) :
                getAllStatistics(start, end, uris).stream()
                        .map(endpointHitMapper::toViewStatsDto)
                        .collect(Collectors.toList());
    }

    private List<ViewStats> getStatisticsWithUniqueIp(LocalDateTime start, LocalDateTime end, List<String> uris) {
        if (uris.isEmpty()) {
            return statsRepository.getStatisticsWithUniqueIp(start, end);
        } else {
            return statsRepository.getStatisticsWithUniqueIpAndUris(start, end, uris);
        }
    }

    private List<ViewStats> getAllStatistics(LocalDateTime start, LocalDateTime end, List<String> uris) {
        if (uris.isEmpty()) {
            return statsRepository.getAllStatistics(start, end);
        } else {
            return statsRepository.getAllStatisticsWithUris(start, end, uris);
        }
    }

    private LocalDateTime parseTime(String date) {
        try {
            return LocalDateTime.parse(date, DateTimeFormatter.ofPattern(DATE_FORMAT));
        } catch (DateTimeException exception) {
            throw new InvalidDateFormatException("Incorrect date format: " + date);
        }
    }
}
