package fr.mossaab.docmultilang.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentCreateRequest {

    @NotBlank(message = "Тип обязателен")
    private String typeCode;
    @NotBlank(message = "Язык обязателен")
    @Size(min = 2, max = 2, message = "Значение должно быть в нижнем регистре и состоять из 2 символов ISO 639-1")
    private String langCode;
    @Pattern(
            regexp = "^\\d+\\.\\d+$",
            message = "Формат версии должен быть 'число.число', например: 1.0, 2.5, 10.12"
    )
    @NotNull(message = "Версия документа не может быть пустой")
    private String version;
    @NotBlank(message = "Содержание обязателено")
    private String content;
}
