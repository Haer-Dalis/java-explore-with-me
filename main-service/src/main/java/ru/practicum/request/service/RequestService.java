package ru.practicum.request.service;

import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.Request;

import java.util.List;

public interface RequestService {

    RequestDto addRequest(Long id, Long eventId);

    RequestDto cancelRequest(Long id, Long requestId);

    List<RequestDto> getRequestDtosByUserId(Long id);
}
