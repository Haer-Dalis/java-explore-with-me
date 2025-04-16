package ru.practicum.request.service;

import lombok.AllArgsConstructor;
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

        User user = getUser(userId);
        Event event = getEvent(eventId);
        validateRequestConditions(userId, event);

        Request request = RequestMapper.toRequest(event, user);

        if (canAutoConfirm(event)) {
            confirmRequestAndUpdateEvent(event, request);
        }

        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    public RequestDto cancelRequest(Long userId, Long requestId) {
        Request request = getRequest(requestId);
        validateCancelPermission(userId, request);

        request.setStatus(Status.CANCELED);
        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    public List<RequestDto> getRequestDtosByUserId(Long userId) {
        return requestRepository.findByUserId(userId)
                .stream()
                .map(RequestMapper::toRequestDto)
                .toList();
    }

    private void validateEventId(Long eventId) {
        if (eventId == null) {
            throw new ValidationException(" ID события не может быть null");
        }
    }

    private void ensureRequestDoesNotExist(Long userId, Long eventId) {
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Запрос уже есть для этого пользователя и события");
        }
    }

    private void validateRequestConditions(Long userId, Event event) {
        if (userId.equals(event.getInitiator().getId())) {
            throw new ConflictException("Нельзя участвовать в своем событии");
        }

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("событие должно быть опубликовано");
        }

        if (event.getParticipantLimit() != 0 &&
                event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников");
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
            throw new ValidationException("Уже отменен");
        }

        if (!userId.equals(request.getRequester().getId())) {
            throw new ValidationException("Нет разрешения");
        }
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Юзер не обнаружен id: " + id));
    }

    private Request getRequest(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запрос не найден id: " + id));
    }

    private Event getEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие не найдено id: " + id));
    }
}