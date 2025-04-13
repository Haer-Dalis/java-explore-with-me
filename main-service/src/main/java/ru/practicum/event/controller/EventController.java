package ru.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.additions.Constants;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.ResultRequestStatusDto;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/users")
public class EventController {
    private final EventService eventService;

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto addEvent(@PathVariable("userId") Long id,
                             @RequestBody @Valid NewEventDto newEventDto) {
        return eventService.addEvent(id, newEventDto);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventDto updateEvent(@PathVariable("userId") Long id,
                                @PathVariable("eventId") Long eventId,
                                @RequestBody @Valid UpdateEventDto updateEventDto) {
        return eventService.updateEvent(id, eventId, updateEventDto);
    }

    @GetMapping("/{userId}/events")
    public List<EventShortDto> getEventsByUserId(@PathVariable("userId") Long userId,
                                                 @RequestParam(defaultValue = Constants.DEFAULT_FROM) Integer from,
                                                 @RequestParam(defaultValue = Constants.DEFAULT_SIZE) Integer size) {
        return eventService.getEventsByUserId(userId, from, size);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventDto getEventByUserIdAndEventId(@PathVariable("userId") Long id,
                                               @PathVariable("eventId") Long eventId) {
        return eventService.getEventByUserIdAndEventId(id, eventId);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<RequestDto> getRequestsByCurrentUserAndEventId(@PathVariable("userId") Long ownerId,
                                                                   @PathVariable("eventId") Long eventId) {
        return eventService.getRequestsByCurrentUserAndEventId(ownerId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public ResultRequestStatusDto changeRequestByCurrentUserId(@PathVariable("userId") Long ownerId,
                                                               @PathVariable("eventId") Long eventId,
                                                               @RequestBody EventRequestStatus eventRequestStatus) {
        return eventService.changeRequestByCurrentUserId(ownerId, eventId, eventRequestStatus);
    }
}
