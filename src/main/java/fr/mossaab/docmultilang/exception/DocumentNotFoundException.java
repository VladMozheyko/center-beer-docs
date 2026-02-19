package fr.mossaab.docmultilang.exception;

import org.springframework.http.HttpStatus;

/**
 * Конкретное исключение для не найденного документа.
 * HTTP: 404 Not Found.
 * Когда бросать: когда сущность документ не найдена.
 */
public class DocumentNotFoundException extends ApiException {
    public DocumentNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }
}
