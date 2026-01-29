package ru.practicum.dto;

import lombok.Data;

@Data
public class EventShortDto {
    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;
    private String eventDate;
    private Long id;
    private UserDto initiator;
    private Boolean paid;
    private String title;
    private Long views;
}
