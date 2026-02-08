package ru.practicum.service;

import ru.practicum.dto.*;

import java.util.List;

public interface EventService {

    EventFullDto create(Long userId, NewEventDto newEventDto);

    EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest request);

    EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest request);

    EventFullDto getByUser(Long userId, Long eventId);

    EventFullDto getPublicEvent(Long eventId);

    List<EventShortDto> getAllByUser(Long userId, Integer from, Integer size);

    List<EventFullDto> searchForAdmin(EventSearchRequestAdmin param);

    List<EventShortDto> searchForUser(EventSearchRequestUser param);

    CommentDto createComment(Long userId, Long eventId, NewCommentDto commentDto);

    CommentDto updateComment(Long userId, Long commentId, NewCommentDto commentDto);

    List<CommentDtoAdmin> searchCommentsByAdmin(CommentSearchRequestAdmin param);

    List<CommentDtoAdmin> changeCommentStatus(CommentStatusChangeRequest dto);

    void deleteCommentByAdmin(Long commentId);

    CommentDtoAdmin getCommentById(Long commentId);

    void deleteCommentByUser(Long userId, Long commentId);
}
