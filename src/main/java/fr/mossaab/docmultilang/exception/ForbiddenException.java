package fr.mossaab.docmultilang.exception;

import org.springframework.http.HttpStatus;

/**
 * Доступ запрещён по политике безопасности, хотя пользователь может быть аутентифицирован.
 * HTTP: 403 Forbidden.
 * Когда бросать: когда есть права, но доступа к ресурсу нет (например, нет роли).
 */
public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }
}