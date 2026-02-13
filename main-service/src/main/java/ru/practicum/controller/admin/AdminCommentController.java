package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentDtoAdmin;
import ru.practicum.dto.CommentSearchRequestAdmin;
import ru.practicum.dto.CommentStatusChangeRequest;
import ru.practicum.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/comments")
public class AdminCommentController {
    private final CommentService commentService;

    @PostMapping("/search")
    public List<CommentDtoAdmin> getComments(@Valid @RequestBody CommentSearchRequestAdmin param) {
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
