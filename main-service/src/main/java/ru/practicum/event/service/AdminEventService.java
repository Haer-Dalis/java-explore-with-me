package ru.practicum.event.service;

import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.State;
import ru.practicum.event.dto.EventUpdateDto;

import java.util.List;

public interface AdminEventService {

    List<EventDto> getEvents(List<Long> users, List<State> states, List<Long> categories,
                             String rangeStart, String rangeEnd, Integer from, Integer size);

    EventDto updateEventAdmin(Long eventId, EventUpdateDto eventUpdateDto);
}

