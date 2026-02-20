package fr.mossaab.docmultilang.mapper;

import fr.mossaab.docmultilang.dto.response.DocumentResponseDto;
import fr.mossaab.docmultilang.dto.response.DocumentTypeResponseDto;
import fr.mossaab.docmultilang.dto.response.LanguageResponseDto;
import fr.mossaab.docmultilang.entity.Document;
import fr.mossaab.docmultilang.entity.DocumentType;
import fr.mossaab.docmultilang.entity.Language;

public class EntityToDtoMapper {

    public static DocumentResponseDto toDto(Document document) {
        DocumentResponseDto dto = new DocumentResponseDto();
        dto.setId(document.getId());
        dto.setVersion(document.getVersion());
        dto.setLanguageName(document.getLanguage().getName());
        dto.setTypeName(document.getType().getName());
        dto.setContent(document.getContent());
        dto.setCreateAt(document.getCreatedAt());
        return dto;
    }

    public static DocumentTypeResponseDto toDto(DocumentType documentType) {
        return DocumentTypeResponseDto.builder()
                .id(documentType.getId())
                .name(documentType.getName())
                .build();
    }

    public static LanguageResponseDto toDto(Language language) {
        return LanguageResponseDto.builder()
                .id(language.getId())
                .name(language.getName())
                .build();
    }


}
