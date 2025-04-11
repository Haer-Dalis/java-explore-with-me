package ru.practicum.compilation.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationRequest;
import ru.practicum.compilation.dto.NewCompilation;
import ru.practicum.compilation.service.CompilationService;

@AllArgsConstructor
@RestController
@RequestMapping("/admin/compilations")
public class CompilationController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@RequestBody @Valid NewCompilation newCompilation) {
        return compilationService.addCompilation(newCompilation);
    }

    @PatchMapping("/{compId}")
    @ResponseStatus(value = HttpStatus.OK)
    public CompilationDto updateCompilation(@PathVariable("compId") Long id,
                                            @RequestBody @Valid CompilationRequest compilationRequest) {
        return compilationService.updateCompilation(id, compilationRequest);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable("compId") Long id) {
        compilationService.deleteCompilation(id);
    }
}
