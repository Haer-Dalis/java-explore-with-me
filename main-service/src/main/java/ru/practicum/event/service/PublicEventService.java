package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

public interface PublicEventService {

    List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, String rangeStart, String rangeEnd,
                                  Boolean onlyAvailable, String sort, Integer from, Integer size,
                                  HttpServletRequest request);

    EventDto getEventById(Long id, HttpServletRequest request);
}

