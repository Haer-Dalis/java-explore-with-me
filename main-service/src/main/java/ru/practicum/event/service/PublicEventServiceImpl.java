package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import ru.practicum.HitDto;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@ComponentScan
@Service
@RequiredArgsConstructor
public class PublicEventServiceImpl implements PublicEventService {
    private final EventRepository eventRepository;
    private final StatsClient statsClient;

    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, String rangeStart,
                                         String rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                         Integer size, HttpServletRequest request) {
        Pageable pageable;
        if (sort != null) {
            String sortField = sort.equals(SortType.EVENT_DATE.name()) ? "eventDate" : "views";
            pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(sortField).descending());
        } else {
            pageable = PageRequest.of(from > 0 ? from / size : 0, size);
        }
        LocalDateTime startDate = rangeStart != null
                ? LocalDateTime.parse(URLDecoder.decode(rangeStart, StandardCharsets.UTF_8),
                Constants.DATE_TIME_FORMATTER)
                : LocalDateTime.now();
        LocalDateTime endDate = null;
        if (rangeEnd != null) {
            endDate = LocalDateTime.parse(URLDecoder.decode(rangeEnd, StandardCharsets.UTF_8),
                    Constants.DATE_TIME_FORMATTER);
        }
        List<Event> events;
        if (endDate != null) {
            if (endDate.isBefore(startDate) || endDate.equals(startDate)) {
                throw new ValidationException("Даты не могут быть равны или дата окончания не может быть раньше даты начала");
            }
            events = eventRepository.findAllPublishedEventsByFilterAndPeriod(text, categories, paid, startDate, endDate,
                    onlyAvailable, pageable);
        } else {
            events = eventRepository.findAllPublishedEventsByFilterAndRangeStart(text, categories, paid, startDate,
                    onlyAvailable, pageable);
        }

        statsClient.createHit(createHitDto(request));
        return events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventDto getEventById(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + id + " не было найдено"));

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new NotFoundException("Можно смотреть только опубликованные события");
        }

        String start = event.getCreatedOn().withNano(0).format(DateTimeFormatter
                .ofPattern(Constants.DATE_TIME_PATTERN));
        String end = event.getEventDate().withNano(0).format(DateTimeFormatter
                .ofPattern(Constants.DATE_TIME_PATTERN));
        List<StatsDto> viewStatsDtoList = statsClient.getStatsByDateAndUris(start, end,
                List.of(request.getRequestURI()), true);

        if (!viewStatsDtoList.isEmpty()) {
            event.setViews(viewStatsDtoList.get(0).getHits());
        }
        statsClient.createHit(createHitDto(request));
        return EventMapper.toEventDto(event);
    }

    @Transactional
    private HitDto createHitDto(HttpServletRequest request) {
        return HitDto.builder()
                .app("ewm-main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().withNano(0).format(DateTimeFormatter
                        .ofPattern(Constants.DATE_TIME_PATTERN)))
                .build();
    }
}

