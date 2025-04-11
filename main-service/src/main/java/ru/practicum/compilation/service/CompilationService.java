package ru.practicum.compilation.service;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationRequest;
import ru.practicum.compilation.dto.NewCompilation;

public interface CompilationService {
    CompilationDto addCompilation(NewCompilation newCompilation);

    CompilationDto updateCompilation(Long id, CompilationRequest collectionRequest);

    void deleteCompilation(Long id);
}
