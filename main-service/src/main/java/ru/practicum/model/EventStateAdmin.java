package ru.practicum.model;

import ru.practicum.exception.ValidationException;

public enum EventStateAdmin {
    PUBLISH_EVENT,
    REJECT_EVENT;

    public static EventStateAdmin fromString(String eventStateAdmin) {
        try {
            return EventStateAdmin.valueOf(eventStateAdmin.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Параметр EventStateAdmin может принимать значения PUBLISH_EVENT или REJECT_EVENT. Передан " + eventStateAdmin);
        }
    }
}
