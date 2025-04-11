package ru.practicum.user.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserShortDto {
    private String email;
    private String name;
}