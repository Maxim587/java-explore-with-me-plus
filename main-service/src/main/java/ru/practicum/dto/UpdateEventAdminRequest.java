package ru.practicum.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000, message = "Значение должно содержать от 20 до 2000 символов")
    private String annotation;

    @PositiveOrZero(message = "Значение должно быть больше или равно 0")
    private Long category;

    @Size(min = 20, max = 7000, message = "Значение должно содержать от 20 до 7000 символов")
    private String description;

    private String eventDate;

    private Location location;

    private Boolean paid;

    @PositiveOrZero(message = "Значение должно быть больше или равно 0")
    private Integer participantLimit;

    private Boolean requestModeration;

    private String stateAction;

    @Size(min = 3, max = 120, message = "Значение должно содержать от 3 до 120 символов")
    private String title;
}
