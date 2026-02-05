package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.*;

import java.util.List;

public interface EventService {

    EventFullDto create(Long userId, NewEventDto newEventDto);

    EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest request);

    EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest request);

    EventFullDto getByUser(Long userId, Long eventId);

    EventFullDto getPublicEvent(Long eventId, HttpServletRequest request);

    List<EventShortDto> getAllByUser(Long userId, Integer from, Integer size);

    List<EventFullDto> searchForAdmin(EventSearchRequestAdmin param);

    List<EventShortDto> searchForUser(EventSearchRequestUser param, HttpServletRequest request);
}
