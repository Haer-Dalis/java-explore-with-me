package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import ru.practicum.StatsClient;
import ru.practicum.StatsDto;
import ru.practicum.additions.Constants;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.SortCriteria;
import ru.practicum.event.dto.State;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.additions.Constants.DATE_TIME_FORMATTER;

@ComponentScan
@Service
@RequiredArgsConstructor
public class PublicEventServiceImpl implements PublicEventService {
    private final EventRepository eventRepository;
    private final StatsClient statsClient;
    private final HitService hitService;

    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, String rangeStart,
                                         String rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                         Integer size, HttpServletRequest request) {
        Pageable pageable = createPageable(from, size, sort);

        LocalDateTime startDate = parseDateTime(rangeStart, LocalDateTime.now());
        LocalDateTime endDate = parseDateTime(rangeEnd, null);

        validateDates(startDate, endDate);

        List<Event> events = findEvents(text, categories, paid, startDate, endDate, onlyAvailable, pageable);

        statsClient.createHit(hitService.createHitDto(request));

        return events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    private Pageable createPageable(Integer from, Integer size, String sort) {
        if (sort != null) {
            String sortField = sort.equals(SortCriteria.EVENT_DATE.name()) ? "eventDate" : "views";
            return PageRequest.of(from / size, size, Sort.by(sortField).descending());
        }
        return PageRequest.of(from / size, size);
    }

    private LocalDateTime parseDateTime(String dateTimeStr, LocalDateTime defaultValue) {
        if (dateTimeStr == null) return defaultValue;
        return LocalDateTime.parse(URLDecoder.decode(dateTimeStr, StandardCharsets.UTF_8),
                Constants.DATE_TIME_FORMATTER);
    }

    private void validateDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (endDate != null && (endDate.isBefore(startDate) || endDate.equals(startDate))) {
            throw new ValidationException("Даты не могут быть равны или дата окончания не может быть раньше даты начала");
        }
    }

    private List<Event> findEvents(String text, List<Long> categories, Boolean paid,
                                   LocalDateTime startDate, LocalDateTime endDate,
                                   Boolean onlyAvailable, Pageable pageable) {
        if (endDate != null) {
            return eventRepository.findPublishedEvents(text, categories, paid,
                    startDate, endDate, onlyAvailable, pageable);
        }
        return eventRepository.findPublishedEvents(text, categories, paid,
                startDate, onlyAvailable, pageable);
    }

    @Override
    public EventDto getEventById(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .filter(e -> e.getState() == State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(
                        "Событие не найдено или не опубликовано (id: " + id + ")"));

        updateEventViews(event, request);

        statsClient.createHit(hitService.createHitDto(request));

        return EventMapper.toEventDto(event);
    }

    private void updateEventViews(Event event, HttpServletRequest request) {
        LocalDateTime createdOn = event.getCreatedOn().withNano(0);
        LocalDateTime eventDate = event.getEventDate().withNano(0);

        List<StatsDto> stats = statsClient.getStatsByDateAndUris(
                createdOn.format(DATE_TIME_FORMATTER),
                eventDate.format(DATE_TIME_FORMATTER),
                List.of(request.getRequestURI()),
                true
        );

        stats.stream()
                .findFirst()
                .ifPresent(stat -> event.setViews(stat.getHits()));
    }
}

