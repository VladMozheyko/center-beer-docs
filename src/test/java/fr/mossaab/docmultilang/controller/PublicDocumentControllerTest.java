package fr.mossaab.docmultilang.controller;


import fr.mossaab.docmultilang.dto.DocumentTypeListDto;
import fr.mossaab.docmultilang.dto.response.DocumentResponseDto;
import fr.mossaab.docmultilang.entity.DocumentType;
import fr.mossaab.docmultilang.entity.Language;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(PublicDocumentController.class)
class PublicDocumentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    DocumentService docService;

    @MockBean
    DocumentTypeService docTypeService;

    @MockBean
    LanguageService languageService;

    @Nested
    @DisplayName("getDocument: Публичное получение документа")
    class GetDocument {

        @Test
        @DisplayName("Успех: возвращает документ (200)")
        void getDocument_success() throws Exception {
            DocumentResponseDto dto = DocumentResponseDto.builder()
                    .id(1L)
                    .typeName("rules")
                    .languageName("ru")
                    .content("text")
                    .createAt(LocalDateTime.of(2024,1,1,12,0))
                    .version(1)
                    .build();

            when(docService.getDocument(eq("rules"), eq("ru"))).thenReturn(dto);

            mockMvc.perform(get("/documents/rules").param("lang", "ru"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(dto.getId()))
                    .andExpect(jsonPath("$.typeName").value("rules"))
                    .andExpect(jsonPath("$.languageName").value("ru"))
                    .andExpect(jsonPath("$.version").value(dto.getVersion()))
                    .andExpect(jsonPath("$.content").value("text"));
        }

        @Test
        @DisplayName("Ошибка: документ не найден (404)")
        void getDocument_notFound() throws Exception {
            when(docService.getDocument(eq("rules"), eq("ru"))).thenThrow(
                    new DocumentNotFoundException("Документ не найден: rules/ru")
            );

            mockMvc.perform(get("/documents/rules").param("lang", "ru"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message", containsString("Документ не найден")));
        }
    }

    @Test
    @DisplayName("listAll: возвращает структуру Map с документами")
    void listAll_returnsDocumentListMap() throws Exception {
        List<DocumentTypeListDto> availableDocs = List.of(
                new DocumentTypeListDto("rules",  List.of("ru")),
                new DocumentTypeListDto("type",  List.of("en"))
        );
        when(docService.listAvailableDocuments()).thenReturn(availableDocs);

        mockMvc.perform(get("/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documents", hasSize(2)))
                .andExpect(jsonPath("$.documents[0].type").value("rules"));
    }

    @Test
    @DisplayName("listAllDocumentsType: возвращает список всех типов документов")
    void listAllDocumentsType_returnsAllTypes() throws Exception {
        List<DocumentType> types = List.of(
                DocumentType.builder().id(1L).code("rules").name("Правила").build(),
                DocumentType.builder().id(2L).code("eula").name("Польз. соглашение").build()
        );
        when(docTypeService.getAll()).thenReturn(types);

        mockMvc.perform(get("/documents/all_documents_type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code").value("rules"));
    }

    @Test
    @DisplayName("listAllLanguage: возвращает список всех языков")
    void listAllLanguage_returnsAllLanguages() throws Exception {
        List<Language> langs = List.of(
                Language.builder().id(1L).code("ru").name("Русский").build(),
                Language.builder().id(2L).code("en").name("English").build()
        );
        when(languageService.getAll()).thenReturn(langs);

        mockMvc.perform(get("/documents/all_language"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code").value("ru"));
    }
}
