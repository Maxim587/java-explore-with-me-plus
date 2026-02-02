package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.ParticipationRequestStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    Optional<ParticipationRequest> getByIdAndRequester_Id(Long requestId, Long userId);

    List<ParticipationRequest> findAllByRequester_Id(Long userId);

    List<ParticipationRequest> findAllByIdIn(Collection<Long> ids);

    List<ParticipationRequest> findAllByEvent_Id(Long eventId);

    Long countConfirmedRequestsByEventId(Long eventId);

    List<ParticipationRequest> findByEventIdAndStatus(Long eventId, ParticipationRequestStatus requestStatus);
}
