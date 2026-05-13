package fr.mossaab.docmultilang.service;

import static org.junit.jupiter.api.Assertions.*;

import fr.mossaab.docmultilang.dto.DocumentTypeListDto;
import fr.mossaab.docmultilang.dto.request.DocumentCreateRequest;
import fr.mossaab.docmultilang.dto.response.DocumentResponseDto;
import fr.mossaab.docmultilang.entity.Document;
import fr.mossaab.docmultilang.entity.DocumentType;
import fr.mossaab.docmultilang.entity.Language;
import fr.mossaab.docmultilang.exception.DocumentNotFoundException;
import fr.mossaab.docmultilang.repository.DocumentRepository;
import fr.mossaab.docmultilang.repository.DocumentTypeRepository;
import fr.mossaab.docmultilang.repository.LanguageRepository;
import fr.mossaab.docmultilang.service.DocumentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
@DisplayName("Юнит‑тесты для DocumentService")
class DocumentServiceTest {

    @InjectMocks
    private DocumentService service;

    @Mock
    private DocumentRepository repo;

    @Mock
    private DocumentTypeRepository documentTypeRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Test
    @DisplayName("getDocument: возвращает документ если он найден")
    void getDocument_whenExists_returnsDocument() {
        Document document = Document.builder()
                .type(DocumentType.builder().code("offer").name("Оферта").build())
                .language(Language.builder().code("ru").name("Русский").build())
                .active(true)
                .version(1)
                .build();
        when(repo.findByType_CodeAndLanguage_CodeAndActiveTrue("offer", "ru"))
                .thenReturn(Optional.of(document));

        DocumentResponseDto dto = service.getDocument("offer", "ru");

        assertNotNull(dto);
        assertEquals("Оферта", dto.getTypeName());
        assertEquals("Русский", dto.getLanguageName());
        verify(repo).findByType_CodeAndLanguage_CodeAndActiveTrue("offer", "ru");
    }

    @Test
    @DisplayName("getDocument: генерирует исключение если не найден документ")
    void getDocument_whenNotFound_throwsException() {
        when(repo.findByType_CodeAndLanguage_CodeAndActiveTrue("eula", "en"))
                .thenReturn(Optional.empty());

        assertThrows(DocumentNotFoundException.class, () -> service.getDocument("eula", "en"));
        verify(repo).findByType_CodeAndLanguage_CodeAndActiveTrue("eula", "en");
    }

    @Test
    @DisplayName("listAvailableDocuments: корректно группирует по типу и языкам")
    void listAvailableDocuments_returnsGroupedByType() {
        DocumentType invoiceType = DocumentType.builder().code("rules").name("Правила").build();
        DocumentType actType = DocumentType.builder().code("offer").name("Оферта").build();
        Language ru = Language.builder().code("ru").build();
        Language en = Language.builder().code("en").build();

        Document doc1 = Document.builder().type(invoiceType).language(ru).active(true).build();
        Document doc2 = Document.builder().type(invoiceType).language(en).active(true).build();
        Document doc3 = Document.builder().type(actType).language(ru).active(true).build();

        when(repo.findAllByActiveTrue()).thenReturn(List.of(doc1, doc2, doc3));

        List<DocumentTypeListDto> result = service.listAvailableDocuments();
        assertEquals(2, result.size());

        Optional<DocumentTypeListDto> invoiceDto = result.stream().filter(d -> d.getType().equals("rules")).findFirst();
        assertTrue(invoiceDto.isPresent());
        assertTrue(invoiceDto.get().getLanguages().containsAll(List.of("ru", "en")));

        Optional<DocumentTypeListDto> actDto = result.stream().filter(d -> d.getType().equals("offer")).findFirst();
        assertTrue(actDto.isPresent());
        assertTrue(actDto.get().getLanguages().contains("ru"));
    }

