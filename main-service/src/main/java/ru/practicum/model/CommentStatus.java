package ru.practicum.model;

import ru.practicum.exception.ValidationException;

public enum CommentStatus {
    PENDING,
    CONFIRMED,
    REJECTED;

    public static CommentStatus fromString(String status) {
        CommentStatus targetStatus;
        try {
            targetStatus = CommentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Некорректное значение параметра status: " + status);
        }

        return targetStatus;
    }
}
