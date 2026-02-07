package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewCategoryDto {
    @NotBlank(message = "Значение не должно быть пустым")
    @Size(min = 1, max = 50, message = "Значение должно содержать от 1 до 50 символов")
    private String name;
}
