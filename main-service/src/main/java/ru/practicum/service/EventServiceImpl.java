package ru.practicum.service;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.dto.*;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequestStatus;
import ru.practicum.model.User;
import ru.practicum.repository.*;
import ru.practicum.util.EventUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository requestRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final StatsClient statsClient;
    private final LocationMapper locationMapper;

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id = " + newEventDto.getCategory() + " не найдена"));

        LocalDateTime eventDate = LocalDateTime.parse(newEventDto.getEventDate(), DATE_TIME_FORMATTER);
        EventUtils.checkEventDateIsValid(eventDate);
        Event savedEvent = eventRepository.save(EventMapper.mapToEvent(newEventDto, user, category, eventDate));
        EventFullDto dto = EventMapper.mapToFullDto(savedEvent);
        dto.setViews(0L);
        dto.setConfirmedRequests(0L);
        return dto;
    }

    @Override
    @Transactional
    public EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));
        Optional.ofNullable(request.getCategory()).ifPresent(id -> event.setCategory(getCategory(id)));
        EventUtils.updateEventFieldsFromUserRequest(request, event);
        EventFullDto dto = EventMapper.mapToFullDto(eventRepository.save(event));
        setEventFullDtoFields(dto, event);

        return dto;
    }

    @Override
    @Transactional
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = getEvent(eventId);
        Optional.ofNullable(request.getCategory()).ifPresent(id -> event.setCategory(getCategory(id)));
        Optional.ofNullable(request.getLocation()).ifPresent(loc -> event.setLocation(locationMapper.mapLocationToEventLocation(loc)));
        EventUtils.updateEventFieldsFromAdminRequest(request, event);
        EventFullDto dto = EventMapper.mapToFullDto(eventRepository.save(event));
        setEventFullDtoFields(dto, event);

        return dto;
    }

    @Override
    public EventFullDto getByUser(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));
        EventFullDto dto = EventMapper.mapToFullDto(event);
        setEventFullDtoFields(dto, event);

        return dto;
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId) {
        Event event = getEvent(eventId);
        EventUtils.checkEventIsPublished(event);
        EventFullDto dto = EventMapper.mapToFullDto(event);
        setEventFullDtoFields(dto, event);

        return dto;
    }

    @Override
    public List<EventShortDto> getAllByUser(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable).getContent();
        Map<Long, Long> viewsMap = getEventsViews(events);
        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequests(eventIds);
        Map<Long, List<CommentDto>> commentsMap = getCommentsMap(eventIds);

        return events.stream()
                .map(event -> {
                    EventShortDto dto = EventMapper.mapToShortDto(event);
                    dto.setViews(viewsMap.getOrDefault(dto.getId(), 0L));
                    dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(dto.getId(), 0L));
                    dto.setComments(commentsMap.getOrDefault(dto.getId(), Collections.emptyList()));
                    return dto;
                })
                .toList();
    }

    @Override
    public List<EventFullDto> searchForAdmin(EventSearchRequestAdmin param) {
        EventUtils.checkDates(param.getRangeStart(), param.getRangeEnd());
        PageRequest page = PageRequest.of(param.getFrom() / param.getSize(), param.getSize());
        Optional<Predicate> searchCriteriaOpt = EventUtils.getAdminSearchCriteria(param);
        List<Event> events = searchCriteriaOpt.map(predicate -> eventRepository.findAll(predicate, page).getContent())
                .orElseGet(() -> eventRepository.findAll(page).getContent());
        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequests(eventIds);
        Map<Long, Long> viewsMap = getEventsViews(events);
        Map<Long, List<CommentDto>> commentsMap = getCommentsMap(eventIds);

        return events.stream()
                .map(event -> {
                    EventFullDto dto = EventMapper.mapToFullDto(event);
                    dto.setViews(viewsMap.getOrDefault(dto.getId(), 0L));
                    dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(dto.getId(), 0L));
                    dto.setComments(commentsMap.getOrDefault(dto.getId(), Collections.emptyList()));
                    return dto;
                })
                .toList();
    }

    @Override
    public List<EventShortDto> searchForUser(EventSearchRequestUser param) {
        EventUtils.checkDates(param.getRangeStart(), param.getRangeEnd());
        PageRequest page = EventUtils.getUserSearchPage(param);
        Predicate searchCriteria = EventUtils.getUserSearchCriteria(param);
        List<Event> events = eventRepository.findAll(searchCriteria, page).getContent();
        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());
        Map<Long, Long> viewsMap = getEventsViews(events);
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequests(eventIds);
        Map<Long, List<CommentDto>> commentsMap = getCommentsMap(eventIds);

        List<EventShortDto> dtos = new ArrayList<>();
        for (Event event : events) {
            long requestsCount = confirmedRequestsMap.getOrDefault(event.getId(), 0L);
            if (param.getOnlyAvailable() && event.getParticipantLimit() > 0 && requestsCount == event.getParticipantLimit()) {
                continue;
            }
            EventShortDto dto = EventMapper.mapToShortDto(event);
            dto.setConfirmedRequests(requestsCount);
            dto.setViews(viewsMap.getOrDefault(dto.getId(), 0L));
            dto.setComments(commentsMap.getOrDefault(dto.getId(), Collections.emptyList()));
            dtos.add(dto);
        }
        return dtos;
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));
    }

    private Map<Long, List<CommentDto>> getCommentsMap(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, List<CommentDto>> commentsMap = new HashMap<>();
        commentRepository.findAllByEventIdIn(eventIds).forEach(comment -> {
            commentsMap.computeIfAbsent(comment.getEvent().getId(), eventId -> new ArrayList<>())
                    .add(commentMapper.mapToCommentDto(comment));
        });
        return commentsMap;
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

        List<ViewStatsDto> viewDtos = statsClient.getStats(start, end, uris, true);
        Map<Long, Long> viewsMap = new HashMap<>();
        for (ViewStatsDto view : viewDtos) {
            String[] parts = view.getUri().split("/");
            if (parts.length == 3) {
                Long eventId = Long.parseLong(parts[parts.length - 1]);
                viewsMap.put(eventId, view.getHits());
            }
        }
        return viewsMap;
    }

    private void setEventFullDtoFields(EventFullDto dto, Event event) {
        dto.setViews(getEventViews(event));
        dto.setConfirmedRequests((long) requestRepository.countByEventIdAndStatus(event.getId(), ParticipationRequestStatus.CONFIRMED));
        dto.setComments(getCommentsDtoList(event.getId()));
    }

    private List<CommentDto> getCommentsDtoList(Long eventId) {
        return commentRepository.findByEventId(eventId).stream()
                .map(commentMapper::mapToCommentDto)
                .toList();
    }

    private Map<Long, Long> getConfirmedRequests(Set<Long> eventIds) {
        return requestRepository.getConfirmedRequestsCount(eventIds, ParticipationRequestStatus.CONFIRMED).stream()
                .collect(Collectors.toMap(object -> (Long) object[0], object -> (Long) object[1]));
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория с id " + id + " не найдена"));
    }
}
