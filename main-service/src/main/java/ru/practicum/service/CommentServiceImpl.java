package ru.practicum.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.*;
import ru.practicum.exception.ConditionsConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.*;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.util.EventUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;


    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto commentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));

        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие с id: " + eventId + " не найдено или не опубликовано"));

        Comment comment = commentRepository.save(commentMapper.mapToComment(commentDto, user, event));
        return commentMapper.mapToCommentDto(comment);
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
        EventUtils.checkDates(param.getRangeStart(), param.getRangeEnd());
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
        if (status != CommentStatus.CONFIRMED && status != CommentStatus.REJECTED) {
            throw new ConditionsConflictException("Комментарий можно перевести в CONFIRMED или REJECTED. Передан статус " + status);
        }
        commentRepository.updateStatus(status, dto.getCommentIds());
        return commentRepository.findAllByIdIn(dto.getCommentIds()).stream()
                .map(commentMapper::mapToCommentDtoAdmin)
                .toList();
    }

    @Override
    @Transactional
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
    @Transactional
    public void deleteCommentByUser(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id: " + commentId + " не найден"));
        checkUserIsCommentAuthor(userId, comment);
        commentRepository.deleteById(commentId);
    }

    private void checkUserIsCommentAuthor(Long userId, Comment comment) {
        if (!Objects.equals(comment.getAuthor().getId(), userId)) {
            throw new ConditionsConflictException("Пользователь с id=" + userId + " не является автором комментария id=" + comment.getId());
        }
    }

    private Optional<Predicate> getAdminCommentSearchCriteria(CommentSearchRequestAdmin req) {
        QComment comment = QComment.comment;
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        Optional.ofNullable(req.getText()).filter(text -> !text.isBlank())
                .ifPresent(text -> booleanBuilder.and(comment.text.containsIgnoreCase(text)));
        Optional.ofNullable(req.getEventIds()).filter(eventIds -> !eventIds.isEmpty())
                .ifPresent(eventIds -> booleanBuilder.and(comment.event.id.in(eventIds)));
        Optional.ofNullable(req.getUserId()).ifPresent(userId -> booleanBuilder.and(comment.author.id.eq(userId)));
        Optional.ofNullable(req.getRangeStart()).ifPresent(start -> booleanBuilder.and(comment.created.goe(start)));
        Optional.ofNullable(req.getRangeEnd()).ifPresent(end -> booleanBuilder.and(comment.created.loe(end)));
        Optional.ofNullable(req.getStatusList()).ifPresent(statusList -> booleanBuilder.and(comment.status.in(statusList)));

        return Optional.ofNullable(booleanBuilder.getValue());
    }
}
