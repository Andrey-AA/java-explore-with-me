package ru.practicum.mapper;

import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentEntryDto;
import ru.practicum.model.Comment;

public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .createdDate(comment.getCreatedDate())
                .updatedDate(comment.getUpdatedDate())
                .autherName(comment.getAuthor().getName())
                .text(comment.getText())
                .build();
    }

    public static Comment fromEntryComment(CommentEntryDto comment) {
        return Comment.builder()
                .text(comment.getText())
                .build();
    }

}
