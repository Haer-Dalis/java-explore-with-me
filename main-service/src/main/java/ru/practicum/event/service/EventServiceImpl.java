package ru.practicum.event.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.location.mapper.LocationMapper;
import ru.practicum.location.model.Location;
import ru.practicum.location.repository.LocationRepository;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.ResultRequestStatusDto;
import ru.practicum.request.dto.Status;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

    @Override
    public EventDto addEvent(Long userId, NewEventDto newEventDto) {
        log.info("Попытка добавить новое событие. userId={}, newEventDto={}", userId, newEventDto);

        checkDateTime(newEventDto.getEventDate());
        log.debug("Дата события прошла проверку: {}", newEventDto.getEventDate());

        User user = getUserById(userId);
        log.debug("Получен пользователь: {}", user);

        Category category = getCategoryById(newEventDto.getCategory());
        log.debug("Получена категория: {}", category);

        Location location = locationRepository.save(LocationMapper.toLocation(newEventDto.getLocation()));
        log.debug("Сохранена локация: {}", location);

        Event event = EventMapper.toEvent(user, category, newEventDto, location);
        event.setState(State.PENDING);
        log.debug("Создано событие: {}", event);

        try {
            event = eventRepository.save(event);
            log.info("Событие успешно сохранено в БД: {}", event);
        } catch (DataIntegrityViolationException exception) {
            log.error("Ошибка при сохранении события: {}", exception.getMessage(), exception);
            throw new ValidationException("Категория не может ничего не содержать");
        }

        EventDto eventDto = EventMapper.toEventDto(event);
        log.info("Результат добавления события: {}", eventDto);

        return eventDto;
    }

    @Override
    public EventDto updateEvent(Long userId, Long eventId, UpdateEventDto updateEventDto) {
        checkExistUser(userId);
        Event event = getEventById(eventId);

        validateInitiator(userId, event);
        if (event.getState() == State.PUBLISHED) {
            throw new ConflictException("События можно изменять в статусах PENDING или CANCELED");
        }

        if (updateEventDto.getEventDate() != null) {
            checkDateTime(updateEventDto.getEventDate());
        }

        Category category = updateEventDto.getCategory() != null
                ? getCategoryById(updateEventDto.getCategory())
                : event.getCategory();

        Event updatedEvent = EventMapper.toUpdatedEvent(updateEventDto, category, event);
        return EventMapper.toEventDto(eventRepository.save(updatedEvent));
    }

    @Override
    public EventDto getEventByUserIdAndEventId(Long userId, Long eventId) {
        checkExistUser(userId);
        Event event = getEventById(eventId);
        validateInitiator(userId, event);
        return EventMapper.toEventDto(event);
    }

    @Override
    public List<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size) {
        checkExistUser(userId);
        Pageable pageable = PageRequest.of(Math.max(0, from / size), size);
        return eventRepository.findByInitiatorId(userId, pageable).stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestDto> getRequestsByCurrentUserAndEventId(Long ownerId, Long eventId) {
        checkExistUser(ownerId);
        Event event = getEventById(eventId);
        validateInitiator(ownerId, event);

        List<Request> requests = requestRepository.findRequestsByEventId(eventId,
                Sort.by(Sort.Direction.DESC, "created"));

        return mapToDtoList(requests);
    }

    @Override
    public ResultRequestStatusDto changeRequestByCurrentUserId(Long ownerId, Long eventId,
                                                               EventRequestStatus eventRequestStatus) {
        checkExistUser(ownerId);
        Event event = getEventById(eventId);
        validateInitiator(ownerId, event);

        List<Request> requests = requestRepository.findAllById(eventRequestStatus.getRequestIds());
        List<Request> confirmed = new ArrayList<>();
        List<Request> rejected = new ArrayList<>();

        for (Request request : requests) {
            validatePendingRequest(request);
            if (!request.getEvent().getId().equals(eventId)) {
                throw new ConflictException("Запрос с id " + request.getId() +
                        " никак не связан с событием id " + eventId);
            }

            if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
                throw new ConflictException("Нельзя делать запросов больше, чем лимит");
            }

            if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
                updateRequestStatus(request, Status.CONFIRMED, event);
                confirmed.add(request);
                continue;
            }

            switch (eventRequestStatus.getStatus()) {
                case "CONFIRMED":
                    if (event.getConfirmedRequests() < event.getParticipantLimit()) {
                        updateRequestStatus(request, Status.CONFIRMED, event);
                        confirmed.add(request);
                    } else {
                        updateRequestStatus(request, Status.REJECTED, null);
                        rejected.add(request);
                    }
                    break;
                case "REJECTED":
                    updateRequestStatus(request, Status.REJECTED, null);
                    rejected.add(request);
                    break;
                default:
                    throw new ConflictException("Ошибка статуса " + eventRequestStatus.getStatus());
            }
        }

        if (event.getConfirmedRequests().equals(event.getParticipantLimit())) {
            for (Request request : requests) {
                if (request.getStatus() == Status.PENDING) {
                    updateRequestStatus(request, Status.REJECTED, null);
                    rejected.add(request);
                }
            }
        }

        requestRepository.saveAll(requests);
        eventRepository.save(event);

        return ResultRequestStatusDto.builder()
                .confirmedRequests(mapToDtoList(confirmed))
                .rejectedRequests(mapToDtoList(rejected))
                .build();
    }

    private void validateInitiator(Long userId, Event event) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Попытка несанкционированного доступа");
        }
    }

    private void validatePendingRequest(Request request) {
        if (request.getStatus() != Status.PENDING) {
            throw new ConflictException("Статус можно менять только в состоянии ожидания");
        }
    }

    private void updateRequestStatus(Request request, Status status, Event event) {
        request.setStatus(status);
        if (status == Status.CONFIRMED && event != null) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        }
    }

    private List<RequestDto> mapToDtoList(List<Request> requests) {
        return requests.stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не обнаружен"));
    }

    private Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория с id " + id + " не обнаружена"));
    }

    private Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие с id " + id + " не обнаружено"));
    }

    private void checkExistUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не обнаружен");
        }
    }

    private void checkDateTime(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Не соблюдено правило двух часов");
        }
    }
}
