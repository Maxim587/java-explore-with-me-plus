package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NewCommentDto {
    @NotBlank(message = "Значение не должно быть пустым")
    @Size(min = 1, max = 2000, message = "Значение должно содержать от 1 до 2000 символов")
    private String text;
}
