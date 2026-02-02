package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    Page<Event> findByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndState(Long eventId, EventState state);

    List<Event> findEventsByIdIn(Collection<Long> ids);

    List<Event> findEventsByCategoryId(Long categoryId);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByIdAndInitiatorId(Long eventId, Long userId);

}
