package ru.practicum.event.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.additions.Constants;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.EventDto;

import ru.practicum.event.dto.State;
import ru.practicum.event.dto.UpdateEventDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.location.model.Location;
import ru.practicum.location.repository.LocationRepository;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class AdminEventServiceImpl implements AdminEventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

    @Override
    public List<EventDto> getEvents(List<Long> users, List<State> states, List<Long> categories,
                                    String rangeStart, String rangeEnd, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);
        LocalDateTime start = parseDate(rangeStart);
        LocalDateTime end = parseDate(rangeEnd);

        if (start != null && end != null) {
            validateDateRange(start, end);
        }

        List<Event> events = eventRepository.findAllEvents(users, states, categories, start, end, pageable);
        return events.stream()
                .map(EventMapper::toEventDto)
                .toList();
    }

    @Override
    public EventDto updateEventAdmin(Long eventId, UpdateEventDto updateEventDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не обнаружено"));

        if (!event.getState().equals(State.PENDING)) {
            throw new ConflictException("Событие должно быть в ином состоянии");
        }

        if (updateEventDto.getEventDate() != null) {
            validateEventDate(updateEventDto.getEventDate());
        }

        if (updateEventDto.getLocation() != null) {
            Location location = updateEventDto.getLocation();
            if (location.getId() == null) {
                location = locationRepository.save(location);
            }
            event.setLocation(location);
        }

        Category category = resolveCategory(updateEventDto.getCategory(), event.getCategory());
        event = eventRepository.save(EventMapper.toUpdatedEvent(updateEventDto, category, event));

        return EventMapper.toEventDto(event);
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null) return null;
        return LocalDateTime.parse(URLDecoder.decode(dateStr, StandardCharsets.UTF_8), Constants.DATE_TIME_FORMATTER);
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new ConflictException("Даты начала и окончания неверны");
        }
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ConflictException("Дата начала события должна быть не ранее чем за час от текущего времени");
        }
    }

    private Category resolveCategory(Long categoryId, Category currentCategory) {
        if (categoryId == null) return currentCategory;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id " + categoryId + " не найдена"));
    }
}
