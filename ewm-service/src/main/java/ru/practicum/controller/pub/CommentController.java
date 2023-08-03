package ru.practicum.controller.pub;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentDto;
import ru.practicum.service.CommentService;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/comment")
public class CommentController {

    private final CommentService service;



    @GetMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto getCommentById(@PathVariable Integer commentId) {
        log.info("Получение комментария с ID: " + commentId);
        return service.getCommentById(commentId);
    }

}