    @Test
    @DisplayName("saveNewVersion: сохраняет новую версию и деактивирует старые")
    void saveNewVersion_whenActiveExists_deactivatesOldAndSavesNew() {
        String typeCode = "invoice";
        String langCode = "ru";

        DocumentType type = DocumentType.builder().code(typeCode).name("Счёт").build();
        Language lang = Language.builder().code(langCode).name("Русский").build();

        Document old1 = Document.builder()
                .id(1L)
                .type(type)
                .language(lang)
                .active(true)
                .version(1)
                .content("old-content")
                .build();
        Document old2 = Document.builder()
                .id(2L)
                .type(type)
                .language(lang)
                .active(false)
                .version(0)
                .content("very-old")
                .build();

        DocumentCreateRequest req = DocumentCreateRequest.builder()
                .content("new-content")
                .typeCode(typeCode)
                .langCode(langCode)
                .build();

        Document saved = Document.builder()
                .type(type)
                .language(lang)
                .active(true)
                .version(2)
                .content("new-content")
                .build();

        when(documentTypeRepository.findByCode(typeCode)).thenReturn(Optional.of(type));
        when(languageRepository.findByCode(langCode)).thenReturn(Optional.of(lang));
        when(repo.findAllByType_CodeAndLanguage_Code(typeCode, langCode)).thenReturn(List.of(old1, old2));
        when(repo.save(any(Document.class))).thenReturn(saved);

        DocumentResponseDto result = service.saveNewVersion(req);

        assertEquals("Счёт", result.getTypeName());
        assertEquals("Русский", result.getLanguageName());
        assertEquals(2, result.getVersion());
        // дважды вызов сейв при деактивации и новом сохранении
        verify(repo, times(2)).save(any(Document.class));
        // корректная деактивация первого документа
        assertFalse(old1.isActive());
        assertTrue(saved.isActive());
    }

    @Test
    @DisplayName("saveNewVersion: выбрасывает ошибку если тип не найден")
    void saveNewVersion_whenTypeNotFound_throwsException() {
        String typeCode = "contract";
        String langCode = "en";
        DocumentCreateRequest req = DocumentCreateRequest.builder().typeCode(typeCode).langCode(langCode).build();
        when(documentTypeRepository.findByCode(typeCode)).thenReturn(Optional.empty());
        assertThrows(DocumentNotFoundException.class, () -> service.saveNewVersion(req));
    }

    @Test
    @DisplayName("saveNewVersion: выбрасывает ошибку если язык не найден")
    void saveNewVersion_whenLangNotFound_throwsException() {
        String typeCode = "contract";
        String langCode = "en";
        DocumentType type = DocumentType.builder().code(typeCode).name("Контракт").build();
        DocumentCreateRequest req = DocumentCreateRequest.builder().typeCode(typeCode).langCode(langCode).build();
        when(documentTypeRepository.findByCode(typeCode)).thenReturn(Optional.of(type));
        when(languageRepository.findByCode(langCode)).thenReturn(Optional.empty());
        assertThrows(DocumentNotFoundException.class, () -> service.saveNewVersion(req));
    }

    @Test
    @DisplayName("saveNewVersion: создаёт версию с номером 1 если раньше не было версий")
    void saveNewVersion_whenNoPrevVersion_startsFrom1() {
        String typeCode = "act";
        String langCode = "en";
        DocumentType type = DocumentType.builder().code(typeCode).name("Акт").build();
        Language lang = Language.builder().code(langCode).name("Английский").build();

        DocumentCreateRequest req = DocumentCreateRequest.builder()
                .content("first")
                .typeCode(typeCode)
                .langCode(langCode)
                .build();

        Document saved = Document.builder()
                .type(type)
                .language(lang)
                .active(true)
                .version(1)
                .content("first")
                .build();

        when(documentTypeRepository.findByCode(typeCode)).thenReturn(Optional.of(type));
        when(languageRepository.findByCode(langCode)).thenReturn(Optional.of(lang));
        when(repo.findAllByType_CodeAndLanguage_Code(typeCode, langCode)).thenReturn(List.of());
        when(repo.save(any(Document.class))).thenReturn(saved);

        DocumentResponseDto result = service.saveNewVersion(req);

        assertEquals(1, result.getVersion());
    }
}
