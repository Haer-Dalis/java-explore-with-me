package ru.practicum.request.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/users")
public class RequestController {
    private final RequestService requestService;

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto addRequest(@PathVariable("userId") Long id,
                                 @RequestParam(required = false) Long eventId) {
        return requestService.addRequest(id, eventId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public RequestDto cancelRequest(@PathVariable("userId") Long id,
                                    @PathVariable("requestId") Long requestId) {
        return requestService.cancelRequest(id, requestId);
    }

    @GetMapping("/{userId}/requests")
    public List<RequestDto> getRequestDtosByUserId(@PathVariable("userId") Long id) {
        return requestService.getRequestDtosByUserId(id);
    }
}

