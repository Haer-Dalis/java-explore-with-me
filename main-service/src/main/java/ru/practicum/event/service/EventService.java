package ru.practicum.event.service;


import ru.practicum.event.dto.*;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.ResultRequestStatusDto;

import java.util.List;

public interface EventService {

    EventDto addEvent(Long id, NewEventDto newEventDto);

    EventDto updateEvent(Long id, Long eventId, UpdateEventDto updateEventDto);

    EventDto getEventByUserIdAndEventId(Long userId, Long eventId);

    List<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size);

    List<RequestDto> getRequestsByCurrentUserAndEventId(Long ownerId, Long eventId);

    ResultRequestStatusDto changeRequestByCurrentUserId(Long ownerId, Long eventId,
                                                        EventRequestStatus eventRequestStatus);
}