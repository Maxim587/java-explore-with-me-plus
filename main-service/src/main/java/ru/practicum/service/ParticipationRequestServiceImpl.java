package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.exception.ConditionsConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final ParticipationRequestRepository requestRepository;
    private final ParticipationRequestMapper mapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        Event event = getEvent(eventId);

        ParticipationRequest request = createParticipationRequest(userId, event);

        if (!event.getRequestModeration()) {
            request.setStatus(ParticipationRequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() == null ? 1 : event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        return mapper.mapToParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        return requestRepository.findAllByRequester_Id(userId).stream()
                .map(mapper::mapToParticipationRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.getByIdAndRequester_Id(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId
                                                         + " для пользователя с id " + userId + " не найден"));

        if (request.getStatus() == ParticipationRequestStatus.REJECTED ||
            request.getStatus() == ParticipationRequestStatus.CANCELLED) {
            throw new ConditionsConflictException("Заявка находится в статусе " + request.getStatus() + ". Отмена заявки невозможна");
        }

        if (request.getStatus() == ParticipationRequestStatus.CONFIRMED) {
            Event event = request.getEvent();
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
        }

        request.setStatus(ParticipationRequestStatus.CANCELLED);
        return mapper.mapToParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest dto) {
        Event event = getEvent(eventId);
        checkRequesterIsEventInitiator(userId, event);
        ParticipationRequestStatus status = getStatusFromString(dto.getStatus());

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            return new EventRequestStatusUpdateResult(Collections.emptyList(), Collections.emptyList());
        }

        long confirmedRequests = event.getConfirmedRequests() == null ? 0 : event.getConfirmedRequests();
        int possibleToConfirmCount = event.getParticipantLimit() - (int) confirmedRequests;

        if (status == ParticipationRequestStatus.CONFIRMED && possibleToConfirmCount == 0) {
            throw new ConditionsConflictException("Достигнут лимит на участие у события");
        }

        List<ParticipationRequest> requests = requestRepository.findAllByIdIn(dto.getRequestIds());
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        if (status == ParticipationRequestStatus.CONFIRMED) {
            for (int i = 0; i < requests.size(); i++) {
                ParticipationRequest request = requests.get(i);
                checkStatus(request);
                if (i + 1 <= possibleToConfirmCount) {
                    request.setStatus(ParticipationRequestStatus.CONFIRMED);
                    confirmed.add(mapper.mapToParticipationRequestDto(request));
                } else {
                    request.setStatus(ParticipationRequestStatus.REJECTED);
                    rejected.add(mapper.mapToParticipationRequestDto(request));
                }
            }
            event.setConfirmedRequests(confirmedRequests + confirmed.size());
            eventRepository.save(event);
        } else {
            for (ParticipationRequest request : requests) {
                checkStatus(request);
                request.setStatus(ParticipationRequestStatus.REJECTED);
                rejected.add(mapper.mapToParticipationRequestDto(request));
            }
        }

        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        Event event = getEvent(eventId);
        checkRequesterIsEventInitiator(userId, event);

        return requestRepository.findAllByEvent_Id(eventId).stream()
                .map(mapper::mapToParticipationRequestDto)
                .toList();
    }


    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));
    }

    private void checkStatus(ParticipationRequest request) {
        if (request.getStatus() != ParticipationRequestStatus.PENDING) {
            throw new ConditionsConflictException("Заявки из списка должны иметь статус PENDING");
        }
    }

    private void checkRequesterIsEventInitiator(Long userId, Event event) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConditionsConflictException("Пользователь с id " + userId + " не является инициатором события " + event.getId());
        }
    }

    private ParticipationRequest createParticipationRequest(Long userId, Event event) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        validateRequest(event, user);
        ParticipationRequest request = new ParticipationRequest();
        request.setRequester(user);
        request.setEvent(event);

        return request;
    }

    private void validateRequest(Event event, User user) {
        if (event.getInitiator().getId().equals(user.getId())) {
            throw new ConditionsConflictException("Пользователь с id " + user.getId() +
                                                  " не может создавать заявку на участие в событии с id " + event.getId() +
                                                  " т.к. является его инициатором");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConditionsConflictException("Нельзя участвовать в неопубликованном событии");
        }

        long confirmedRequests = event.getConfirmedRequests() == null ? 0 : event.getConfirmedRequests();

        if (event.getParticipantLimit() > 0 && event.getParticipantLimit().longValue() == confirmedRequests) {
            throw new ConditionsConflictException("Достигнут лимит на участие у события");
        }
    }

    private ParticipationRequestStatus getStatusFromString(String status) {
        ParticipationRequestStatus targetStatus;
        try {
            targetStatus = ParticipationRequestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Некорректное значение параметра status: " + status);
        }

        if (!(targetStatus.equals(ParticipationRequestStatus.CONFIRMED) ||
              targetStatus.equals(ParticipationRequestStatus.REJECTED))) {
            throw new ConditionsConflictException("Новый статус для заявок может принимать значения CONFIRMED или REJECTED. Передан " + status);
        }
        return targetStatus;
    }
}
