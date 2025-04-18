package ru.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.additions.Constants;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventRequestUpdate;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.EventNewDto;
import ru.practicum.event.dto.EventUpdateDto;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.RequestUpdateResultDto;

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
                             @RequestBody @Valid EventNewDto eventNewDto) {
        return eventService.addEvent(id, eventNewDto);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventDto updateEvent(@PathVariable("userId") Long id,
                                @PathVariable("eventId") Long eventId,
                                @RequestBody @Valid EventUpdateDto eventUpdateDto) {
        return eventService.updateEvent(id, eventId, eventUpdateDto);
    }

    @GetMapping("/{userId}/events")
    public List<EventShortDto> getAllByUser(@PathVariable("userId") Long userId,
                                            @RequestParam(defaultValue = Constants.DEFAULT_FROM) Integer from,
                                            @RequestParam(defaultValue = Constants.DEFAULT_SIZE) Integer size) {
        return eventService.getAllByUser(userId, from, size);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventDto getByUserAndId(@PathVariable("userId") Long id,
                                   @PathVariable("eventId") Long eventId) {
        return eventService.getByUserAndId(id, eventId);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<RequestDto> getRequestsByUser(@PathVariable("userId") Long ownerId,
                                                                   @PathVariable("eventId") Long eventId) {
        return eventService.getRequestsByUser(ownerId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public RequestUpdateResultDto updateRequests(@PathVariable("userId") Long ownerId,
                                                 @PathVariable("eventId") Long eventId,
                                                 @RequestBody EventRequestUpdate eventRequestUpdate) {
        return eventService.updateRequests(ownerId, eventId, eventRequestUpdate);
    }
}
