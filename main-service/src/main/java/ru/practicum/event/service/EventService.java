package ru.practicum.event.service;

import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventRequestUpdate;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.EventNewDto;
import ru.practicum.event.dto.EventUpdateDto;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.RequestUpdateResultDto;

import java.util.List;

public interface EventService {

    EventDto addEvent(Long id, EventNewDto eventNewDto);

    EventDto updateEvent(Long id, Long eventId, EventUpdateDto eventUpdateDto);

    EventDto getByUserAndId(Long userId, Long eventId);

    List<EventShortDto> getAllByUser(Long userId, Integer from, Integer size);

    List<RequestDto> getRequestsByUser(Long ownerId, Long eventId);

    RequestUpdateResultDto updateRequests(Long ownerId, Long eventId,
                                          EventRequestUpdate eventRequestUpdate);
}