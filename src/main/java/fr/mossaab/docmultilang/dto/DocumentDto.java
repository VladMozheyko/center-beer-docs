package fr.mossaab.docmultilang.dto;

import fr.mossaab.docmultilang.entity.Document;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDto {
    @NotBlank
            (message = "Тип обязателен")
    private String typeCode;
    @NotBlank(message = "Язык обязателен")
    private String langCode;
    private String version;
    private String content;
    private boolean isActive;
    private LocalDateTime updatedAt;

    public static DocumentDto fromEntity(Document document) {
        DocumentDto dto = new DocumentDto();
        dto.setTypeCode(document.getType().getCode());
        dto.setLangCode(document.getLanguage().getCode());
        dto.setVersion(document.getVersion());
        dto.setContent(document.getContent());
        dto.setActive(document.isActive());
        dto.setUpdatedAt(document.getUpdatedAt());
        return dto;
    }

    public DocumentDto(String typeCode, String langCode, String version, String content, boolean isActive) {
        this.typeCode = typeCode;
        this.langCode = langCode;
        this.version = version;
        this.content = content;
        this.isActive = isActive;
        this.updatedAt = LocalDateTime.now();
    }
}