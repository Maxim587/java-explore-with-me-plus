package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Event;

import java.util.Collection;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {
    Collection<Event> findAllByIdIn(Set<Long> eventIds);
}
