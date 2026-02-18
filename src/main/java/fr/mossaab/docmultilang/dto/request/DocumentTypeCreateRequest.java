package fr.mossaab.docmultilang.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentTypeCreateRequest {

    @NotBlank(message = "Обязательно для заполнения")
    private String code;
    @NotBlank(message = "Обязательно для заполнения")
    private String name;
}
