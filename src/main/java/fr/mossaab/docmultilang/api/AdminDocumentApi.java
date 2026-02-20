package fr.mossaab.docmultilang.api;

import fr.mossaab.docmultilang.dto.request.DocumentCreateRequest;
import fr.mossaab.docmultilang.dto.response.DocumentResponseDto;
import fr.mossaab.docmultilang.dto.request.DocumentTypeCreateRequest;
import fr.mossaab.docmultilang.dto.response.DocumentTypeResponseDto;
import fr.mossaab.docmultilang.dto.request.LanguageCreateRequest;
import fr.mossaab.docmultilang.dto.response.LanguageResponseDto;
import fr.mossaab.docmultilang.exception.dto.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;

@Tag(name = "Admin Documents", description = "API для админа")
@RequestMapping("/admin/documents")
public interface AdminDocumentApi {

    @Operation(
            summary = "Создание нового документа или обновление существующей версии",
            description = "Метод позволяет добавить новый документ или обновить существующую версию документа. " +
                    "При наличии активной версии документа она будет деактивирована, а новая версия станет активной.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Документ успешно создан/обновлен",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = DocumentResponseDto.class
                                    ),
                                    examples = {
                                            @ExampleObject(
                                                    summary = "Пример успешного ответа",
                                                    value = """
                                                            {
                                                                "id": 1,
                                                                "typeName": "Правила",
                                                                "languageName": "Русский",
                                                                "version": "1.0"
                                                            }"""
                                            )
                                    })
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверные параметры",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ApiError.class
                                    ),
                                    examples = {
                                            @ExampleObject(
                                                    summary = "Пример ошибки валидации",
                                                    value = """
                                                            {
                                                                "code": "VALIDATION_ERROR",
                                                                "message": "Validation failed",
                                                                "details": {
                                                                    "typeCode": "Тип обязателен",
                                                                    "langCode": "Язык обязателен",
                                                                }
                                                            }"""
                                            )
                                    })
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Тип документа или язык не найдены",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = ApiError.class
                                    ),
                                    examples = {
                                            @ExampleObject(
                                                    summary = "Тип документа не найден",
                                                    value = """
                                                            {
                                                                "code": "NOT_FOUND",
                                                                "message": "Не найден тип документа с кодом 'bad_code'.",
                                                                "details": null
                                                            }
                                                            """
                                            )
                                    })
                    )
    })
    @PostMapping("/create/document")
    ResponseEntity<DocumentResponseDto> createOrUpdate(
            @RequestBody(
                    description = "Данные новой версии документа",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = DocumentCreateRequest.class
                            ),
                            examples = {@ExampleObject(
                                    summary = "Пример запроса",
                                    value = """
                                            {
                                                "typeCode": "rules",
                                                "langCode": "ru",
                                                "content": "Текст документа на русском языке"
                                            }"""
                            )})) @org.springframework.web.bind.annotation.RequestBody @Valid DocumentCreateRequest dto);



    @Operation(
            summary = "Добавление нового типа документа",
            description = "Создает новый тип документа в системе",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Тип документа успешно создан",
                            content = @Content(schema = @Schema(implementation = DocumentTypeResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Тип документа с таким кодом уже существует",
                            content = @Content(
                                    schema = @Schema(implementation = ApiError.class),
                                    examples = {
                                            @ExampleObject(
                                                    summary = "Конфликт при создании типа документа",
                                                    value = """
                        {
                            "code": "CONFLICT",
                            "message": "Тип документа 'rules' существует",
                            "details": null
                        }
                        """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверные параметры",
                            content = @Content(
                                    schema = @Schema(implementation = ApiError.class),
                                    examples = {
                                            @ExampleObject(
                                                    summary = "Ошибка валидации",
                                                    value = """
                        {
                            "code": "VALIDATION_ERROR",
                            "message": "Validation failed",
                            "details": {
                                "code": "Код типа обязателен",
                                "name": "Название типа обязательно"
                            }
                        }
                        """
                                            )
                                    }
                            )
                    )
            }
    )
    @PostMapping("/create/document-type")
    ResponseEntity<DocumentTypeResponseDto> createNewDocumentType(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные нового типа документа",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = DocumentTypeCreateRequest.class
                            ), examples = {@ExampleObject(
                            summary = "Пример запроса",
                            value = """
                                    {
                                        "code": "rules",
                                        "name": "Правила"
                                    }"""
                    )}
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody @Valid DocumentTypeCreateRequest request
    );


    @Operation(
            summary = "Добавление нового языка",
            description = "Создает новый язык документа",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Язык успешно создан",
                            content = @Content(schema = @Schema(implementation = LanguageResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Язык с таким кодом уже существует",
                            content = @Content(
                                    schema = @Schema(implementation = ApiError.class),
                                    examples = {
                                            @ExampleObject(
                                                    summary = "Конфликт при создании языка",
                                                    value = """
                        {
                            "code": "CONFLICT",
                            "message": "Язык документа 'ru' существует",
                            "details": null
                        }
                        """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверные параметры",
                            content = @Content(
                                    schema = @Schema(implementation = ApiError.class),
                                    examples = {
                                            @ExampleObject(
                                                    summary = "Ошибка валидации",
                                                    value = """
                        {
                            "code": "VALIDATION_ERROR",
                            "message": "Validation failed",
                            "details": {
                                "code": "Код языка обязателен",
                                "name": "Название языка обязательно"
                            }
                        }
                        """
                                            )
                                    }
                            )
                    )
            }
    )
    @PostMapping("/create/language")
    ResponseEntity<LanguageResponseDto> createNewLanguage(
            @RequestBody(description = "Данные нового языка",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = LanguageCreateRequest.class
                            ), examples = {@ExampleObject(
                            summary = "Пример запроса",
                            value = """
                                    {
                                        "code": "ru",
                                        "name": "Русский"
                                    }"""
                    )}
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody @Valid LanguageCreateRequest request
    );
}
