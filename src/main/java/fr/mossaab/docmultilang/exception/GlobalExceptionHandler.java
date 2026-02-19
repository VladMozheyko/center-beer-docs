package fr.mossaab.docmultilang.exception;

import fr.mossaab.docmultilang.exception.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import jakarta.validation.ConstraintViolationException;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений.
 * Что делает: конвертирует любые ApiException в стандартный ApiError с соответствующим HTTP-статусом;
 * обрабатывает валидаторы, ошибки БД, прочие системные исключения.
 * Зачем нужен: единая структура ошибок на фронте и упрощённая отладка.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Общее для ApiException и его подклассов
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex, HttpServletRequest request) {
        log.warn("API error: code={}, message={}. Request: {} {}", ex.getCode(), ex.getMessage(), request.getMethod(),
                request.getRequestURI());
        ApiError error = new ApiError(ex.getCode(), ex.getMessage(), null);
        return new ResponseEntity<>(error, ex.getStatus());
    }

    // Нестандартные исключения из кода выше (на случай, если сущность ещё кидает NoSuchElementException)
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNotFound(NoSuchElementException ex, HttpServletRequest request) {
        log.info("Not found: {} {}", request.getMethod(), request.getRequestURI());
        ApiError error = new ApiError("NOT_FOUND", ex.getMessage(), null);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // Валидация входных данных на уровне DTO (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));

        log.warn("Validation error at {} {}: {}", request.getMethod(), request.getRequestURI(), fieldErrors);

        ApiError error = new ApiError("VALIDATION_ERROR", "Validation failed", fieldErrors);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Валидaция через javax/jakarta constraints на уровне сущности
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> details = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage));

        log.warn("Constraint violation at {} {}: {}", request.getMethod(), request.getRequestURI(), details);

        ApiError error = new ApiError("VALIDATION_ERROR", "Validation failed", details);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Ошибка чтения тела запроса (плохой JSON)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON request at {} {}", request.getMethod(), request.getRequestURI());
        ApiError error = new ApiError("BAD_REQUEST", "Malformed JSON request", null);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Ошибки БД
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDataAccess(DataAccessException ex, HttpServletRequest request) {
        ex.getMostSpecificCause();
        String dbMessage = ex.getMostSpecificCause().getMessage();
        log.error("Database error at {} {}: {}", request.getMethod(), request.getRequestURI(), dbMessage);
        ApiError error = new ApiError("DATABASE_ERROR", "Database error: " + dbMessage, null);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

// На будущее для  Spring Security и исключения доступа:
// @ExceptionHandler(SecurityException.class) или конкретно AccessDeniedException, если есть зависимость
/*
@ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
public ResponseEntity<ApiError> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
ApiError error = new ApiError("FORBIDDEN", "Access denied", null);
return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
}
*/

    // Прочие исключения
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method not allowed at {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        ApiError error = new ApiError("METHOD_NOT_ALLOWED", ex.getMessage(), null);
        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {} {}: ", request.getMethod(), request.getRequestURI(), ex);
        ApiError error = new ApiError("INTERNAL_SERVER_ERROR", "Unexpected error: " + ex.getMessage(), null);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}