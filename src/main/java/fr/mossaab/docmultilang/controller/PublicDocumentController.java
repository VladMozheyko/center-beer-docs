package fr.mossaab.docmultilang.controller;

import fr.mossaab.docmultilang.api.PublicDocumentApi;
import fr.mossaab.docmultilang.dto.response.DocumentResponseDto;
import fr.mossaab.docmultilang.dto.response.LanguageResponseDto;
import fr.mossaab.docmultilang.entity.DocumentType;
import fr.mossaab.docmultilang.entity.Language;
import fr.mossaab.docmultilang.repository.LanguageRepository;
import fr.mossaab.docmultilang.service.DocumentService;
import fr.mossaab.docmultilang.service.DocumentTypeService;
import fr.mossaab.docmultilang.service.LanguageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Публичный API документов")
public class PublicDocumentController implements PublicDocumentApi {

    private final DocumentService docService;
    private final DocumentTypeService documentTypeService;
    private final LanguageService languageService;

    @Override
    public ResponseEntity<DocumentResponseDto> getDocument(
            @Parameter(description = "Тип документа", example = "rules") @PathVariable("type") String type,
            @Parameter(description = "Язык", example = "ru") @RequestParam("lang") String lang) {
        return ResponseEntity.ok(docService.getDocument(type, lang));
    }

    @Override
    public ResponseEntity<Map<String, Object>> listAll() {
        Map<String, Object> result = Map.of("documents", docService.listAvailableDocuments());
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<List<DocumentType>> listAllDocumentsType() {
        List<DocumentType> allDocumentsType = documentTypeService.getAll();
        return ResponseEntity.ok(allDocumentsType);
    }

    @Override
    public ResponseEntity<List<Language>> listAllLanguage() {
        List<Language> allLanguages = languageService.getAll();
        return ResponseEntity.ok(allLanguages);
    }
}
