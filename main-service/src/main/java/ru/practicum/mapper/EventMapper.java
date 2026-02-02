package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.*;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.model.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class EventMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC);

    public static Event mapToEvent(NewEventDto newEventDto, User user, Category category) {
        Event event = new Event();

        event.setAnnotation(newEventDto.getAnnotation());
        event.setDescription(newEventDto.getDescription());
        event.setEventDate(LocalDateTime.from(FORMATTER.parse(newEventDto.getEventDate())));
        event.setCreatedOn(LocalDateTime.now());
        event.setInitiator(user);
        event.setCategory(category);
        event.setConfirmedRequests(0L);
        event.setLat(newEventDto.getLocation().getLat());
        event.setLon(newEventDto.getLocation().getLon());
        event.setPaid(newEventDto.getPaid());
        event.setParticipantLimit(newEventDto.getParticipantLimit());
        event.setRequestModeration(newEventDto.getRequestModeration());
        event.setState(EventState.PENDING);
        event.setTitle(newEventDto.getTitle());
        event.setViews(0L);

        return event;
    }

    public static EventFullDto mapToFullDto(Event event) {
        Location location = new Location(event.getLat(), event.getLon());
        EventFullDto fullDto = new EventFullDto();

        fullDto.setAnnotation(event.getAnnotation());
        fullDto.setCategory(event.getCategory() != null ? CategoryMapper.mapToCategoryDto(event.getCategory()) : null);
        fullDto.setConfirmedRequests(event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0L);
        fullDto.setCreatedOn(FORMATTER.format(event.getCreatedOn()));
        fullDto.setDescription(event.getDescription());
        fullDto.setEventDate(event.getEventDate() != null ? FORMATTER.format(event.getEventDate()) : "");
        fullDto.setId(event.getId());
        fullDto.setInitiator(UserMapper.toShortDto(event.getInitiator()));
        fullDto.setLocation(location);
        fullDto.setPaid(event.getPaid() != null ? event.getPaid() : false);
        fullDto.setParticipantLimit(event.getParticipantLimit());
        fullDto.setPublishedOn(event.getPublishedOn() != null ? FORMATTER.format(event.getPublishedOn()) : "");
        fullDto.setRequestModeration(event.getRequestModeration() != null ? event.getRequestModeration() : true);
        fullDto.setState(event.getState().toString());
        fullDto.setTitle(event.getTitle());
        fullDto.setViews(event.getViews() != null ? event.getViews() : 0L);

        return fullDto;
    }

    public EventShortDto mapToShortDto(Event event) {
        EventShortDto shortDto = new EventShortDto();

        shortDto.setAnnotation(event.getAnnotation());
        shortDto.setCategory(event.getCategory() != null ? CategoryMapper.mapToCategoryDto(event.getCategory()) : null);
        shortDto.setConfirmedRequests(event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0L);
        shortDto.setEventDate(event.getEventDate() != null ? FORMATTER.format(event.getEventDate()) : "");
        shortDto.setId(event.getId());
        shortDto.setInitiator(event.getInitiator() != null ? UserMapper.toShortDto(event.getInitiator()) : null);
        shortDto.setPaid(event.getPaid() != null ? event.getPaid() : false);
        shortDto.setTitle(event.getTitle());
        shortDto.setViews(event.getViews() != null ? event.getViews() : 0L);

        return shortDto;
    }

    public static void updateEventFromUserRequest(UpdateEventUserRequest request, Event event) {
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }

        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }

        if (request.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(request.getEventDate()));
        }

        if (request.getLocation() != null && request.getLocation().getLat() != null
                && request.getLocation().getLon() != null) {
            event.setLat(request.getLocation().getLat());
            event.setLon(request.getLocation().getLon());
        }

        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }

        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }

        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
    }

    public static void updateEventFromAdminRequest(UpdateEventAdminRequest request, Event event) {
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }

        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }

        if (request.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(request.getEventDate()));
        }

        if (request.getLocation() != null && request.getLocation().getLat() != null
                && request.getLocation().getLon() != null) {
            event.setLat(request.getLocation().getLat());
            event.setLon(request.getLocation().getLon());
        }

        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }

        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }

        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
    }

}
