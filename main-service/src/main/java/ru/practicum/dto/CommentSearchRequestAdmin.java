package ru.practicum.dto;


import lombok.Getter;
import ru.practicum.model.CommentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Getter
public class CommentSearchRequestAdmin {
    private final Optional<String> text;
    private final Optional<List<Long>> eventIds;
    private final Optional<Long> userId;
    private final Optional<LocalDateTime> rangeStart;
    private final Optional<LocalDateTime> rangeEnd;
    private final Optional<List<CommentStatus>> statusList;
    private final Integer from;
    private final Integer size;

    public CommentSearchRequestAdmin(String text,
                                     List<Long> eventIds,
                                     Long userId,
                                     LocalDateTime rangeStart,
                                     LocalDateTime rangeEnd,
                                     List<CommentStatus> statusList,
                                     Integer from,
                                     Integer size) {
        this.text = Optional.ofNullable(text);
        this.eventIds = Optional.ofNullable(eventIds);
        this.userId = Optional.ofNullable(userId);
        this.rangeStart = Optional.ofNullable(rangeStart);
        this.rangeEnd = Optional.ofNullable(rangeEnd);
        this.statusList = Optional.ofNullable(statusList);
        this.from = from;
        this.size = size;
    }
}
