package ru.practicum.service;

import ru.practicum.dto.*;

import java.util.List;

public interface CommentService {

    CommentDto createComment(Long userId, Long eventId, NewCommentDto commentDto);

    CommentDto updateComment(Long userId, Long commentId, NewCommentDto commentDto);

    List<CommentDtoAdmin> searchCommentsByAdmin(CommentSearchRequestAdmin param);

    List<CommentDtoAdmin> changeCommentStatus(CommentStatusChangeRequest dto);

    void deleteCommentByAdmin(Long commentId);

    CommentDtoAdmin getCommentById(Long commentId);

    void deleteCommentByUser(Long userId, Long commentId);
}
