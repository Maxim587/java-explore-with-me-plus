package ru.practicum.service;

import ru.practicum.dto.*;
import ru.practicum.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    EventFullDto create(Long userId, NewEventDto newEventDto);

    EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest request);

    EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest request);

    EventFullDto getByUser(Long userId, Long eventId);

    EventFullDto getPublicEvent(Long eventId);

    List<EventShortDto> getAllByUser(Long userId, Integer from, Integer size);

    List<EventFullDto> searchForAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                      LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    List<EventShortDto> searchForUser(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                      LocalDateTime rangeEnd, Boolean onlyAvailable,
                                      String sort, Integer from, Integer size);

//    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

//    EventRequestStatusUpdateResult updateRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest request);
}
