package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class CommentStatusChangeRequest {
    @NotNull
    private Set<Long> commentIds;

    @NotBlank
    private String status;
}
