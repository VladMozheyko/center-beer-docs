package fr.mossaab.docmultilang.exception;

import org.springframework.http.HttpStatus;

/**
 * Клиентская ошибка из-за неверного запроса/данных.
 * HTTP: 400 Bad Request.
 * Когда бросать: при валидируемых или некорректных входных данных, нарушении бизнес-логики на входе.
 */
public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }
}