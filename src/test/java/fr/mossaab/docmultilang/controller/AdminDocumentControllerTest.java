package fr.mossaab.docmultilang.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import fr.mossaab.docmultilang.controller.AdminDocumentController;
import fr.mossaab.docmultilang.dto.request.DocumentCreateRequest;
import fr.mossaab.docmultilang.dto.request.DocumentTypeCreateRequest;
import fr.mossaab.docmultilang.dto.request.LanguageCreateRequest;
import fr.mossaab.docmultilang.dto.response.DocumentResponseDto;
import fr.mossaab.docmultilang.dto.response.DocumentTypeResponseDto;
import fr.mossaab.docmultilang.dto.response.LanguageResponseDto;
import fr.mossaab.docmultilang.exception.ConflictException;
import fr.mossaab.docmultilang.exception.DocumentNotFoundException;
import fr.mossaab.docmultilang.service.DocumentService;
import fr.mossaab.docmultilang.service.DocumentTypeService;
import fr.mossaab.docmultilang.service.LanguageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminDocumentController.class)
@DisplayName("Unit-тесты для контроллера AdminDocumentController")
class AdminDocumentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    DocumentService documentService;

    @MockBean
    DocumentTypeService docTypeService;

    @MockBean
    LanguageService languageService;

    @Nested
    @DisplayName("Создание или обновление документа")
    class CreateOrUpdateDoc {
        @Test
        @DisplayName("Успех: возвращает 201 и тело DTO")
        void createOrUpdateDocument_returns201Created() throws Exception {
            DocumentCreateRequest req = DocumentCreateRequest.builder()
                    .typeCode("rules")
                    .langCode("ru")
                    .content("Текст документа на русском")
                    .build();

            DocumentResponseDto dto = DocumentResponseDto.builder()
                    .id(5L)
                    .typeName("Правила")
                    .languageName("Русский")
                    .version(2)
                    .build();

            when(documentService.saveNewVersion(any())).thenReturn(dto);

            mockMvc.perform(post("/admin/documents/create/document")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(5L))
                    .andExpect(jsonPath("$.typeName").value("Правила"))
                    .andExpect(jsonPath("$.languageName").value("Русский"))
                    .andExpect(jsonPath("$.version").value(2));
        }

        @Test
        @DisplayName("Ошибка: документ не найден (404)")
        void createOrUpdateDocument_notFoundException() throws Exception {
            DocumentCreateRequest req = DocumentCreateRequest.builder()
                    .typeCode("badcode")
                    .langCode("ru")
                    .content("text")
                    .build();

            when(documentService.saveNewVersion(any())).thenThrow(
                    new DocumentNotFoundException("Не найден тип документа с кодом 'badcode'.")
            );

            mockMvc.perform(post("/admin/documents/create/document")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("Создание нового типа документа")
    class CreateNewType {
        @Test
        @DisplayName("Успех: возвращает 201 и DTO типа документа")
        void createType_success() throws Exception {
            DocumentTypeCreateRequest req = DocumentTypeCreateRequest.builder()
                    .code("rules")
                    .name("Правила")
                    .build();

            DocumentTypeResponseDto dto = new DocumentTypeResponseDto(1L, "Правила");

            when(docTypeService.createNew(anyString(), anyString())).thenReturn(dto);

            mockMvc.perform(post("/admin/documents/create/document-type")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Правила"));
        }

        @Test
        @DisplayName("Конфликт: тип документа уже существует (409)")
        void createType_conflict() throws Exception {
            DocumentTypeCreateRequest req = DocumentTypeCreateRequest.builder()
                    .code("rules")
                    .name("Правила")
                    .build();

            when(docTypeService.createNew(anyString(), anyString()))
                    .thenThrow(new ConflictException("Тип документа 'rules' существует"));

            mockMvc.perform(post("/admin/documents/create/document-type")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("CONFLICT"))
                    .andExpect(jsonPath("$.message").value("Тип документа 'rules' существует"));
        }
    }

    @Nested
    @DisplayName("Создание нового языка")
    class CreateNewLanguage {
        @Test
        @DisplayName("Успех: возвращает 201 и DTO языка")
        void createLang_success() throws Exception {
            LanguageCreateRequest req = LanguageCreateRequest.builder()
                    .code("ru")
                    .name("Русский")
                    .build();

            LanguageResponseDto dto = new LanguageResponseDto(1L, "Русский");

            when(languageService.createNew(eq("ru"), eq("Русский"))).thenReturn(dto);

            mockMvc.perform(post("/admin/documents/create/language")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Русский"));
        }

        @Test
        @DisplayName("Конфликт: язык уже существует (409)")
        void createLang_conflict() throws Exception {
            LanguageCreateRequest req = LanguageCreateRequest.builder()
                    .code("ru")
                    .name("Русский")
                    .build();

            when(languageService.createNew(anyString(), anyString()))
                    .thenThrow(new ConflictException("Язык документа 'ru' существует"));

            mockMvc.perform(post("/admin/documents/create/language")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("CONFLICT"))
                    .andExpect(jsonPath("$.message").value("Язык документа 'ru' существует"));
        }
    }
}
