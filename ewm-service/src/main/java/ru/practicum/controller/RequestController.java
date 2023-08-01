package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.RequestDto;
import ru.practicum.dto.RequestStatusUpdateDto;
import ru.practicum.dto.RequestStatusUpdateResultDto;
import ru.practicum.service.RequestService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class RequestController {

    private final RequestService requestService;

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto createRequest(@PathVariable int userId, @RequestParam int eventId) {
        log.info("Создается новая заявка для пользователя с ID = {} на событие с ID = {}", userId, eventId);
        return requestService.createRequest(userId, eventId);
    }

    @GetMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<RequestDto> getRequestList(@PathVariable int userId) {
        log.info("Запрашивается список заявок для пользователя с ID = {}", userId);
        return requestService.getRequests(userId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public RequestDto patchRequest(@PathVariable int userId, @PathVariable int requestId) {
        log.info("Пользователь с ID = {} отменяет заявку с ID = {}", userId, requestId);
        return requestService.patch(userId, requestId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public RequestStatusUpdateResultDto patchRequestByUser(@PathVariable int userId, @PathVariable int eventId,
                                                           @RequestBody RequestStatusUpdateDto requestStatusUpdateDto) {
        log.info("Пользователь с ID = {} изменяет статусы заявок на событие с ID = {}, новый статус: {}", userId, eventId, requestStatusUpdateDto);
        return requestService.patchByUser(userId, eventId, requestStatusUpdateDto);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<RequestDto> getRequestByUser(@PathVariable int userId, @PathVariable int eventId) {
        log.info("Запрашивается список заявок пользователя с ID = {} на событие с ID = {}", userId, eventId);
        return requestService.getRequestsByUser(userId, eventId);
    }
}
