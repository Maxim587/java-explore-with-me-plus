package ru.practicum.validation;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.dto.ApiError;
import ru.practicum.exception.ConditionsConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(MissingRequestValueException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingRequestValue(final MissingRequestValueException e) {
        log.debug(e.getMessage());
        return new ApiError(e.getMessage(), "Отсутствуют необходимые параметры запроса", HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiError handleValidationExceptions(MethodArgumentNotValidException e) {
        List<String> violations = e.getBindingResult().getFieldErrors().stream()
                .map(error -> "field: " + error.getField() + "; message: " + error.getDefaultMessage())
                .toList();
        log.debug(e.getMessage());
        return new ApiError("Ошибка валидации данных", "Некорректные параметры запроса", HttpStatus.BAD_REQUEST, violations);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(final NotFoundException e) {
        log.debug(e.getMessage());
        return new ApiError(e.getMessage(), "Объект не найден", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConditionsConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConditionsConflict(final ConditionsConflictException e) {
        log.debug(e.getMessage());
        return new ApiError(e.getMessage(), "Нарушение условий выполнения запроса", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDuplicateParticipationRequestConstraintViolation(final Throwable e, HttpServletRequest request) {
        log.debug(e.getMessage());
        String req = request.getMethod() + " " + request.getRequestURI() + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
        String description = "";
        if (e.getMessage().contains("unique_user_event_request")) {
            description = ". Запрос на данное событие от пользователя уже существует";
        } else if (e.getMessage().contains("categories_name_unique")) {
            description = ". Категория с таким названием уже существует";
        }

        return new ApiError("Ошибка при вызове метода " + req + description,
                "Нарушение целостности данных",
                HttpStatus.CONFLICT);
    }
}
