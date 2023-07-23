package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class EndpointHitMapper {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public EndpointHit toEndpointHit(EndpointHitDto endpointHitDto) {
        if (endpointHitDto == null) {
            return null;
        }

        EndpointHit endpointHit = new EndpointHit();

        endpointHit.setApp(endpointHitDto.getApp());
        endpointHit.setUri(endpointHitDto.getUri());
        endpointHit.setIp(endpointHitDto.getIp());

        if (endpointHitDto.getCreated() != null) {
            endpointHit.setCreated(endpointHitDto.getCreated());
        }else{
            endpointHit.setCreated(LocalDateTime.now());
        }

        return endpointHit;
    }

    public EndpointHitDto toEndpointHitDto(EndpointHit endpointHit) {
        if (endpointHit == null) {
            return null;
        }

        EndpointHitDto endpointHitDto = new EndpointHitDto();

        endpointHitDto.setApp(endpointHit.getApp());
        endpointHitDto.setUri(endpointHit.getUri());
        endpointHitDto.setIp(endpointHit.getIp());

        if (endpointHit.getCreated() != null) {
            endpointHitDto.setCreated(endpointHit.getCreated());
        }

        return endpointHitDto;
    }

    public ViewStatsDto toViewStatsDto(ViewStats viewStats) {
        if (viewStats == null) {
            return null;
        }

        ViewStatsDto viewStatsDto = new ViewStatsDto();

        viewStatsDto.setApp(viewStats.getApp());
        viewStatsDto.setUri(viewStats.getUri());
        viewStatsDto.setHits(viewStats.getHits());

        return viewStatsDto;
    }
}