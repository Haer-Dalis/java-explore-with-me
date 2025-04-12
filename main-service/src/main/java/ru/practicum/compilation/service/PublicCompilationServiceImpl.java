package ru.practicum.compilation.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@AllArgsConstructor
public class PublicCompilationServiceImpl implements PublicCompilationService {

    private final CompilationRepository compilationRepository;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(Math.max(from, 0) / size, size);

        List<Compilation> compilations = (pinned == null)
                ? compilationRepository.findAll(pageable).getContent()
                : compilationRepository.findCompilationsByPinned(pinned, pageable);

        return compilations.stream()
                .map(CompilationMapper::toCompilationDto)
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long id) {
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Собрание с id = " + id + " не было найдено!"));

        return CompilationMapper.toCompilationDto(compilation);
    }
}
