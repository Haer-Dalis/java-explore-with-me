package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.additions.Constants;
import ru.practicum.location.dto.LocationDto;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventNewDto {
    @NotBlank(message = "аннотация не может быть только из пробелов")
    @Size(min = 20, max = 2000)
    private String annotation;

    @NotBlank(message = "описание не может быть только из пробелов")
    @Size(min = 20, max = 7000)
    private String description;

    @NotNull
    private Long category;

    @Future
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_TIME_PATTERN)
    private LocalDateTime eventDate;

    @NotNull(message = "место не может быть пустым")
    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero
    private Integer participantLimit;

    private Boolean requestModeration;

    @NotBlank(message = "название не должно быть пустым")
    @Size(min = 3, max = 120)
    private String title;
}
