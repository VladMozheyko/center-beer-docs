package fr.mossaab.docmultilang.exception.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Унифицированный DTO-ответ об ошибке.
 * Что содержит: code, message, optional details (карта полей с сообщениями).
 * Зачем нужен: единообразный ответ клиенту независимо от конкретного исключения.
 * Пример использования: глобальный обработчик формирует ApiError и возвращает клиенту.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {
    private String code;
    private String message;
    @Nullable
    private Map<String, String> details;
}
