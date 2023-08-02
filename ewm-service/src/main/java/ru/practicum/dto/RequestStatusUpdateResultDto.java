package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RequestStatusUpdateResultDto {

    private List<RequestDto> confirmedRequests;

    private List<RequestDto> rejectedRequests;
}
