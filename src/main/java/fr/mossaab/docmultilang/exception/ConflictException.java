package fr.mossaab.docmultilang.exception;

import org.springframework.http.HttpStatus;

/**
 * Конфликт текущего состояния ресурса (уже существует, противоречие).
 * HTTP: 409 Conflict.
 * Когда бросать: попытка создать дублирующий ресурс или нарушить уникальные ограничения.
 */
public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "CONFLICT");
    }
}
