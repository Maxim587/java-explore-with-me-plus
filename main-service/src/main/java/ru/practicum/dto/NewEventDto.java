package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewEventDto {
    @NotBlank(message = "Значение не должно быть пустым")
    private String annotation;

    @NotNull(message = "Значение не должно быть пустым")
    @PositiveOrZero(message = "Значение должно быть больше или равно 0")
    private Long category;

    @NotBlank(message = "Значение не должно быть пустым")
    @Size(min = 20, max = 7000, message = "Значение должно содержать от 20 до 7000 символов")
    private String description;

    @NotBlank(message = "Значение не должно быть пустым")
    private String eventDate;

    @NotNull(message = "Значение не должно быть пустым")
    private Location location;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;

    @NotBlank(message = "Значение не должно быть пустым")
    @Size(min = 3, max = 120, message = "Значение должно содержать от 3 до 120 символов")
    private String title;
}
