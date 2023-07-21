package ru.practicum.mapper;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EndpointHitMapper {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public EndpointHit toEndpointHit(EndpointHitDto endpointHitDto) {
        if (endpointHitDto == null) {
            return null;
        }

        EndpointHit.EndpointHitBuilder endpointHit = EndpointHit.builder();

        endpointHit.app(endpointHitDto.getApp());
        endpointHit.uri(endpointHitDto.getUri());
        endpointHit.ip(endpointHitDto.getIp());

        if (endpointHitDto.getTimestamp() != null) {
            endpointHit.timestamp(String.valueOf(LocalDateTime.parse(endpointHitDto.getTimestamp(), formatter)));
        }

        return endpointHit.build();
    }

    public EndpointHitDto toEndpointHitDto(EndpointHit endpointHit) {
        if (endpointHit == null) {
            return null;
        }

        EndpointHitDto.EndpointHitDtoBuilder endpointHitDto = EndpointHitDto.builder();

        endpointHitDto.app(endpointHit.getApp());
        endpointHitDto.uri(endpointHit.getUri());
        endpointHitDto.ip(endpointHit.getIp());

        if (endpointHit.getTimestamp() != null) {
            endpointHitDto.timestamp(endpointHit.getTimestamp().format(String.valueOf(formatter)));
        }

        return endpointHitDto.build();
    }

    public ViewStatsDto toViewStatsDto(ViewStats viewStats) {
        if (viewStats == null) {
            return null;
        }

        ViewStatsDto.ViewStatsDtoBuilder viewStatsDto = ViewStatsDto.builder();

        viewStatsDto.app(viewStats.getApp());
        viewStatsDto.uri(viewStats.getUri());
        viewStatsDto.hits(viewStats.getHits());

        return viewStatsDto.build();
    }
}