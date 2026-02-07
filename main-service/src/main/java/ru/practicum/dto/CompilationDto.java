package ru.practicum.dto;

import lombok.Data;

import java.util.Set;

@Data
public class CompilationDto {
    private Set<EventShortDto> events;
    private Long id;
    private Boolean pinned;
    private String title;
}
