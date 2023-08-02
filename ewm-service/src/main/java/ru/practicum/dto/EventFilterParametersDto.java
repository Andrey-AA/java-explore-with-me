package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Data
@AllArgsConstructor
public class EventFilterParametersDto {
    private String text;
    private List<Integer> categories;
    private Boolean paid;
    private String rangeStart;
    private String rangeEnd;
    private Boolean onlyAvailable = false;
    private String sort;
    @PositiveOrZero
    private Integer from = 0;
    @Positive
    private Integer size = 10;
}
