package ru.practicum.model;

import ru.practicum.exception.ValidationException;

public enum EventUserSort {
    EVENT_DATE,
    VIEWS;

    public static EventUserSort fromString(String sort) {
        EventUserSort eventSort;
        try {
            eventSort = EventUserSort.valueOf(sort.toUpperCase());
        } catch (ValidationException e) {
            throw new ValidationException("Параметр сортировки sort может принимать значения EVENT_DATE или VIEWS. Передан " + sort);
        }
        return eventSort;
    }
}
