package ru.practicum.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.NewEndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.dto.*;
import ru.practicum.exception.ConditionsConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.*;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);
    private static final String APP_NAME = "main-service";
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository requestRepository;
    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id = " + newEventDto.getCategory() + " не найдена"));

        LocalDateTime eventDate = LocalDateTime.parse(newEventDto.getEventDate(), DATE_TIME_FORMATTER);

        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Дата события должна быть не раньше чем через 2 часа от текущего момента");
        }

        Event savedEvent = eventRepository.save(EventMapper.mapToEvent(newEventDto, user, category, eventDate));
        EventFullDto dto = EventMapper.mapToFullDto(savedEvent);
        dto.setViews(0L);
        dto.setConfirmedRequests(0L);
        return EventMapper.mapToFullDto(savedEvent);
    }

    @Override
    @Transactional
    public EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConditionsConflictException("Нельзя редактировать опубликованное событие");
        }

        if (request.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(request.getEventDate(), DATE_TIME_FORMATTER);
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException(
                        "Дата и время события не может быть раньше, чем через 2 часа от текущего момента"
                );
            }
            event.setEventDate(eventDate);
        }

        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id " + request.getCategory() + " не найдена"));
            event.setCategory(category);
        }

        EventMapper.updateEventFromUserRequest(request, event);

        if (request.getStateAction() != null) {
            if (EventStateUser.valueOf(request.getStateAction()) == EventStateUser.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            } else if (EventStateUser.valueOf(request.getStateAction()) == EventStateUser.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            }
        }

        EventFullDto dto = EventMapper.mapToFullDto(eventRepository.save(event));
        dto.setViews(getEventViews(event));
        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
        dto.setConfirmedRequests(confirmedRequests);

        return dto;
    }

    @Override
    @Transactional
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (request.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(request.getEventDate(), DATE_TIME_FORMATTER);
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException(
                        "Дата и время события не может быть раньше, чем через 2 часа от текущего момента"
                );
            }
            event.setEventDate(eventDate);
        }

        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id " + request.getCategory() + " не найдена"));
            event.setCategory(category);
        }

        EventMapper.updateEventFromAdminRequest(request, event);

        if (request.getStateAction() != null) {
            if (EventStateAdmin.valueOf(request.getStateAction()) == EventStateAdmin.PUBLISH_EVENT) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConditionsConflictException("Событие можно публиковать только если оно в состоянии ожидания публикации");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (EventStateAdmin.valueOf(request.getStateAction()) == EventStateAdmin.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConditionsConflictException("Событие можно отклонить только если оно еще не опубликовано");
                }
                event.setState(EventState.CANCELED);
            }
        }

        EventFullDto dto = EventMapper.mapToFullDto(eventRepository.save(event));
        dto.setViews(getEventViews(event));
        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
        dto.setConfirmedRequests(confirmedRequests);
        return dto;
    }

    @Override
    public EventFullDto getByUser(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        Long views = getEventViews(event);
        EventFullDto dto = EventMapper.mapToFullDto(event);
        dto.setViews(views);
        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
        dto.setConfirmedRequests(confirmedRequests);

        return dto;
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие не опубликовано");
        }

        Long views = getEventViews(event);
        EventFullDto dto = EventMapper.mapToFullDto(event);
        dto.setViews(views);
        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
        dto.setConfirmedRequests(confirmedRequests);

        statsClient.hit(new NewEndpointHitDto(APP_NAME, request.getRequestURI(),
                request.getRemoteAddr(), LocalDateTime.now().format(DATE_TIME_FORMATTER)));

        return dto;
    }

    @Override
    public List<EventShortDto> getAllByUser(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable).getContent();
        Map<Long, Long> viewsMap = getEventsViews(events);
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequests(events);

        return events.stream()
                .map(event -> {
                    EventShortDto dto = EventMapper.mapToShortDto(event);
                    dto.setViews(viewsMap.getOrDefault(dto.getId(), 0L));
                    dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(dto.getId(), 0L));
                    return dto;
                })
                .toList();
    }

    @Override
    public List<EventFullDto> searchForAdmin(EventSearchRequestAdmin param) {
        checkDates(param.getRangeStart(), param.getRangeEnd());
        PageRequest page = PageRequest.of(param.getFrom() / param.getSize(), param.getSize());
        Optional<Predicate> searchCriteriaOpt = getAdminSearchCriteria(param);
        List<Event> events = searchCriteriaOpt.map(predicate -> eventRepository.findAll(predicate, page).getContent())
                .orElseGet(() -> eventRepository.findAll(page).getContent());

        Map<Long, Long> viewsMap = getEventsViews(events);
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequests(events);

        return events.stream()
                .map(event -> {
                    EventFullDto dto = EventMapper.mapToFullDto(event);
                    dto.setViews(viewsMap.getOrDefault(dto.getId(), 0L));
                    dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(dto.getId(), 0L));
                    return dto;
                })
                .toList();
    }

    @Override
    public List<EventShortDto> searchForUser(EventSearchRequestUser param, HttpServletRequest request) {
        checkDates(param.getRangeStart(), param.getRangeEnd());

        PageRequest page;
        if (getUserSearchSort(param).isEmpty()) {
            page = PageRequest.of(param.getFrom() / param.getSize(), param.getSize());
        } else {
            page = PageRequest.of(param.getFrom() / param.getSize(), param.getSize(), getUserSearchSort(param).get());
        }

        Predicate searchCriteria = getUserSearchCriteria(param);
        List<Event> events = eventRepository.findAll(searchCriteria, page).getContent();
        Map<Long, Long> viewsMap = getEventsViews(events);
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequests(events);

        statsClient.hit(new NewEndpointHitDto(APP_NAME, request.getRequestURI(),
                request.getRemoteAddr(), LocalDateTime.now().format(DATE_TIME_FORMATTER)));

        List<EventShortDto> dtos = new ArrayList<>();
        for (Event event : events) {
            long requestsCount = confirmedRequestsMap.getOrDefault(event.getId(), 0L);
            if (param.getOnlyAvailable() && event.getParticipantLimit() > 0 && requestsCount == event.getParticipantLimit()) {
                continue;
            }
            EventShortDto dto = EventMapper.mapToShortDto(event);
            dto.setConfirmedRequests(requestsCount);
            dto.setViews(viewsMap.getOrDefault(dto.getId(), 0L));
            dtos.add(dto);
        }
        return dtos;
    }

    private Optional<Sort> getUserSearchSort(EventSearchRequestUser req) {
        if (req.getSort() == null) {
            return Optional.empty();
        }
        EventUserSort userSort = EventUserSort.fromString(req.getSort());
        String sortColumn = switch (userSort) {
            case EVENT_DATE -> "eventDate";
            case VIEWS -> "views";
        };
        return Optional.of(Sort.by(Sort.Direction.DESC, sortColumn));
    }

    private Predicate getUserSearchCriteria(EventSearchRequestUser req) {
        QEvent event = QEvent.event;
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(event.state.eq(EventState.PUBLISHED));

        if (req.getText() != null && !req.getText().isBlank()) {
            booleanBuilder.and(event.annotation.containsIgnoreCase(req.getText())
                    .or(event.description.containsIgnoreCase(req.getText())));
        }
        if (req.getCategories() != null && !req.getCategories().isEmpty()) {
            booleanBuilder.and(event.category.id.in(req.getCategories()));
        }
        if (req.getRangeStart() == null && req.getRangeEnd() == null) {
            booleanBuilder.and(event.eventDate.goe(LocalDateTime.now()));
        } else {
            if (req.getRangeStart() != null) {
                booleanBuilder.and(event.eventDate.goe(req.getRangeStart()));
            }
            if (req.getRangeEnd() != null) {
                booleanBuilder.and(event.eventDate.loe(req.getRangeEnd()));
            }
        }
        if (req.getPaid() != null) {
            booleanBuilder.and(event.paid.eq(req.getPaid()));
        }

        return booleanBuilder.getValue();
    }

    private Optional<Predicate> getAdminSearchCriteria(EventSearchRequestAdmin req) {
        QEvent event = QEvent.event;
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (req.getUsers() != null && !req.getUsers().isEmpty()) {
            booleanBuilder.and(event.initiator.id.in(req.getUsers()));
        }
        if (req.getStates() != null && !req.getStates().isEmpty()) {
            booleanBuilder.and(event.state.in(req.getStates()));
        }
        if (req.getCategories() != null && !req.getCategories().isEmpty()) {
            booleanBuilder.and(event.category.id.in(req.getCategories()));
        }
        if (req.getRangeStart() != null) {
            booleanBuilder.and(event.eventDate.goe(req.getRangeStart()));
        }
        if (req.getRangeEnd() != null) {
            booleanBuilder.and(event.eventDate.loe(req.getRangeEnd()));
        }
        return Optional.ofNullable(booleanBuilder.getValue());
    }

    private void checkDates(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания.");
        }
    }

    private Long getEventViews(Event event) {
        if (event.getPublishedOn() == null) {
            return 0L;
        }

        LocalDateTime start = event.getPublishedOn();
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = List.of("/events/" + event.getId());

        return statsClient.getStats(start, end, uris, true)
                .stream()
                .findFirst()
                .map(stats -> stats.getHits() != null ? stats.getHits() : 0L)
                .orElse(0L);
    }

    private Map<Long, Long> getEventsViews(List<Event> events) {
        List<String> uris = new ArrayList<>();
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start;
        for (Event event : events) {
            uris.add("/events/" + event.getId());
            if (event.getPublishedOn() != null && start.isAfter(event.getPublishedOn())) {
                start = event.getPublishedOn();
            }
        }
        if (start.isEqual(end)) {
            return Collections.emptyMap();
        }

        return statsClient.getStats(start, end, uris, true)
                .stream()
                .collect(Collectors.toMap(
                        stats -> extractEventFromUri(stats.getUri()),
                        stats -> stats.getHits() != null ? stats.getHits() : 0L,
                        (existing, replacement) -> existing)
                );
    }

    private Long extractEventFromUri(String uri) {
        try {
            String[] parts = uri.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            throw new ValidationException("Неверный формат URI " + uri);
        }
    }

    Map<Long, Long> getConfirmedRequests(List<Event> events) {
        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());
        return requestRepository.getConfirmedRequestsCount(eventIds, ParticipationRequestStatus.CONFIRMED).stream()
                .collect(Collectors.toMap(object -> (Long) object[0], object -> (Long) object[1]));
    }
}
