package fr.mossaab.docmultilang.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import fr.mossaab.docmultilang.dto.request.DocumentCreateRequest;
import fr.mossaab.docmultilang.dto.request.DocumentTypeCreateRequest;
import fr.mossaab.docmultilang.dto.request.LanguageCreateRequest;
import fr.mossaab.docmultilang.entity.DocumentType;
import fr.mossaab.docmultilang.entity.Language;
import fr.mossaab.docmultilang.repository.DocumentRepository;
import fr.mossaab.docmultilang.repository.DocumentTypeRepository;
import fr.mossaab.docmultilang.repository.LanguageRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Интеграционный тест для связки контроллера AdminDocumentController и его сервисов с репозиториями")
class AdminDocumentControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    DocumentTypeRepository docTypeRepo;

    @Autowired
    LanguageRepository langRepo;

    @Autowired
    DocumentRepository docRepo;

    @BeforeEach
    void setUp() {
        docRepo.deleteAll();
        docTypeRepo.deleteAll();
        langRepo.deleteAll();
    }

    @Test
    @DisplayName("Создание нового языка (H2 integration)")
    void createNewLanguage_integration() throws Exception {
        LanguageCreateRequest req = LanguageCreateRequest.builder()
                .code("en")
                .name("English")
                .build();

        mockMvc.perform(post("/admin/documents/create/language")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("English"));

        assertEquals(1, langRepo.count());
        Language lang = langRepo.findByCode("en").orElseThrow();
        assertEquals("English", lang.getName());
    }

    @Test
    @DisplayName("Создание нового типа документа (H2 integration)")
    void createNewDocumentType_integration() throws Exception {
        DocumentTypeCreateRequest req = DocumentTypeCreateRequest.builder()
                .code("rules")
                .name("Правила")
                .build();

        mockMvc.perform(post("/admin/documents/create/document-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Правила"));

        assertEquals(1, docTypeRepo.count());
        DocumentType type = docTypeRepo.findByCode("rules").orElseThrow();
        assertEquals("Правила", type.getName());
    }

    @Test
    @DisplayName("Создание нового документа с связыванием по type и lang (integration, H2 logic)")
    void createOrUpdateDocument_integration() throws Exception {
        docTypeRepo.save(DocumentType.builder().code("rules").name("Правила").build());
        langRepo.save(Language.builder().code("ru").name("Русский").build());

        DocumentCreateRequest req = DocumentCreateRequest.builder()
                .typeCode("rules")
                .langCode("ru")
                .content("Текст документа на русском")
                .build();

        mockMvc.perform(post("/admin/documents/create/document")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.typeName").value("Правила"))
                .andExpect(jsonPath("$.languageName").value("Русский"))
                .andExpect(jsonPath("$.content").value("Текст документа на русском"))
                .andExpect(jsonPath("$.version", is(1)));
    }

    @Test
    @DisplayName("Создание документа для второго языка создает новую версию для другого языка (integration, H2 logic)")
    void createDocument_updatesOnlyTargetLang() throws Exception {
        docTypeRepo.save(DocumentType.builder().code("rules").name("Правила").build());
        langRepo.save(Language.builder().code("ru").name("Русский").build());
        langRepo.save(Language.builder().code("en").name("English").build());

        // Сначала на русском
        DocumentCreateRequest reqRu = DocumentCreateRequest.builder()
                .typeCode("rules").langCode("ru").content("На русском!").build();
        mockMvc.perform(post("/admin/documents/create/document")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqRu)))
                .andExpect(status().isCreated());

        // Потом создаём на английском
        DocumentCreateRequest reqEn = DocumentCreateRequest.builder()
                .typeCode("rules").langCode("en").content("In English!").build();
        mockMvc.perform(post("/admin/documents/create/document")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqEn)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version", is(1)))
                .andExpect(jsonPath("$.content", is("In English!")));

        assertEquals(2, docRepo.count());
    }
}
