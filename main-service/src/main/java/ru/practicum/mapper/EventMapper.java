package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import org.mapstruct.factory.Mappers;
import ru.practicum.dto.*;
import ru.practicum.exception.ConditionsConflictException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.*;

import java.time.LocalDateTime;

import static ru.practicum.model.EventStateAdmin.PUBLISH_EVENT;
import static ru.practicum.model.EventStateAdmin.REJECT_EVENT;
import static ru.practicum.service.EventServiceImpl.DATE_TIME_FORMATTER;

@UtilityClass
public class EventMapper {
    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    private final LocationMapper locationMapper = Mappers.getMapper(LocationMapper.class);

    public static Event mapToEvent(NewEventDto newEventDto, User user, Category category, LocalDateTime eventDate) {
        Event event = new Event();

        event.setAnnotation(newEventDto.getAnnotation());
        event.setDescription(newEventDto.getDescription());
        event.setEventDate(eventDate);
        event.setCreatedOn(LocalDateTime.now());
        event.setInitiator(user);
        event.setCategory(category);
        event.setLocation(locationMapper.mapLocationToEventLocation(newEventDto.getLocation()));
        event.setPaid(newEventDto.getPaid());
        event.setParticipantLimit(newEventDto.getParticipantLimit());
        event.setRequestModeration(newEventDto.getRequestModeration());
        event.setState(EventState.PENDING);
        event.setTitle(newEventDto.getTitle());

        return event;
    }

    public static EventFullDto mapToFullDto(Event event) {
        EventFullDto fullDto = new EventFullDto();

        fullDto.setAnnotation(event.getAnnotation());
        fullDto.setCategory(CategoryMapper.mapToCategoryDto(event.getCategory()));
        fullDto.setCreatedOn(DATE_TIME_FORMATTER.format(event.getCreatedOn()));
        fullDto.setDescription(event.getDescription());
        fullDto.setEventDate(DATE_TIME_FORMATTER.format(event.getEventDate()));
        fullDto.setId(event.getId());
        fullDto.setInitiator(userMapper.toShortDto(event.getInitiator()));
        fullDto.setLocation(locationMapper.mapEventLocationToLocation(event.getLocation()));
        fullDto.setPaid(event.isPaid());
        fullDto.setParticipantLimit(event.getParticipantLimit());
        fullDto.setPublishedOn(event.getPublishedOn() != null ? DATE_TIME_FORMATTER.format(event.getPublishedOn()) : null);
        fullDto.setRequestModeration(event.getRequestModeration());
        fullDto.setState(event.getState());
        fullDto.setTitle(event.getTitle());

        return fullDto;
    }

    public static EventShortDto mapToShortDto(Event event) {
        EventShortDto shortDto = new EventShortDto();

        shortDto.setAnnotation(event.getAnnotation());
        shortDto.setCategory(CategoryMapper.mapToCategoryDto(event.getCategory()));
        shortDto.setEventDate(DATE_TIME_FORMATTER.format(event.getEventDate()));
        shortDto.setId(event.getId());
        shortDto.setInitiator(userMapper.toShortDto(event.getInitiator()));
        shortDto.setPaid(event.isPaid());
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

        if (request.getLocation() != null) {
            event.setLocation(locationMapper.mapLocationToEventLocation(request.getLocation()));
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

        if (request.getStateAction() != null) {
            EventStateAdmin stateAdmin = EventStateAdmin.fromString(request.getStateAction());
            if (stateAdmin.equals(PUBLISH_EVENT)) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConditionsConflictException("Событие можно публиковать только если оно в состоянии ожидания публикации");
                }
                event.setState(EventState.PUBLISHED);
            } else if (stateAdmin.equals(REJECT_EVENT)) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConditionsConflictException("Событие можно отклонить только если оно еще не опубликовано");
                }
                event.setState(EventState.CANCELED);
            }
        }
    }

}
