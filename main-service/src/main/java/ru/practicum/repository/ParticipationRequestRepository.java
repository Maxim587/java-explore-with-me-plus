package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.ParticipationRequestStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    Optional<ParticipationRequest> getByIdAndRequester_Id(Long requestId, Long userId);

    List<ParticipationRequest> findAllByRequester_Id(Long userId);

    List<ParticipationRequest> findAllByIdIn(Collection<Long> ids);

    List<ParticipationRequest> findAllByEvent_Id(Long eventId);

    int countByEventIdAndStatus(Long eventId, ParticipationRequestStatus requestStatus);

    @Modifying
    @Query("update ParticipationRequest r set r.status = :status where r.id in :ids")
    void updateStatus(ParticipationRequestStatus status, List<Long> ids);

    @Query("select r.event.id, count(r.id) from ParticipationRequest r where r.status = :status and r.event.id in :ids group by r.event.id")
    List<Object[]> getConfirmedRequestsCount(Set<Long> ids, ParticipationRequestStatus status);

}
