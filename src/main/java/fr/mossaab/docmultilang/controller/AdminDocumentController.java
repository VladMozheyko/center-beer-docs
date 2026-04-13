package fr.mossaab.docmultilang.controller;

import fr.mossaab.docmultilang.api.AdminDocumentApi;
import fr.mossaab.docmultilang.dto.request.DocumentCreateRequest;
import fr.mossaab.docmultilang.dto.request.DocumentTypeCreateRequest;
import fr.mossaab.docmultilang.dto.request.LanguageCreateRequest;
import fr.mossaab.docmultilang.dto.response.DocumentResponseDto;
import fr.mossaab.docmultilang.dto.response.DocumentTypeResponseDto;
import fr.mossaab.docmultilang.dto.response.LanguageResponseDto;
import fr.mossaab.docmultilang.service.DocumentService;
import fr.mossaab.docmultilang.service.DocumentTypeService;
import fr.mossaab.docmultilang.service.LanguageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class AdminDocumentController implements AdminDocumentApi {

    private final DocumentService docService;
    private final DocumentTypeService docTypeService;
    private final LanguageService languageService;

    @Override
    public ResponseEntity<DocumentResponseDto> createOrUpdate(DocumentCreateRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(docService.saveNewVersion(dto));
    }

    @Override
    public ResponseEntity<DocumentTypeResponseDto> createNewDocumentType(DocumentTypeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(docTypeService.createNew(request.getCode(), request.getName()));
    }

    @Override
    public ResponseEntity<LanguageResponseDto> createNewLanguage(LanguageCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(languageService.createNew(request.getCode(), request.getName()));
    }
}
