package ru.practicum.compilation.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.additions.Constants;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.PublicCompilationService;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/compilations")
public class PublicCompilationController {
    private final PublicCompilationService publicCompilationService;

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(defaultValue = Constants.DEFAULT_FROM) Integer from,
                                                @RequestParam(defaultValue = Constants.DEFAULT_SIZE) Integer size) {
        return publicCompilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable("compId") Long id) {
        return publicCompilationService.getCompilationById(id);
    }
}
