package ru.practicum.compilation.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class PublicCompilationServiceImpl implements PublicCompilationService {

    private final CompilationRepository compilationRepository;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Запрос на получение подборок: pinned={}, from={}, size={}", pinned, from, size);

        int page = Math.max(from, 0) / size;
        Pageable pageable = PageRequest.of(page, size);

        log.debug("Рассчитанный Pageable: page={}, size={}", page, size);

        List<Compilation> compilations;

        if (pinned == null) {
            log.debug("Поиск всех подборок без фильтра pinned");
            compilations = compilationRepository.findAll(pageable).getContent();
        } else {
            log.debug("Поиск подборок с фильтром pinned={}", pinned);
            compilations = compilationRepository.findCompilationsByPinned(pinned, pageable);
        }

        log.debug("Найдено {} подборок", compilations.size());

        List<CompilationDto> dtos = compilations.stream()
                .map(compilation -> {
                    CompilationDto dto = CompilationMapper.toCompilationDto(compilation);
                    log.trace("Преобразована подборка: {}", dto);
                    return dto;
                })
                .toList();

        log.info("Результат getCompilations: возвращено {} подборок", dtos.size());

        return dtos;
    }

    @Override
    public CompilationDto getCompilationById(Long id) {
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Собрание с id = " + id + " не было найдено!"));

        return CompilationMapper.toCompilationDto(compilation);
    }
}
