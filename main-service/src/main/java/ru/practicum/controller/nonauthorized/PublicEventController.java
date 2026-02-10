package ru.practicum.controller.nonauthorized;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.NewEndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventSearchRequestUser;
import ru.practicum.dto.EventShortDto;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.service.EventServiceImpl.DATE_TIME_FORMATTER;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events")
public class PublicEventController {
    private static final String APP_NAME = "main-service";
    private final EventService eventService;
    private final StatsClient statsClient;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        log.info("Получение информации о событии");

        NewEndpointHitDto hitDto = new NewEndpointHitDto(APP_NAME, request.getRequestURI(),
                request.getRemoteAddr(), LocalDateTime.now().format(DATE_TIME_FORMATTER));

        log.info("Отправка запроса в сервис статистики из метода getEvent() с dto={}", hitDto);
        statsClient.hit(hitDto);
        log.info("Отправка запроса в сервис статистики из метода getEvent() завершена успешно");
        return eventService.getPublicEvent(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> searchForUser(@RequestParam(required = false) String text,
                                             @RequestParam(required = false) List<Long> categories,
                                             @RequestParam(required = false) Boolean paid,
                                             @RequestParam(required = false)
                                             @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                             @RequestParam(required = false)
                                             @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                             @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                             @RequestParam(required = false) String sort,
                                             @RequestParam(defaultValue = "0") Integer from,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             HttpServletRequest request) {
        log.info("Получение событий публичным эндпоинтом");
        EventSearchRequestUser param = new EventSearchRequestUser(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        log.info("Сформирован DTO с параметрами запроса {}", param);

        List<EventShortDto> resp = eventService.searchForUser(param);

        NewEndpointHitDto hitDto = new NewEndpointHitDto(APP_NAME, request.getRequestURI(),
                request.getRemoteAddr(), LocalDateTime.now().format(DATE_TIME_FORMATTER));

        log.info("Отправка запроса в сервис статистики из метода searchForUser() с dto={}", hitDto);
        statsClient.hit(hitDto);
        log.info("Отправка запроса в сервис статистики из метода searchForUser() завершена успешно");

        return resp;
    }
}
