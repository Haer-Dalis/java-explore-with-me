package ru.practicum.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompilationRequest {
    private Boolean pinned;

    @Size(max = 50)
    private String title;

    private List<Long> events;
}

