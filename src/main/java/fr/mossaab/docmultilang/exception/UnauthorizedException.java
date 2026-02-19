package fr.mossaab.docmultilang.exception;

import org.springframework.http.HttpStatus;

/**
 * Отсутствие корректной аутентификации.
 * HTTP: 401 Unauthorized.
 * Когда бросать: когда пользователь не аутентифицирован или токен недействителен.
 */
public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}
