package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Data
@AllArgsConstructor
public class AdminEventSearchParametersDto {
    private List<Integer> users;
    private List<String> states;
    private List<Integer> categories;
    private String rangeStart;
    private String rangeEnd;
    @PositiveOrZero
    private Integer from = 0;
    @Positive
    private Integer size = 10;
}
