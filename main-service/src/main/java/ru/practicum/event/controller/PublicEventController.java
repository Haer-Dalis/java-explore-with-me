package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.additions.Constants;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.PublicEventService;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/events")
public class PublicEventController {
    private final PublicEventService publicEventService;

    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                         @RequestParam(required = false) List<Long> categories,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false) String rangeStart,
                                         @RequestParam(required = false) String rangeEnd,
                                         @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                         @RequestParam(required = false) String sort,
                                         @RequestParam(defaultValue = Constants.DEFAULT_FROM) Integer from,
                                         @RequestParam(defaultValue = Constants.DEFAULT_SIZE) Integer size,
                                         HttpServletRequest request) {
        return publicEventService.getEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from,
                size, request);
    }

    @GetMapping("/{eventId}")
    public EventDto getEventById(@PathVariable("eventId") Long id,
                                             HttpServletRequest request) {
        return publicEventService.getEventById(id, request);
    }
}
