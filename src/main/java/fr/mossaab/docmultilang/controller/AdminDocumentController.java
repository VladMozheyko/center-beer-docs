package fr.mossaab.docmultilang.controller;

import fr.mossaab.docmultilang.dto.DocumentDto;
import fr.mossaab.docmultilang.dto.request.DocumentTypeCreateRequest;
import fr.mossaab.docmultilang.dto.request.LanguageCreateRequest;
import fr.mossaab.docmultilang.service.DocumentService;
import fr.mossaab.docmultilang.service.DocumentTypeService;
import fr.mossaab.docmultilang.service.LanguageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/documents")
@RequiredArgsConstructor
@Tag(name = "Admin Documents", description = "Управление документами (только для админов)")
@Validated
public class AdminDocumentController {

    private final DocumentService docService;
    private final DocumentTypeService docTypeService;
    private final LanguageService languageService;

    @Operation(summary = "Добавление нового документа или обновление версии")
    @PostMapping("/create/document")
    public ResponseEntity<Void> createOrUpdate(
            @Valid @RequestBody DocumentDto dto) {
        docService.saveNewVersion(dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Добавление нового типа документа")
    @PostMapping("/create/document-type")
    public ResponseEntity<Void> createNewDocumentType(
            @Valid @RequestBody DocumentTypeCreateRequest request) {
        boolean isCreate = docTypeService.createNew(request.getCode(), request.getName());
        if (isCreate) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Добавление нового языка")
    @PostMapping("/create/language")
    public ResponseEntity<Void> createNewLanguage(
            @Valid @RequestBody LanguageCreateRequest request) {
        boolean isCreate = languageService.createNew(request.getCode(), request.getName());
        if (isCreate) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
