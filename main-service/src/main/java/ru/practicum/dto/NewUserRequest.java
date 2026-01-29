package ru.practicum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewUserRequest {
    @NotBlank(message = "Значение не должно быть пустым")
    @Email(message = "Значение должно иметь формат email")
    private String email;

    @NotBlank(message = "Значение не должно быть пустым")
    @Size(min = 2, max = 250, message = "Значение должно содержать от 2 до 250 символов")
    private String name;
}
