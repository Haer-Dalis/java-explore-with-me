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
        System.out.println("=== Вход в метод addRequest ===");
        System.out.println("userId: " + id);
        System.out.println("eventId: " + eventId);

        if (id == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }

        RequestDto dto = requestService.addRequest(id, eventId);
        System.out.println("Создан запрос: " + dto);
        return dto;
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public RequestDto cancelRequest(@PathVariable("userId") Long id,
                                    @PathVariable("requestId") Long requestId) {
        System.out.println("=== Вход в метод cancelRequest ===");
        System.out.println("userId: " + id);
        System.out.println("requestId: " + requestId);

        if (id == null || requestId == null) {
            throw new IllegalArgumentException("userId и requestId не могут быть null");
        }

        RequestDto dto = requestService.cancelRequest(id, requestId);
        System.out.println("Отменён запрос: " + dto);
        return dto;
    }

    @GetMapping("/{userId}/requests")
    public List<RequestDto> getRequestDtosByUserId(@PathVariable("userId") Long id) {
        System.out.println("=== Вход в метод getRequestDtosByUserId ===");
        System.out.println("userId: " + id);

        if (id == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }

        List<RequestDto> list = requestService.getRequestDtosByUserId(id);
        System.out.println("Найдено запросов: " + list.size());
        list.forEach(dto -> System.out.println("Запрос: " + dto));
        return list;
    }
}

