package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {

    @NotNull
    @NotBlank
    @Size(min = 20, max = 2000)
    String annotation;

    @NotNull
    Integer category;

    @NotNull
    @NotBlank
    @Size(min = 20, max = 7000)
    String description;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    @NotNull
    LocationDto location;

    Boolean paid;

    Integer participantLimit;

    Boolean requestModeration; // Если true, то все заявки будут ожидать подтверждения инициатором события. Если false - то будут подтверждаться автоматически.

    @NotNull
    @NotBlank
    @Size(min = 3, max = 120)
    String title;
}
