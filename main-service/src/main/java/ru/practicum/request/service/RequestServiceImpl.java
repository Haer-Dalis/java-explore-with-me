package ru.practicum.request.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import ru.practicum.event.dto.State;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.Status;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;


import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public RequestDto addRequest(Long userId, Long eventId) {
        validateEventId(eventId);
        ensureRequestDoesNotExist(userId, eventId);

        User user = getUserById(userId);
        Event event = getEventById(eventId);

        validateRequestConditions(userId, event);

        Request request = RequestMapper.toRequest(event, user);

        if (canAutoConfirm(event)) {
            confirmRequestAndUpdateEvent(event, request);
        }

        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    public RequestDto cancelRequest(Long userId, Long requestId) {
        getUserById(userId);

        Request request = getRequestById(requestId);
        validateCancelPermission(userId, request);

        request.setStatus(Status.CANCELED);
        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    public List<RequestDto> getRequestDtosByUserId(Long userId) {
        getUserById(userId);

        return requestRepository.findAllByRequesterId(userId, Sort.by(Sort.Direction.DESC, "created"))
                .stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    private void validateEventId(Long eventId) {
        if (eventId == null) {
            throw new ValidationException("eventId не может быть пустым");
        }
    }

    private void ensureRequestDoesNotExist(Long userId, Long eventId) {
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Нельзя создавать одинаковый запрос на уже существующее событие");
        }
    }

    private void validateRequestConditions(Long userId, Event event) {
        if (userId.equals(event.getInitiator().getId())) {
            throw new ConflictException("Вы хотите на событие к себе самому?");
        }

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Событие должно быть опубликованно");
        }

        if (event.getParticipantLimit() != 0 &&
                event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Не больше лимита");
        }
    }

    private boolean canAutoConfirm(Event event) {
        return event.getParticipantLimit() == 0 || !event.getRequestModeration();
    }

    private void confirmRequestAndUpdateEvent(Event event, Request request) {
        request.setStatus(Status.CONFIRMED);
        event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        eventRepository.save(event);
    }

    private void validateCancelPermission(Long userId, Request request) {
        if (request.getStatus().equals(Status.CANCELED)) {
            throw new ValidationException("Событие было отменено");
        }

        if (!userId.equals(request.getRequester().getId())) {
            throw new ValidationException("Попытка несанкционированного доступа");
        }
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не обнаружен"));
    }

    private Request getRequestById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запрос с id = " + id + " не обнаружен"));
    }

    private Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + id + " не обнаружено"));
    }
}