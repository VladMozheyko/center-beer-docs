package fr.mossaab.docmultilang.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Базовый абстрактный класс исключений API.
 */
@Getter
public abstract class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    protected ApiException(String message, HttpStatus status, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
