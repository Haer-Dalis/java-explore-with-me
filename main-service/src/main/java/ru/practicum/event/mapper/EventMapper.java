package ru.practicum.event.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.*;
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

    public static Event toEvent(User initiator, Category category, NewEventDto newEventDto, Location location) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .description(newEventDto.getDescription())
                .createdOn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .eventDate(newEventDto.getEventDate())
                .location(location)
                .paid(newEventDto.getPaid() == null ? Boolean.FALSE : newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit() == null ? 0 : newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration() == null ? Boolean.TRUE : newEventDto.getRequestModeration())
                .title(newEventDto.getTitle())
                .confirmedRequests(0)
                .initiator(initiator)
                .views(0L)
                .build();
    }

    public static Event toUpdatedEvent(UpdateEventDto updateRequest, Category newCategory, Event oldEvent) {
        Event updatedEvent = Event.builder()
                .id(oldEvent.getId())
                .annotation(updateRequest.getAnnotation() != null ? updateRequest.getAnnotation() : oldEvent.getAnnotation())
                .category(newCategory)
                .description(updateRequest.getDescription() != null ? updateRequest.getDescription() : oldEvent.getDescription())
                .createdOn(oldEvent.getCreatedOn())
                .eventDate(updateRequest.getEventDate() != null ? updateRequest.getEventDate() : oldEvent.getEventDate())
                .location(updateRequest.getLocation() != null ? updateRequest.getLocation() : oldEvent.getLocation())
                .paid(updateRequest.getPaid() != null ? updateRequest.getPaid() : oldEvent.getPaid())
                .participantLimit(updateRequest.getParticipantLimit() != null ? updateRequest.getParticipantLimit() : oldEvent.getParticipantLimit())
                .requestModeration(updateRequest.getRequestModeration() != null ? updateRequest.getRequestModeration() : oldEvent.getRequestModeration())
                .title(updateRequest.getTitle() != null ? updateRequest.getTitle() : oldEvent.getTitle())
                .confirmedRequests(oldEvent.getConfirmedRequests())
                .initiator(oldEvent.getInitiator())
                .views(oldEvent.getViews())
                .state(oldEvent.getState())
                .build();

        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case SEND_TO_REVIEW -> updatedEvent.setState(State.PENDING);
                case REJECT_EVENT, CANCEL_REVIEW -> updatedEvent.setState(State.CANCELED);
                case PUBLISH_EVENT -> {
                    updatedEvent.setState(State.PUBLISHED);
                    updatedEvent.setPublishedOn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
                }
            }
        }
        return updatedEvent;
    }
}
