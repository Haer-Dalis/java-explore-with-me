package ru.practicum.compilation.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationRequest;
import ru.practicum.compilation.dto.NewCompilation;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto addCompilation(NewCompilation newCompilation) {
        List<Long> eventIds = Optional.ofNullable(newCompilation.getEvents())
                .orElse(Collections.emptyList());

        List<Event> foundEvents = eventRepository.findAllById(eventIds);
        if (foundEvents.size() != eventIds.size()) {
            throw new NotFoundException("Одно или более событий не существуют");
        }

        List<Event> sortedEvents = foundEvents.stream()
                .sorted(Comparator.comparing(Event::getEventDate))
                .toList();

        Set<Event> events = new LinkedHashSet<>(sortedEvents);

        Compilation compilation = CompilationMapper.toCompilation(newCompilation, events);
        Compilation savedCompilation = compilationRepository.save(compilation);

        return CompilationMapper.toCompilationDto(savedCompilation);
    }

    @Override
    public CompilationDto updateCompilation(Long id, CompilationRequest request) {
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Компиляция с таким id " + id + " не обнаружена"));

        compilation.setTitle(
                Optional.ofNullable(request.getTitle()).orElse(compilation.getTitle())
        );

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }

        if (request.getEvents() != null) {
            List<Event> foundEvents = eventRepository.findAllById(request.getEvents());
            if (foundEvents.size() != request.getEvents().size()) {
                throw new NotFoundException("Одно или более событий не существуют");
            }

            List<Event> sortedEvents = foundEvents.stream()
                    .sorted(Comparator.comparing(Event::getEventDate))
                    .toList();

            Set<Event> events = new LinkedHashSet<>(sortedEvents);
            compilation.setEvents(events);
        }

        Compilation savedCompilation = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(savedCompilation);
    }

    @Override
    public void deleteCompilation(Long id) {
        if (!compilationRepository.existsById(id)) {
            throw new ValidationException("Компиляция с таким id " + id + " не была обнаружена");
        }
        compilationRepository.deleteById(id);
    }
}
