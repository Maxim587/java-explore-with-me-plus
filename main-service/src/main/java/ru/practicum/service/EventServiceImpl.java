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
import ru.practicum.mapper.EventMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


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

        return dto;
    }

    @Override
    @Transactional
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = getEvent(eventId);
        eventUtils.updateEventFieldsFromAdminRequest(request, event);
        EventFullDto dto = EventMapper.mapToFullDto(eventRepository.save(event));
        eventUtils.setEventFullDtoFields(dto, event);

        return dto;
    }

    @Override
    public EventFullDto getByUser(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));
        EventFullDto dto = EventMapper.mapToFullDto(event);
        eventUtils.setEventFullDtoFields(dto, event);

        return dto;
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId) {
        Event event = getEvent(eventId);
        eventUtils.checkEventIsPublished(event);
        EventFullDto dto = EventMapper.mapToFullDto(event);
        eventUtils.setEventFullDtoFields(dto, event);

        return dto;
    }

    @Override
    public List<EventShortDto> getAllByUser(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable).getContent();
        Map<Long, Long> viewsMap = eventUtils.getEventsViews(events);
        Map<Long, Long> confirmedRequestsMap = eventUtils.getConfirmedRequests(events);

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
        eventUtils.checkDates(param.getRangeStart(), param.getRangeEnd());
        PageRequest page = PageRequest.of(param.getFrom() / param.getSize(), param.getSize());
        Optional<Predicate> searchCriteriaOpt = eventUtils.getAdminSearchCriteria(param);
        List<Event> events = searchCriteriaOpt.map(predicate -> eventRepository.findAll(predicate, page).getContent())
                .orElseGet(() -> eventRepository.findAll(page).getContent());

        Map<Long, Long> confirmedRequestsMap = eventUtils.getConfirmedRequests(events);
        Map<Long, Long> viewsMap = eventUtils.getEventsViews(events);

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
    public List<EventShortDto> searchForUser(EventSearchRequestUser param) {
        eventUtils.checkDates(param.getRangeStart(), param.getRangeEnd());
        PageRequest page = eventUtils.getUserSearchPage(param);
        Predicate searchCriteria = eventUtils.getUserSearchCriteria(param);
        List<Event> events = eventRepository.findAll(searchCriteria, page).getContent();
        Map<Long, Long> viewsMap = eventUtils.getEventsViews(events);
        Map<Long, Long> confirmedRequestsMap = eventUtils.getConfirmedRequests(events);

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

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));
    }
}
