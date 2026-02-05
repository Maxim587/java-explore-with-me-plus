package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import org.mapstruct.factory.Mappers;
import ru.practicum.dto.*;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.model.User;

import java.time.LocalDateTime;

import static ru.practicum.service.EventServiceImpl.DATE_TIME_FORMATTER;

@UtilityClass
public class EventMapper {
    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    public static Event mapToEvent(NewEventDto newEventDto, User user, Category category, LocalDateTime eventDate) {
        Event event = new Event();

        event.setAnnotation(newEventDto.getAnnotation());
        event.setDescription(newEventDto.getDescription());
        event.setEventDate(eventDate);
        event.setCreatedOn(LocalDateTime.now());
        event.setInitiator(user);
        event.setCategory(category);
        event.getLocation().setLat(newEventDto.getLocation().getLat());
        event.getLocation().setLon(newEventDto.getLocation().getLon());
        event.setPaid(newEventDto.getPaid());
        event.setParticipantLimit(newEventDto.getParticipantLimit());
        event.setRequestModeration(newEventDto.getRequestModeration());
        event.setState(EventState.PENDING);
        event.setTitle(newEventDto.getTitle());

        return event;
    }

    public static EventFullDto mapToFullDto(Event event) {
        Location location = new Location(event.getLocation().getLat(), event.getLocation().getLon());
        EventFullDto fullDto = new EventFullDto();

        fullDto.setAnnotation(event.getAnnotation());
        fullDto.setCategory(event.getCategory() != null ? CategoryMapper.mapToCategoryDto(event.getCategory()) : null);
        fullDto.setCreatedOn(DATE_TIME_FORMATTER.format(event.getCreatedOn()));
        fullDto.setDescription(event.getDescription());
        fullDto.setEventDate(event.getEventDate() != null ? DATE_TIME_FORMATTER.format(event.getEventDate()) : "");
        fullDto.setId(event.getId());
        fullDto.setInitiator(userMapper.toShortDto(event.getInitiator()));
        fullDto.setLocation(location);
        fullDto.setPaid(event.getPaid() != null ? event.getPaid() : false);
        fullDto.setParticipantLimit(event.getParticipantLimit());
        fullDto.setPublishedOn(event.getPublishedOn() != null ? DATE_TIME_FORMATTER.format(event.getPublishedOn()) : "");
        fullDto.setRequestModeration(event.getRequestModeration() != null ? event.getRequestModeration() : true);
        fullDto.setState(event.getState().toString());
        fullDto.setTitle(event.getTitle());

        return fullDto;
    }

    public static EventShortDto mapToShortDto(Event event) {
        EventShortDto shortDto = new EventShortDto();

        shortDto.setAnnotation(event.getAnnotation());
        shortDto.setCategory(event.getCategory() != null ? CategoryMapper.mapToCategoryDto(event.getCategory()) : null);
        shortDto.setEventDate(event.getEventDate() != null ? DATE_TIME_FORMATTER.format(event.getEventDate()) : "");
        shortDto.setId(event.getId());
        shortDto.setInitiator(event.getInitiator() != null ? userMapper.toShortDto(event.getInitiator()) : null);
        shortDto.setPaid(event.getPaid() != null ? event.getPaid() : false);
        shortDto.setTitle(event.getTitle());

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

        if (request.getEventDate() != null) {
            LocalDateTime newEventDate = LocalDateTime.parse(request.getEventDate(), DATE_TIME_FORMATTER);
            if (!newEventDate.isAfter(LocalDateTime.now())) {
                throw new ValidationException("Дата события должна быть больше текущей даты");
            }
            event.setEventDate(LocalDateTime.parse(request.getEventDate(), DATE_TIME_FORMATTER));
        }
    }

}
