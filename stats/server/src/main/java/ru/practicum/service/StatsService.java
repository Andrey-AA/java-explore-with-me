package ru.practicum.service;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    EndpointHitDto save(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> getStatistics(String start, String end, List<String> uris, Boolean unique);

}
