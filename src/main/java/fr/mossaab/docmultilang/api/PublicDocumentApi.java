package fr.mossaab.docmultilang.api;

import fr.mossaab.docmultilang.dto.response.DocumentResponseDto;
import fr.mossaab.docmultilang.dto.response.LanguageResponseDto;
import fr.mossaab.docmultilang.entity.DocumentType;
import fr.mossaab.docmultilang.entity.Language;
import fr.mossaab.docmultilang.exception.dto.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@Tag(name = "Documents", description = "Публичный API документов")
@RequestMapping("/documents")
public interface PublicDocumentApi {

    @Operation(
            summary = "Получить документ по типу и языку",
            description = "Возвращает активную версию документа по указанному типу и языку.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Документ найден",
                            content = @Content(
                                    schema = @Schema(implementation = DocumentResponseDto.class),
                                    examples = @ExampleObject(
                                            name = "successExample",
                                            summary = "Пример успешного ответа",
                                            value = """
                                                {
                                                    "id": 1,
                                                    "typeName": "Правила",
                                                    "languageName": "Russian",
                                                    "version": "1.0",
                                                    "createAt": "2024-01-01T12:00:00"
                                                }
                                                """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Документ не найден",
                            content = @Content(
                                    schema = @Schema(implementation = ApiError.class),
                                    examples = @ExampleObject(
                                            name = "notFoundExample",
                                            summary = "Пример ошибки NOT_FOUND",
                                            value = """
                                                {
                                                    "code": "NOT_FOUND",
                                                    "message": "Документ не найден: rules/ru",
                                                    "details": null
                                                }
                                                """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/{type}")
    ResponseEntity<DocumentResponseDto> getDocument(
            @Parameter(description = "Тип документа", example = "rules") @PathVariable("type") String type,
            @Parameter(description = "Язык", example = "ru") @RequestParam("lang") String lang
    );

    @Operation(
            summary = "Список всех доступных документов и языков",
            description = "Возвращает сводку по активным документам: типы и поддерживаемые языки для каждого типа.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешно",
                            content = @Content(
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(
                                            name = "listAllExample",
                                            summary = "Пример ответа",
                                            value = """
                                                {
                                                    "documents": [
                                                        { "type": "rules", "languages": ["ru","en"] },
                                                        { "type": "eula", "languages": ["en"] }
                                                    ]
                                                }
                                                """
                                    )
                            )
                    )
            }
    )
    @GetMapping
    ResponseEntity<Map<String, Object>> listAll();

    @Operation(
            summary = "Список всех доступных типов документов",
            description = "Возвращает все доступные типы документов в системе.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Типы документов получены",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = DocumentType.class),
                                    examples = @ExampleObject(
                                            name = "documentTypesExample",
                                            summary = "Пример списка типов",
                                            value = """
                                                [
                                                    { "id": 1, "code": "rules", "name": "Правила" },
                                                    { "id": 2, "code": "offer", "name": "Оферта" }
                                                ]
                                                """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/all_documents_type")
    ResponseEntity<List<DocumentType>> listAllDocumentsType();

    @Operation(
            summary = "Список всех доступных языков",
            description = "Возвращает все поддерживаемые языки.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Языки получены",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Language.class),
                                    examples = @ExampleObject(
                                            name = "languagesExample",
                                            summary = "Пример списка языков",
                                            value = """
                                                [
                                                    { "id": 1, "code": "ru", "name": "Русский" },
                                                    { "id": 2, "code": "en", "name": "English" }
                                                ]
                                                """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/all_language")
    ResponseEntity<List<Language>> listAllLanguage();
}