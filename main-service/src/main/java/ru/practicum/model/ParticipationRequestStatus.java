package ru.practicum.model;

import ru.practicum.exception.ValidationException;

public enum ParticipationRequestStatus {
    PENDING,
    CONFIRMED,
    REJECTED,
    CANCELED;

    public static ParticipationRequestStatus fromString(String status) {
        ParticipationRequestStatus targetStatus;
        try {
            targetStatus = ParticipationRequestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Некорректное значение параметра status: " + status);
        }

        return targetStatus;
    }
}
