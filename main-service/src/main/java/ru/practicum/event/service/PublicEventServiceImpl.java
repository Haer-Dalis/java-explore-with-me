package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import ru.practicum.StatsClient;
import ru.practicum.StatsDto;
import ru.practicum.additions.Constants;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.SortType;
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
import java.util.Optional;
import java.util.stream.Collectors;

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
        Pageable pageable = buildPageable(from, size, sort);
        LocalDateTime start = parseStartDate(rangeStart);
        LocalDateTime end = parseEndDate(rangeEnd);

        validateDateRange(start, end);

        List<Event> events = eventRepository.findPublicEvents(text, categories, paid, start, end, onlyAvailable, pageable);
        statsClient.createHit(hitService.createHitDto(request));

        return events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventDto getEventById(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + id + " не найдено"));

        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundException("Событие не опубликовано");
        }

        updateEventViews(event, request);
        statsClient.createHit(hitService.createHitDto(request));

        return EventMapper.toEventDto(event);
    }

    private Pageable buildPageable(int from, int size, String sort) {
        int page = from > 0 ? from / size : 0;
        if (sort == null) {
            return PageRequest.of(page, size);
        }

        String sortField = SortType.EVENT_DATE.name().equals(sort) ? "eventDate" : "views";
        return PageRequest.of(page, size, Sort.by(sortField).descending());
    }

    private LocalDateTime parseStartDate(String rangeStart) {
        return Optional.ofNullable(rangeStart)
                .map(s -> LocalDateTime.parse(URLDecoder.decode(s, StandardCharsets.UTF_8), Constants.DATE_TIME_FORMATTER))
                .orElse(LocalDateTime.now());
    }

    private LocalDateTime parseEndDate(String rangeEnd) {
        return Optional.ofNullable(rangeEnd)
                .map(s -> LocalDateTime.parse(URLDecoder.decode(s, StandardCharsets.UTF_8), Constants.DATE_TIME_FORMATTER))
                .orElse(null);
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (end != null && !end.isAfter(start)) {
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }
    }

    private void updateEventViews(Event event, HttpServletRequest request) {
        String start = event.getCreatedOn().withNano(0).format(Constants.DATE_TIME_FORMATTER);
        String end = event.getEventDate().withNano(0).format(Constants.DATE_TIME_FORMATTER);

        List<StatsDto> stats = statsClient.getStatsByDateAndUris(start, end, List.of(request.getRequestURI()), true);
        if (!stats.isEmpty()) {
            event.setViews(stats.getFirst().getHits());
        }
    }
}
