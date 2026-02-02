package ru.practicum.exception;

public class ConditionsConflictException extends RuntimeException {
    public ConditionsConflictException(String message) {
        super(message);
    }
}
