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
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventRequestUpdate;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.State;
import ru.practicum.event.dto.UpdateEventDto;
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
import ru.practicum.request.dto.RequestUpdateResultDto;
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
        checkDateTime(newEventDto.getEventDate());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        Category category = getCategoryById(newEventDto.getCategory());
        Location location = locationRepository.save(LocationMapper.toLocation(newEventDto.getLocation()));

        Event event = EventMapper.toEvent(user, category, newEventDto, location);
        event.setState(State.PENDING);

        try {
            event = eventRepository.save(event);
        } catch (DataIntegrityViolationException ex) {
            throw new ValidationException("Ошибка сохранения события");
        }

        return EventMapper.toEventDto(event);
    }

    @Override
    public EventDto updateEvent(Long userId, Long eventId, UpdateEventDto updateEventDto) {
        checkExistUser(userId);
        Event event = getEventById(eventId);
        validateInitiator(userId, event);

        if (event.getState() == State.PUBLISHED) {
            throw new ConflictException("Нельзя редактировать опубликованные события");
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
    public EventDto getByUserAndId(Long userId, Long eventId) {
        checkExistUser(userId);
        Event event = getEventById(eventId);
        validateInitiator(userId, event);
        return EventMapper.toEventDto(event);
    }

    @Override
    public List<EventShortDto> getAllByUser(Long userId, Integer from, Integer size) {
        checkExistUser(userId);
        Pageable pageable = PageRequest.of(Math.max(0, from / size), size);
        return eventRepository.findByInitiatorId(userId, pageable).stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestDto> getRequestsByUser(Long ownerId, Long eventId) {
        checkExistUser(ownerId);
        Event event = getEventById(eventId);
        validateInitiator(ownerId, event);

        List<Request> requests = requestRepository.findRequestsByEventId(eventId,
                Sort.by(Sort.Direction.DESC, "created"));
        return mapToDtoList(requests);
    }

    @Override
    public RequestUpdateResultDto updateRequests(Long ownerId, Long eventId,
                                                               EventRequestUpdate eventRequestUpdate) {
        checkExistUser(ownerId);
        Event event = getEventById(eventId);
        validateInitiator(ownerId, event);

        List<Request> requests = requestRepository.findAllById(eventRequestUpdate.getRequestIds());
        List<Request> confirmed = new ArrayList<>();
        List<Request> rejected = new ArrayList<>();

        for (Request request : requests) {
            validatePendingRequest(request);

            if (!request.getEvent().getId().equals(eventId)) {
                throw new ConflictException("Запрос не относится к указанному событию");
            }

            boolean canConfirm = event.getParticipantLimit() == 0
                    || event.getConfirmedRequests() < event.getParticipantLimit();

            if (!canConfirm) {
                updateRequestStatus(request, Status.REJECTED, null);
                rejected.add(request);
                continue;
            }

            switch (eventRequestUpdate.getStatus()) {
                case CONFIRMED -> {
                    updateRequestStatus(request, Status.CONFIRMED, event);
                    confirmed.add(request);
                }
                case REJECTED -> {
                    updateRequestStatus(request, Status.REJECTED, null);
                    rejected.add(request);
                }
                default -> throw new ConflictException("Неверный статус: " + eventRequestUpdate.getStatus());
            }
        }

        requestRepository.saveAll(requests);
        eventRepository.save(event);

        return RequestUpdateResultDto.builder()
                .confirmedRequests(mapToDtoList(confirmed))
                .rejectedRequests(mapToDtoList(rejected))
                .build();
    }

    private void validateInitiator(Long userId, Event event) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Доступ запрещён: пользователь не является инициатором события");
        }
    }

    private void validatePendingRequest(Request request) {
        if (request.getStatus() != Status.PENDING) {
            throw new ConflictException("Статус запроса должен быть PENDING");
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

    private Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория с id " + id + " не найдена"));
    }

    private Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие с id " + id + " не найдено"));
    }

    private void checkExistUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не существует");
        }
    }

    private void checkDateTime(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Событие должно начинаться минимум через два часа");
        }
    }
}
