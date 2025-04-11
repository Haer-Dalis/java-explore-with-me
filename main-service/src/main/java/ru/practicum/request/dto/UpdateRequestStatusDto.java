package ru.practicum.request.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateRequestStatusDto {
    private Status status;
    private List<Long> requestIds;
}

