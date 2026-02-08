package ru.practicum.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.model.CommentStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CommentDto {
    private Long id;
    private LocalDateTime created;
    private String text;
    private String authorName;
    private Long eventId;
    private CommentStatus status;
}
