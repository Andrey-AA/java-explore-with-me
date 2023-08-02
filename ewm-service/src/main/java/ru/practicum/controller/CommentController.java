package ru.practicum.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentEntryDto;
import ru.practicum.service.CommentService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/comment")
public class CommentController {

    private final CommentService service;

    @GetMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getCommentsByUser(@PathVariable Integer userId,
                                              @RequestParam(defaultValue = "false") Boolean asc,
                                              @RequestParam(required = false, defaultValue = "0") Integer from,
                                              @RequestParam(required = false, defaultValue = "10") Integer size) {

        return service.getCommentsByUser(userId, asc, from, size);
    }

    @PostMapping("/users/{userId}/comment/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable Integer userId,
                                 @PathVariable Integer eventId,
                                 @Valid @RequestBody CommentEntryDto comment) {
        log.info("Пользователь с ID: " + userId + " добавляет комментарий");
        return service.addComment(userId, eventId, comment);
    }

    @PutMapping("/users/{userId}/comment/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@PathVariable Integer userId,
                                    @PathVariable Integer commentId,
                                    @RequestBody CommentEntryDto comment) {
        log.info("Пользователь с ID: " + userId + " обновляет комментарий");
        return service.updateComment(userId, commentId, comment);
    }

    @DeleteMapping("/users/{userId}/comment/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByOwner(@PathVariable Integer userId,
                                     @PathVariable Integer commentId) {
        log.info("Пользователь с ID: " + userId + " удаляет комментарий с ID: " + commentId);
        service.deleteCommentByOwner(userId, commentId);
    }

    @GetMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto getCommentById(@PathVariable Integer commentId) {
        log.info("Получение комментария с ID: " + commentId);
        return service.getCommentById(commentId);
    }

}
