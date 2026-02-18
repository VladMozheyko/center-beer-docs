package fr.mossaab.docmultilang.controller;

import fr.mossaab.docmultilang.dto.DocumentDto;
import fr.mossaab.docmultilang.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Публичный API документов")
public class PublicDocumentController {

    private final DocumentService docService;

    @Operation(summary = "Получить документ по типу и языку")
    @GetMapping("/{type}")
    public ResponseEntity<DocumentDto> getDocument(
            @Parameter(description = "Тип документа", example = "rules") @PathVariable String type,
            @Parameter(description = "Язык", example = "ru") @RequestParam String lang) {
        return ResponseEntity.ok(docService.getDocument(type, lang));
    }

    @Operation(summary = "Список всех доступных документов и языков")
    @GetMapping
    public ResponseEntity<Map<String, Object>> listAll() {
        Map<String, Object> result = Map.of("documents", docService.listAvailableDocuments());
        return ResponseEntity.ok(result);
    }
}
