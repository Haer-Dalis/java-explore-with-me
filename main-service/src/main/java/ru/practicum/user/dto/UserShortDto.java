package ru.practicum.user.dto;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserShortDto {
    private String email;
    private String name;
}