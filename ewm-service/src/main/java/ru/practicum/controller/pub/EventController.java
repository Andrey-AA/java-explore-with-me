package ru.practicum.controller.pub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EventFilterParametersDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;
    private final StatsClient statsClient;

    @Autowired
    public EventController(EventService eventService, StatsClient statsClient) {
        this.eventService = eventService;
        this.statsClient = statsClient;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEventFullWithFilter(HttpServletRequest request,
                                                     @RequestParam(value = "text", required = false) String text,
                                                     @RequestParam(value = "categories", required = false) List<Integer> categories,
                                                     @RequestParam(value = "paid", required = false) Boolean paid,
                                                     @RequestParam(value = "rangeStart", required = false) String rangeStart,
                                                     @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
                                                     @RequestParam(value = "onlyAvailable", required = false, defaultValue = "false") Boolean onlyAvailable,
                                                     @RequestParam(value = "sort", required = false) String sort,
                                                     @PositiveOrZero @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
                                                     @Positive @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        EventFilterParametersDto parameters = new EventFilterParametersDto(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        log.info("Получение событий  с параметрами поиска {}", parameters);

        statsClient.addStat(new EndpointHitDto("main-service", "/events", request.getRemoteAddr(), LocalDateTime.now()));
        return eventService.getEventFullWithFilter(parameters);
    }


    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventFullById(HttpServletRequest request, @PathVariable int eventId) {
        log.info("Ищется событие с id = {} ", eventId);
        statsClient.addStat(new EndpointHitDto("main-service", "/events/" + eventId, request.getRemoteAddr(), LocalDateTime.now()));
        return eventService.getEventFullById(eventId);
    }
}
