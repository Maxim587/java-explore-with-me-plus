package ru.practicum.controller.authorized;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.service.CommentService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable Long userId,
                                 @RequestParam Long eventId,
                                 @Valid @RequestBody NewCommentDto newDto) {
        log.info("Пользователь отправил запрос на создание комментария userId={}, eventId={}, newDto={}", userId, eventId, newDto);
        return commentService.createComment(userId, eventId, newDto);
    }

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@PathVariable Long userId,
                                    @PathVariable Long commentId,
                                    @Valid @RequestBody NewCommentDto newDto) {
        log.info("Пользователь отправил запрос на изменение комментария userId={}, commentId={}, newDto={}", userId, commentId, newDto);
        return commentService.updateComment(userId, commentId, newDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByUser(@PathVariable Long userId,
                                    @PathVariable Long commentId) {
        log.info("Создан запрос на удаление комментария пользователем, commentId={}, userId={}", commentId, userId);
        commentService.deleteCommentByUser(userId, commentId);
    }
}
