package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import org.mapstruct.factory.Mappers;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.model.User;

import java.time.LocalDateTime;

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


}
