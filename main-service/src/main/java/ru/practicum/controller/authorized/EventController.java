package ru.practicum.controller.authorized;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.StatsClient;
import ru.practicum.dto.*;
import ru.practicum.service.EventService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/events")
public class EventController {
    private final EventService eventService;
    private final StatsClient statsClient;

    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId, @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Пользователь отправил запрос на создание события");
        return eventService.create(userId, newEventDto);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateByUser(@PathVariable Long userId,
                                     @PathVariable Long eventId,
                                     @Valid @RequestBody UpdateEventUserRequest request) {
        log.info("Пользователь с id {}, обновил событие с id {}", userId, eventId);
        return eventService.updateByUser(userId, eventId, request);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getByUser(Long userId, Long eventId) {
        log.info("Получение информации о событии, добавленном пользователем {}", userId);
        return eventService.getByUser(userId, eventId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getAllByUser(@PathVariable Long userId,
                                            @RequestParam(defaultValue = "0") Integer from,
                                            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получение событий, добавленных пользователем {}", userId);
        return eventService.getAllByUser(userId, from, size);
    }

    @GetMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Получение информации о запросах на участие в событии текущего пользователя");
        return eventService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable Long userId,
                                                              @PathVariable Long eventId,
                                                              @Valid @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("Изменение статуса заявок на участие в событии текущего пользователя");
        return eventService.updateRequests(userId, eventId, request);
    }
}
