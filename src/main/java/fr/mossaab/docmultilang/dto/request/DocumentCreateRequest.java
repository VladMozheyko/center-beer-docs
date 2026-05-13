package fr.mossaab.docmultilang.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentCreateRequest {

    @NotBlank(message = "Тип обязателен")
    private String typeCode;
    @NotBlank(message = "Язык обязателен")
    @Size(min = 2, max = 2, message = "Значение должно быть в нижнем регистре и состоять из 2 символов ISO 639-1")
    private String langCode;
    @NotBlank(message = "Содержание обязателено")
    private String content;
}
