package fr.mossaab.docmultilang.exception;

import org.springframework.http.HttpStatus;

/**
 * Бизнес-логика не может обработать запрос в текущем состоянии.
 * HTTP: 422 Unprocessable Entity.
 * Когда бросать: когда данные валидны синтаксически, но нарушают бизнес-правила.
 */
public class UnprocessableEntityException extends ApiException {
    public UnprocessableEntityException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, "UNPROCESSABLE_ENTITY");
    }
}
