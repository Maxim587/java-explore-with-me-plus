package ru.practicum;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewEndpointHitDto {

    @NotBlank(message = "Значение не должно быть пустым")
    private String app;

    @NotBlank(message = "Значение не должно быть пустым")
    private String uri;

    @NotBlank(message = "Значение не должно быть пустым")
    private String ip;

    @NotBlank(message = "Значение не должно быть пустым")
    private String timestamp;
}
