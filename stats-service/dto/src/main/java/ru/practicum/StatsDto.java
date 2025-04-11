package ru.practicum;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StatsDto {
    private String app;
    private String uri;
    @PositiveOrZero(message = "клики не могут уйти в минус")
    private Long hits;
}
