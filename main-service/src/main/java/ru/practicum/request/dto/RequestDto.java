package ru.practicum.request.dto;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestDto {
    private Long id;
    private Long event;
    private Long requester;
    private LocalDateTime created;
    private Status status;
}
