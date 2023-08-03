package ru.practicum.dto;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {

    private Integer id;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String autherName;
    private String text;
}
