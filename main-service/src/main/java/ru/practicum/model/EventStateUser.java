package ru.practicum.model;

import ru.practicum.exception.ValidationException;

public enum EventStateUser {
    SEND_TO_REVIEW,
    CANCEL_REVIEW;

    public static EventStateUser fromString(String eventStateUser) {
        EventStateUser state;
        try {
            state = EventStateUser.valueOf(eventStateUser.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Параметр EventStateUser может принимать значения SEND_TO_REVIEW или CANCEL_REVIEW. Передан " + eventStateUser);
        }
        return state;
    }
}
