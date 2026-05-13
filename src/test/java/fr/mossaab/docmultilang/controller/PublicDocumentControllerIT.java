package fr.mossaab.docmultilang.controller;

import fr.mossaab.docmultilang.entity.Document;
import fr.mossaab.docmultilang.entity.DocumentType;
import fr.mossaab.docmultilang.entity.Language;
import fr.mossaab.docmultilang.repository.DocumentRepository;
import fr.mossaab.docmultilang.repository.DocumentTypeRepository;
import fr.mossaab.docmultilang.repository.LanguageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Интеграционные тесты для связки контроллера PublicDocumentController и его сервисов с репозиториями")
class PublicDocumentControllerIT {

    @Autowired MockMvc mockMvc;

    @Autowired DocumentRepository docRepo;
    @Autowired DocumentTypeRepository typeRepo;
    @Autowired LanguageRepository langRepo;

    @BeforeEach
    void clearDatabase() {
        docRepo.deleteAll();
        typeRepo.deleteAll();
        langRepo.deleteAll();
    }

    @Test
    @DisplayName("getDocument: выдаёт конкретный документ по типу и языку")
    void getDocument_success() throws Exception {
        DocumentType type = typeRepo.save(DocumentType.builder().code("rules").name("Правила").build());
        Language lang = langRepo.save(Language.builder().code("ru").name("Русский").build());
        Document doc = docRepo.save(Document.builder()
                .type(type)
                .language(lang)
                .content("test.doc.ru")
                .version(1)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/documents/rules").param("lang", "ru"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(doc.getId()))
                .andExpect(jsonPath("$.typeName").value("Правила"))
                .andExpect(jsonPath("$.languageName").value("Русский"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.content").value("test.doc.ru"));
    }

    @Test
    @DisplayName("getDocument: если нет — 404")
    void getDocument_notFound() throws Exception {
        mockMvc.perform(get("/documents/offer").param("lang", "en"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("listAll: возвращает сводку документов и языков")
    void listAllDocuments_typeLangGrouped() throws Exception {
        DocumentType type1 = typeRepo.save(DocumentType.builder().code("rules").name("Правила").build());
        DocumentType type2 = typeRepo.save(DocumentType.builder().code("offer").name("Оферта").build());
        Language ru = langRepo.save(Language.builder().code("ru").name("Русский").build());
        Language en = langRepo.save(Language.builder().code("en").name("English").build());

        // rules-ru и rules-en и offer-en
        docRepo.save(Document.builder().type(type1).language(ru).active(true).content("r_ru").version(1).build());
        docRepo.save(Document.builder().type(type1).language(en).active(true).content("r_en").version(1).build());
        docRepo.save(Document.builder().type(type2).language(en).active(true).content("o_en").version(1).build());

        mockMvc.perform(get("/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documents", hasSize(2)))
                .andExpect(jsonPath("$.documents[0].type", anyOf(equalTo("rules"), equalTo("offer"))))
                .andExpect(jsonPath("$.documents[?(@.type == 'rules')].languages").isNotEmpty());
    }

    @Test
    @DisplayName("listAllDocumentsType: выдаёт все типы документов")
    void listAllDocumentsType_allTypes() throws Exception {
        DocumentType type1 = typeRepo.save(DocumentType.builder().code("rules").name("Правила").build());
        DocumentType type2 = typeRepo.save(DocumentType.builder().code("offer").name("Оферта").build());

        mockMvc.perform(get("/documents/all_documents_type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.code == 'rules')].name").value(hasItem("Правила")))
                .andExpect(jsonPath("$[?(@.code == 'offer')].name").value(hasItem("Оферта")));
    }

    @Test
    @DisplayName("listAllLanguage: выдаёт все языки")
    void listAllLanguage_allLangs() throws Exception {
        langRepo.save(Language.builder().code("ru").name("Русский").build());
        langRepo.save(Language.builder().code("en").name("English").build());

        mockMvc.perform(get("/documents/all_language"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code", anyOf(equalTo("ru"), equalTo("en"))));
    }
}
