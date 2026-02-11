package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentDtoAdmin;
import ru.practicum.dto.CommentSearchRequestAdmin;
import ru.practicum.dto.CommentStatusChangeRequest;
import ru.practicum.model.CommentStatus;
import ru.practicum.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/comments")
public class AdminCommentController {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDtoAdmin> getComments(@RequestParam(required = false) String text,
                                             @RequestParam(required = false) List<Long> eventIds,
                                             @RequestParam(required = false) Long userId,
                                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                             @RequestParam(required = false) List<CommentStatus> statusList,
                                             @RequestParam(defaultValue = "0") Integer from,
                                             @RequestParam(defaultValue = "10") Integer size) {
        CommentSearchRequestAdmin param = new CommentSearchRequestAdmin(text, eventIds, userId, rangeStart, rangeEnd, statusList, from, size);
        log.info("Создан запрос на поиск комментариев администратором с параметрами param={}", param);
        return commentService.searchCommentsByAdmin(param);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDtoAdmin> changeCommentStatus(@Valid @RequestBody CommentStatusChangeRequest dto) {
        log.info("Создан запрос на изменение статусов комментариев dto={}", dto);
        return commentService.changeCommentStatus(dto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(@PathVariable Long commentId) {
        log.info("Создан запрос на удаление комментария администратором, commentId={}", commentId);
        commentService.deleteCommentByAdmin(commentId);
    }

    @GetMapping("/{commentId}")
    public CommentDtoAdmin getCommentById(@PathVariable Long commentId) {
        log.info("Создан запрос на получение комментария администратором, commentId={}", commentId);
        return commentService.getCommentById(commentId);
    }

}
