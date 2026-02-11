package ru.practicum.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.practicum.model.CommentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class CommentSearchRequestAdmin {
    private String text;
    private List<Long> eventIds;
    private Long userId;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private List<CommentStatus> statusList;
    private Integer from;
    private Integer size;
}
