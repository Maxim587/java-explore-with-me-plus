package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Data
public class ApiError {
    private final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    private String message;
    private String reason;
    private String status;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> errors;

    public ApiError(String message, String reason, HttpStatus status) {
        this.message = message;
        this.reason = reason;
        this.status = status.getReasonPhrase();
    }

    public ApiError(String message, String reason, HttpStatus status, StackTraceElement[] stackTrace) {
        this.message = message;
        this.reason = reason;
        this.status = status.getReasonPhrase();
        this.errors = Arrays.stream(stackTrace)
                .map(StackTraceElement::toString)
                .toList();
    }

    public ApiError(String message, String reason, HttpStatus status, List<String> violations) {
        this.message = message;
        this.reason = reason;
        this.status = status.getReasonPhrase();
        this.errors = violations;
    }
}
