package ru.practicum.validation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;

@Getter
public class ErrorResponse {
    private final String error;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Violation> violations;

    public ErrorResponse(String error) {
        this.error = error;
    }

    public ErrorResponse(String error, List<Violation> violations) {
        this.error = error;
        this.violations = violations;
    }
}
