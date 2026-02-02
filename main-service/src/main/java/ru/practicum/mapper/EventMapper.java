package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.*;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.model.User;

import java.time.LocalDateTime;

import static ru.practicum.service.EventServiceImpl.DATE_TIME_FORMATTER;

@UtilityClass
public class EventMapper {

    public static Event mapToEvent(NewEventDto newEventDto, User user, Category category, LocalDateTime eventDate) {
        Event event = new Event();

        event.setAnnotation(newEventDto.getAnnotation());
        event.setDescription(newEventDto.getDescription());
        event.setEventDate(eventDate);
        event.setCreatedOn(LocalDateTime.now());
        event.setInitiator(user);
        event.setCategory(category);
        event.setConfirmedRequests(0L);
        event.getLocation().setLat(newEventDto.getLocation().getLat());
        event.getLocation().setLon(newEventDto.getLocation().getLon());
        event.setPaid(newEventDto.getPaid());
        event.setParticipantLimit(newEventDto.getParticipantLimit());
        event.setRequestModeration(newEventDto.getRequestModeration());
        event.setState(EventState.PENDING);
        event.setTitle(newEventDto.getTitle());
        event.setViews(0L);

        return event;
    }

    public static EventFullDto mapToFullDto(Event event) {
        Location location = new Location(event.getLocation().getLat(), event.getLocation().getLon());
        EventFullDto fullDto = new EventFullDto();

        fullDto.setAnnotation(event.getAnnotation());
        fullDto.setCategory(event.getCategory() != null ? CategoryMapper.mapToCategoryDto(event.getCategory()) : null);
        fullDto.setConfirmedRequests(event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0L);
        fullDto.setCreatedOn(DATE_TIME_FORMATTER.format(event.getCreatedOn()));
        fullDto.setDescription(event.getDescription());
        fullDto.setEventDate(event.getEventDate() != null ? DATE_TIME_FORMATTER.format(event.getEventDate()) : "");
        fullDto.setId(event.getId());
        fullDto.setInitiator(UserMapper.toShortDto(event.getInitiator()));
        fullDto.setLocation(location);
        fullDto.setPaid(event.getPaid() != null ? event.getPaid() : false);
        fullDto.setParticipantLimit(event.getParticipantLimit());
        fullDto.setPublishedOn(event.getPublishedOn() != null ? DATE_TIME_FORMATTER.format(event.getPublishedOn()) : "");
        fullDto.setRequestModeration(event.getRequestModeration() != null ? event.getRequestModeration() : true);
        fullDto.setState(event.getState().toString());
        fullDto.setTitle(event.getTitle());
        fullDto.setViews(event.getViews() != null ? event.getViews() : 0L);

        return fullDto;
    }

    public static EventShortDto mapToShortDto(Event event) {
        EventShortDto shortDto = new EventShortDto();

        shortDto.setAnnotation(event.getAnnotation());
        shortDto.setCategory(event.getCategory() != null ? CategoryMapper.mapToCategoryDto(event.getCategory()) : null);
        shortDto.setConfirmedRequests(event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0L);
        shortDto.setEventDate(event.getEventDate() != null ? DATE_TIME_FORMATTER.format(event.getEventDate()) : "");
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

        if (request.getLocation() != null && request.getLocation().getLat() != null
            && request.getLocation().getLon() != null) {
            event.getLocation().setLat(request.getLocation().getLat());
            event.getLocation().setLon(request.getLocation().getLon());
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

        if (request.getLocation() != null && request.getLocation().getLat() != null
            && request.getLocation().getLon() != null) {
            event.getLocation().setLat(request.getLocation().getLat());
            event.getLocation().setLon(request.getLocation().getLon());
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
