package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCompilationDto {

    private Set<Integer> events;

    private Boolean pinned;

    @Size(min = 1, max = 50)
    private String title;
}
