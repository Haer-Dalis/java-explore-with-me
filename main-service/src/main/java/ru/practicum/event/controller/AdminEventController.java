package ru.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.additions.Constants;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.State;
import ru.practicum.event.dto.EventUpdateDto;
import ru.practicum.event.service.AdminEventService;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/admin/events")
public class AdminEventController {
    private final AdminEventService adminEventService;

    @GetMapping
    public List<EventDto> getEvents(@RequestParam(required = false) List<Long> users,
                                    @RequestParam(required = false) List<State> states,
                                    @RequestParam(required = false) List<Long> categories,
                                    @RequestParam(required = false) String rangeStart,
                                    @RequestParam(required = false) String rangeEnd,
                                    @RequestParam(defaultValue = Constants.DEFAULT_FROM) Integer from,
                                    @RequestParam(defaultValue = Constants.DEFAULT_SIZE) Integer size) {
        return adminEventService.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventDto updateEventAdmin(@PathVariable("eventId") Long eventId,
                                     @RequestBody @Valid EventUpdateDto eventUpdateDto) {
        return adminEventService.updateEventAdmin(eventId, eventUpdateDto);
    }
}
