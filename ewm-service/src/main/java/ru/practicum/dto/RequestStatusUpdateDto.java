package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.model.RequestStatus;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RequestStatusUpdateDto {

    private List<Integer> requestIds;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}
