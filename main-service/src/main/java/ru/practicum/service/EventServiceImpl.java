package ru.practicum.service;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.*;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.*;
import ru.practicum.repository.*;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;
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
    private final EventUtils eventUtils;
    private final ParticipationRequestRepository requestRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id = " + newEventDto.getCategory() + " не найдена"));

        LocalDateTime eventDate = LocalDateTime.parse(newEventDto.getEventDate(), DATE_TIME_FORMATTER);
        eventUtils.checkEventDateIsValid(eventDate);
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
        eventUtils.updateEventFieldsFromUserRequest(request, event);
        EventFullDto dto = EventMapper.mapToFullDto(eventRepository.save(event));
        eventUtils.setEventFullDtoFields(dto, event);
        List<CommentDto> comments = commentRepository.findByEventId(eventId).stream()
                .map(commentMapper::mapToCommentDto)
                .toList();
        dto.setComments(comments);

        return dto;
    }

    @Override
    @Transactional
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = getEvent(eventId);
        eventUtils.updateEventFieldsFromAdminRequest(request, event);
        EventFullDto dto = EventMapper.mapToFullDto(eventRepository.save(event));
        eventUtils.setEventFullDtoFields(dto, event);
        List<CommentDto> comments = commentRepository.findByEventId(eventId).stream()
                .map(commentMapper::mapToCommentDto)
                .toList();
        dto.setComments(comments);

        return dto;
    }

    @Override
    public EventFullDto getByUser(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));
        EventFullDto dto = EventMapper.mapToFullDto(event);
        eventUtils.setEventFullDtoFields(dto, event);
        List<CommentDto> comments = commentRepository.findByEventId(eventId).stream()
                .map(commentMapper::mapToCommentDto)
                .toList();
        dto.setComments(comments);

        return dto;
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId) {
        Event event = getEvent(eventId);
        eventUtils.checkEventIsPublished(event);
        EventFullDto dto = EventMapper.mapToFullDto(event);
        eventUtils.setEventFullDtoFields(dto, event);

        List<CommentDto> comments = commentRepository.findByEventId(eventId).stream()
                .map(commentMapper::mapToCommentDto)
                .toList();
        dto.setComments(comments);
        return dto;
    }

    @Override
    public List<EventShortDto> getAllByUser(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable).getContent();
        Map<Long, Long> viewsMap = eventUtils.getEventsViews(events);
        Map<Long, Long> confirmedRequestsMap = eventUtils.getConfirmedRequests(events);
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
        eventUtils.checkDates(param.getRangeStart(), param.getRangeEnd());
        PageRequest page = PageRequest.of(param.getFrom() / param.getSize(), param.getSize());
        Optional<Predicate> searchCriteriaOpt = eventUtils.getAdminSearchCriteria(param);
        List<Event> events = searchCriteriaOpt.map(predicate -> eventRepository.findAll(predicate, page).getContent())
                .orElseGet(() -> eventRepository.findAll(page).getContent());

        Map<Long, Long> confirmedRequestsMap = eventUtils.getConfirmedRequests(events);
        Map<Long, Long> viewsMap = eventUtils.getEventsViews(events);
        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());
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
        eventUtils.checkDates(param.getRangeStart(), param.getRangeEnd());
        PageRequest page = eventUtils.getUserSearchPage(param);
        Predicate searchCriteria = eventUtils.getUserSearchCriteria(param);
        List<Event> events = eventRepository.findAll(searchCriteria, page).getContent();
        Map<Long, Long> viewsMap = eventUtils.getEventsViews(events);
        Map<Long, Long> confirmedRequestsMap = eventUtils.getConfirmedRequests(events);

        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());

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

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto commentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));

        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие с id: " + eventId + " не найдено или не опубликовано"));

        return commentMapper.mapToCommentDto(commentRepository.save(commentMapper.mapToComment(commentDto, user, event)));
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto commentDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id: " + commentId + " не найден"));
        checkUserIsCommentAuthor(userId, comment);
        comment.setText(commentDto.getText());
        comment.setStatus(CommentStatus.PENDING);

        return commentMapper.mapToCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDtoAdmin> searchCommentsByAdmin(CommentSearchRequestAdmin param) {
        checkDates(param.getRangeStart().orElse(null), param.getRangeEnd().orElse(null));
        Optional<Predicate> searchCriteriaOpt = getAdminCommentSearchCriteria(param);
        PageRequest page = PageRequest.of(param.getFrom() / param.getSize(), param.getSize());

        return searchCriteriaOpt.map(predicate -> commentRepository.findAll(predicate, page).getContent())
                .orElseGet(() -> commentRepository.findAll(page).getContent())
                .stream()
                .map(commentMapper::mapToCommentDtoAdmin)
                .toList();
    }

    @Override
    @Transactional
    public List<CommentDtoAdmin> changeCommentStatus(CommentStatusChangeRequest dto) {
        CommentStatus status = CommentStatus.fromString(dto.getStatus());
        if (!(status == CommentStatus.CONFIRMED || status == CommentStatus.REJECTED)) {
            throw new ConditionsConflictException("Запрос можно перевести в CONFIRMED или REJECTED. Передан статус " + status);
        }
        commentRepository.updateStatus(status, dto.getCommentIds());
        return commentRepository.findAllByIdIn(dto.getCommentIds()).stream()
                .map(commentMapper::mapToCommentDtoAdmin)
                .toList();
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Override
    public CommentDtoAdmin getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .map(commentMapper::mapToCommentDtoAdmin)
                .orElseThrow(() -> new NotFoundException("Комментарий с id: " + commentId + " не найден"));
    }

    @Override
    public void deleteCommentByUser(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id: " + commentId + " не найден"));
        checkUserIsCommentAuthor(userId, comment);
        commentRepository.deleteById(commentId);
    }


    private Optional<Predicate> getAdminCommentSearchCriteria(CommentSearchRequestAdmin req) {
        QComment comment = QComment.comment;
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        req.getText().filter(text -> !text.isBlank())
                .ifPresent(text -> booleanBuilder.and(comment.text.containsIgnoreCase(text)));
        req.getEventIds().filter(eventIds -> !eventIds.isEmpty())
                .ifPresent(eventIds -> booleanBuilder.and(comment.event.id.in(eventIds)));
        req.getUserId().ifPresent(userId -> booleanBuilder.and(comment.author.id.eq(userId)));
        req.getRangeStart().ifPresent(start -> booleanBuilder.and(comment.created.goe(start)));
        req.getRangeEnd().ifPresent(end -> booleanBuilder.and(comment.created.loe(end)));
        req.getStatusList().ifPresent(statusList -> booleanBuilder.and(comment.status.in(statusList)));

        return Optional.ofNullable(booleanBuilder.getValue());
    }

    private void checkUserIsCommentAuthor(Long userId, Comment comment) {
        if (!Objects.equals(comment.getAuthor().getId(), userId)) {
            throw new ConditionsConflictException("Пользователь с id=" + userId + " не является автором комментария id=" + comment.getId());
        }
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
}
