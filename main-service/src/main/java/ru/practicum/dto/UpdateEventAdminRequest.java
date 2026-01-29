package ru.practicum.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000, message = "Значение должно содержать от 20 до 2000 символов")
    private String annotation;

    @Positive(message = "Значение должно быть положительным числом")
    private Long category;

    @Size(min = 20, max = 7000, message = "Значение должно содержать от 20 до 7000 символов")
    private String description;

    private String eventDate;

    private Location location;

    private Boolean paid;

    @Positive(message = "Значение должно быть положительным числом")
    private Integer participantLimit;

    private Boolean requestModeration;

    private String stateAction;

    @Size(min = 3, max = 120, message = "Значение должно содержать от 3 до 120 символов")
    private String title;
}
