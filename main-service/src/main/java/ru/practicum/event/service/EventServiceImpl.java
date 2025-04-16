package ru.practicum.event.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        Event event = EventMapper.toEvent(
                getUserById(userId),
                getCategoryById(newEventDto.getCategory()),
                newEventDto,
                locationRepository.save(LocationMapper.toLocation(newEventDto.getLocation()))
        );
        event.setState(State.PENDING);

        return EventMapper.toEventDto(eventRepository.save(event));
    }

    @Override
    public EventDto updateEvent(Long userId, Long eventId, UpdateEventDto updateEventDto) {
        Event event = getEventByIdAndInitiator(eventId, userId);

        if (event.getState() == State.PUBLISHED) {
            throw new ConflictException("События можно изменять в статусах PENDING или CANCELED");
        }
        if (updateEventDto.getEventDate() != null) {
            checkDateTime(updateEventDto.getEventDate());
        }

        Category category = updateEventDto.getCategory() != null
                ? getCategoryById(updateEventDto.getCategory())
                : event.getCategory();

        return EventMapper.toEventDto(
                eventRepository.save(EventMapper.toUpdatedEvent(updateEventDto, category, event))
        );
    }

    @Override
    public EventDto getByUserAndId(Long userId, Long eventId) {
        return EventMapper.toEventDto(getEventByIdAndInitiator(eventId, userId));
    }

    @Override
    public List<EventShortDto> getAllByUser(Long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.findByInitiatorId(userId, pageable).stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestDto> getRequestsByUser(Long ownerId, Long eventId) {
        getEventByIdAndInitiator(eventId, ownerId);
        return requestRepository.findRequestsByEventId(eventId, Sort.by(Sort.Direction.DESC, "created"))
                .stream()
                .map(RequestMapper::toRequestDto)
                .toList();
    }

    @Override
    public RequestUpdateResultDto updateRequests(Long ownerId, Long eventId, EventRequestUpdate eventRequestStatus) {
        Event event = getEventByIdAndInitiator(eventId, ownerId);
        List<Request> requests = requestRepository.findAllById(eventRequestStatus.getRequestIds());

        List<Request> confirmed = new ArrayList<>();
        List<Request> rejected = new ArrayList<>();

        requests.forEach(request -> {
            if (request.getStatus() != Status.PENDING) {
                throw new ConflictException("Статус можно менять только в состоянии ожидания");
            }
            if (!request.getEvent().getId().equals(eventId)) {
                throw new ConflictException("Запрос с id " + request.getId() + " не связан с событием id " + eventId);
            }

            if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
                throw new ConflictException("Достигнут лимит участников");
            }

            if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
                confirmRequest(request, event);
                confirmed.add(request);
            } else {
                switch (eventRequestStatus.getStatus()) {
                    case CONFIRMED -> {
                        confirmRequest(request, event);
                        confirmed.add(request);
                    }
                    case REJECTED -> {
                        request.setStatus(Status.REJECTED);
                        rejected.add(request);
                    }
                    default -> throw new ConflictException("Неверный статус: " + eventRequestStatus.getStatus());
                }
            }
        });

        if (event.getConfirmedRequests().equals(event.getParticipantLimit())) {
            requests.stream()
                    .filter(r -> r.getStatus() == Status.PENDING)
                    .forEach(r -> {
                        r.setStatus(Status.REJECTED);
                        rejected.add(r);
                    });
        }

        requestRepository.saveAll(requests);
        eventRepository.save(event);

        return RequestUpdateResultDto.builder()
                .confirmedRequests(confirmed.stream().map(RequestMapper::toRequestDto).toList())
                .rejectedRequests(rejected.stream().map(RequestMapper::toRequestDto).toList())
                .build();
    }

    private void confirmRequest(Request request, Event event) {
        request.setStatus(Status.CONFIRMED);
        event.setConfirmedRequests(event.getConfirmedRequests() + 1);
    }

    private Event getEventByIdAndInitiator(Long eventId, Long userId) {
        Event event = getEventById(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Попытка несанкционированного доступа");
        }
        return event;
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

    private void checkDateTime(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Дата события должна быть не раньше чем через 2 часа от текущего момента");
        }
    }
}
