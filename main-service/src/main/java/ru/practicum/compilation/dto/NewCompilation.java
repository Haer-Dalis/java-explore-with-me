package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewCompilation {
    private Boolean pinned;

    @NotBlank
    @Size(max = 50)
    private String title;

    private List<Long> events;
}