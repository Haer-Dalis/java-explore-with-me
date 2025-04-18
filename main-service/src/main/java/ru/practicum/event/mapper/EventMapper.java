package ru.practicum.event.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.EventNewDto;
import ru.practicum.event.dto.State;
import ru.practicum.event.dto.EventUpdateDto;
import ru.practicum.event.dto.StateAction;
import ru.practicum.event.model.Event;
import ru.practicum.location.mapper.LocationMapper;
import ru.practicum.location.model.Location;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@UtilityClass
public class EventMapper {

    public static EventDto toEventDto(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .location(LocationMapper.toLocationDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .views(event.getViews())
                .build();
    }

    public static EventShortDto toEventShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .title(event.getTitle())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .views(event.getViews())
                .build();
    }

    public static Event toEvent(User initiator, Category category, EventNewDto eventNewDto, Location location) {
        Boolean paid = Boolean.FALSE;
        if (eventNewDto.getPaid() != null) {
            paid = eventNewDto.getPaid();
        }

        int participantLimit = 0;
        if (eventNewDto.getParticipantLimit() != null) {
            participantLimit = eventNewDto.getParticipantLimit();
        }

        Boolean requestModeration = Boolean.TRUE;
        if (eventNewDto.getRequestModeration() != null) {
            requestModeration = eventNewDto.getRequestModeration();
        }

        return Event.builder()
                .annotation(eventNewDto.getAnnotation())
                .category(category)
                .description(eventNewDto.getDescription())
                .createdOn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .eventDate(eventNewDto.getEventDate())
                .location(location)
                .paid(paid)
                .participantLimit(participantLimit)
                .requestModeration(requestModeration)
                .title(eventNewDto.getTitle())
                .confirmedRequests(0)
                .initiator(initiator)
                .views(0L)
                .build();
    }

    public static Event toUpdatedEvent(EventUpdateDto updateRequest, Category newCategory, Event oldEvent) {
        String annotation = oldEvent.getAnnotation();
        if (updateRequest.getAnnotation() != null) {
            annotation = updateRequest.getAnnotation();
        }

        String description = oldEvent.getDescription();
        if (updateRequest.getDescription() != null) {
            description = updateRequest.getDescription();
        }

        LocalDateTime eventDate = oldEvent.getEventDate();
        if (updateRequest.getEventDate() != null) {
            eventDate = updateRequest.getEventDate();
        }

        Location location = oldEvent.getLocation();
        if (updateRequest.getLocation() != null) {
            location = updateRequest.getLocation();
        }

        Boolean paid = oldEvent.getPaid();
        if (updateRequest.getPaid() != null) {
            paid = updateRequest.getPaid();
        }

        Integer participantLimit = oldEvent.getParticipantLimit();
        if (updateRequest.getParticipantLimit() != null) {
            participantLimit = updateRequest.getParticipantLimit();
        }

        Boolean requestModeration = oldEvent.getRequestModeration();
        if (updateRequest.getRequestModeration() != null) {
            requestModeration = updateRequest.getRequestModeration();
        }

        String title = oldEvent.getTitle();
        if (updateRequest.getTitle() != null) {
            title = updateRequest.getTitle();
        }

        Event updatedEvent = Event.builder()
                .id(oldEvent.getId())
                .annotation(annotation)
                .category(newCategory)
                .description(description)
                .createdOn(oldEvent.getCreatedOn())
                .eventDate(eventDate)
                .location(location)
                .paid(paid)
                .participantLimit(participantLimit)
                .requestModeration(requestModeration)
                .title(title)
                .confirmedRequests(oldEvent.getConfirmedRequests())
                .initiator(oldEvent.getInitiator())
                .views(oldEvent.getViews())
                .state(oldEvent.getState())
                .build();

        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == StateAction.SEND_TO_REVIEW) {
                updatedEvent.setState(State.PENDING);
            } else if (updateRequest.getStateAction() == StateAction.REJECT_EVENT || updateRequest.getStateAction() == StateAction.CANCEL_REVIEW) {
                updatedEvent.setState(State.CANCELED);
            } else if (updateRequest.getStateAction() == StateAction.PUBLISH_EVENT) {
                updatedEvent.setState(State.PUBLISHED);
                updatedEvent.setPublishedOn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            }
        }

        return updatedEvent;
    }
}