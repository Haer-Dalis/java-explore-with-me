package ru.practicum;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class HitDto {
    @NotBlank(message = "Должно быть указано")
    private String app;
    @NotBlank(message = "URI не может быть пустым")
    private String uri;
    @NotBlank(message = "IP-адрес должен быть")
    private String ip;
    @NotBlank(message = "Время должно быть задано")
    private String timestamp;
}