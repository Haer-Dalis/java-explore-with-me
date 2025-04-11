package ru.practicum.location.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationDto {
    private Double lat;
    private Double lon;
}
