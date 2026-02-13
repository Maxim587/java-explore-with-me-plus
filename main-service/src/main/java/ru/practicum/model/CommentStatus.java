package ru.practicum.model;

import ru.practicum.exception.ValidationException;

public enum CommentStatus {
    PENDING,
    CONFIRMED,
    REJECTED;

    public static CommentStatus fromString(String status) {
        try {
            return CommentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Некорректное значение параметра status: " + status);
        }
    }
}
