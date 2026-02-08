package ru.practicum.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventShortDto {
    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;
    private String eventDate;
    private Long id;
    private UserShortDto initiator;
    private Boolean paid;
    private String title;
    private Long views;
    private List<CommentDto> comments = new ArrayList<>();
}
