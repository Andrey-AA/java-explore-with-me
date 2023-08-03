package ru.practicum.controller.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.*;
import ru.practicum.service.CommentService;
import ru.practicum.service.EventService;
import ru.practicum.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class PrivateController {

    private final RequestService requestService;
    private final EventService eventService;
    private final CommentService commentService;

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

    @GetMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventFullByInitiator(@PathVariable int userId, @PathVariable int eventId) {
        log.info("Ищется событие с с id = {} пользователем с id = {}", eventId, userId);
        return eventService.getEventFullByInitiator(userId, eventId);
    }

    @GetMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventShortByInitiator(@PathVariable int userId,
                                                        @PositiveOrZero @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
                                                        @Positive @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        log.info("Ищутся события пользователем с id = {}", userId);
        return eventService.getEventShortListByInitiator(userId, from, size);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventByInitiator(@PathVariable int userId,
                                               @PathVariable int eventId,
                                               @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) {

        log.info("Пользователем с id = {} обновляет событие {} с id = {} ", userId, updateEventUserRequest, eventId);
        return eventService.updateEventByInitiator(userId, eventId, updateEventUserRequest);
    }

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable int userId, @RequestBody @Valid NewEventDto newEventDto) {

        log.info("Добавляется событие {} пользователем с id = {}", newEventDto, userId);
        return eventService.createEvent(userId, newEventDto);
    }

    @PostMapping("/{userId}/comment/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable Integer userId,
                                 @PathVariable Integer eventId,
                                 @Valid @RequestBody CommentEntryDto comment) {
        log.info("Пользователь с ID: " + userId + " добавляет комментарий");
        return commentService.addComment(userId, eventId, comment);
    }

    @PutMapping("/{userId}/comment/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@PathVariable Integer userId,
                                    @PathVariable Integer commentId,
                                    @RequestBody CommentEntryDto comment) {
        log.info("Пользователь с ID: " + userId + " обновляет комментарий");
        return commentService.updateComment(userId, commentId, comment);
    }

    @DeleteMapping("/{userId}/comment/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByOwner(@PathVariable Integer userId,
                                     @PathVariable Integer commentId) {
        log.info("Пользователь с ID: " + userId + " удаляет комментарий с ID: " + commentId);
        commentService.deleteCommentByOwner(userId, commentId);
    }

    @GetMapping("/{userId}/comment")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getCommentsByUser(@PathVariable Integer userId,
                                              @RequestParam(defaultValue = "false") Boolean asc,
                                              @RequestParam(required = false, defaultValue = "0") Integer from,
                                              @RequestParam(required = false, defaultValue = "10") Integer size) {

        return commentService.getCommentsByUser(userId, asc, from, size);
    }

}
