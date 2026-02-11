package ru.practicum.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class EventSearchRequestAdmin {
    private List<Long> users;
    private List<EventState> states;
    private List<Long> categories;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Integer from;
    private Integer size;
}
