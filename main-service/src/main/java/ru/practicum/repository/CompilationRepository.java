package ru.practicum.repository;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @EntityGraph(attributePaths = {"events.category", "events.initiator", "events.location"})
    List<Compilation> findAllByPinned(Boolean pinned, Pageable pageable);


    @EntityGraph(attributePaths = {"events.category", "events.initiator", "events.location"})
    @Query("select c from Compilation c")
    List<Compilation> getCompilationList(Pageable pageable);
}
