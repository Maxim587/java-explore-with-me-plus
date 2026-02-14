package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.model.Comment;
import ru.practicum.model.CommentStatus;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CommentRepository extends JpaRepository<Comment, Long>, QuerydslPredicateExecutor<Comment> {

    @Modifying
    @Query("update Comment c set c.status = :status where c.id in :ids")
    void updateStatus(CommentStatus status, Set<Long> ids);

    List<Comment> findAllByIdIn(Collection<Long> ids);

    List<Comment> findAllByEventIdIn(Collection<Long> ids);

    List<Comment> findByEventId(Long id);
}
