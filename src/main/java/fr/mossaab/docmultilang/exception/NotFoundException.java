package fr.mossaab.docmultilang.exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение ресурсов, которых нет.
 * HTTP: 404 Not Found.
 * Когда бросать: когда сущность (например тип/язык/документ) не найдена.
 */
public class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }
}
