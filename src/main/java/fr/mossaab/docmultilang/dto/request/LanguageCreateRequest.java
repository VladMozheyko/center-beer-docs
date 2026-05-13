package fr.mossaab.docmultilang.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LanguageCreateRequest {

    @NotBlank(message = "Обязательно для заполнения")
    @Size(min = 2, max = 2, message = "Значение должно быть ровно 2 символа ('ru','en')")
    @Pattern(regexp = "^[a-z]{2}$", message = "Значение должно быть в нижнем регистре и иметь ровно 2 символа ('ru','en')")
    private String code;
    @NotBlank(message = "Обязательно для заполнения")
    private String name;
}
