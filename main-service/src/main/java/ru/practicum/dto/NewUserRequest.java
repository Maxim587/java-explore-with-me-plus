package ru.practicum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewUserRequest {
    @Email(message = "Значение должно иметь формат email")
    @Size(min = 6, max = 254, message = "Значение должно содержать от 6 до 254 символов")
    @NotBlank(message = "Значение не должно быть пустым")
    private String email;

    @Size(min = 2, max = 250, message = "Значение должно содержать от 2 до 250 символов")
    @NotBlank(message = "Значение не должно быть пустым")
    private String name;
}
