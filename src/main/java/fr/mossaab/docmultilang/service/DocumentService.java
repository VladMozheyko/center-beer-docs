package fr.mossaab.docmultilang.service;

import fr.mossaab.docmultilang.dto.DocumentTypeListDto;
import fr.mossaab.docmultilang.dto.request.DocumentCreateRequest;
import fr.mossaab.docmultilang.dto.response.DocumentResponseDto;
import fr.mossaab.docmultilang.entity.Document;
import fr.mossaab.docmultilang.entity.DocumentType;
import fr.mossaab.docmultilang.entity.Language;
import fr.mossaab.docmultilang.exception.DocumentNotFoundException;
import fr.mossaab.docmultilang.mapper.EntityToDtoMapper;
import fr.mossaab.docmultilang.repository.DocumentRepository;
import fr.mossaab.docmultilang.repository.DocumentTypeRepository;
import fr.mossaab.docmultilang.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с документами.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository repo;
    private final DocumentTypeRepository documentTypeRepository;
    private final LanguageRepository languageRepository;

    /**
     * Возвращает активный документ для заданного типа и языка.
     * Поиск ведётся по коду типа (type) и коду языка (lang) у документов,
     * где активный флаг (active) равен true.
     * @param type код типа документа
     * @param lang код языка документа
     * @return {@link  DocumentResponseDto}, представляющий найденный активный документ
     * @throws DocumentNotFoundException если документ не найден по указанным type и lang
     */
    public DocumentResponseDto getDocument(String type, String lang) {
        return repo.findByType_CodeAndLanguage_CodeAndActiveTrue(type, lang)
                .map(EntityToDtoMapper::toDto)
                .orElseThrow(() -> new DocumentNotFoundException("Документ не найден: " + type + " / " + lang));
    }

    /**
     * Возвращает список доступных документов, сгруппированных по типу.
     * Для каждого типа формируется {@link DocumentTypeListDto}, содержащий код типа
     * и список уникальных кодов языков, для которых существует активный документ данного типа.
     * @return список доступных документов с разбивкой по типу и языкам
     */
    public List<DocumentTypeListDto> listAvailableDocuments() {
        List<Document> all = repo.findAllByActiveTrue();
        return all.stream()
                .collect(Collectors.groupingBy(Document::getType))
                .entrySet().stream()
                .map(e -> new DocumentTypeListDto(
                        e.getKey().getCode(),
                        e.getValue().stream().map(document -> document.getLanguage().getCode()).distinct().toList()
                )).toList();
    }

    /**
     * Сохраняет новую версию документа.
     *  Выполняется в рамках транзакции: сначала создаётся новая активная версия
     * для данного типа и языка, затем деактивируются (при необходимости) старые версии.
     * Если указанный тип или язык не найдены, генерируется DocumentNotFoundException.
     * @param dto данные новой версии документа (содержат typeCode, langCode, content, version и пр.)
     * @return {@link  DocumentResponseDto}, представляющий сохранённую версию документа
     * @throws DocumentNotFoundException если указанный тип или язык не найден */
    @Transactional
    public DocumentResponseDto saveNewVersion(DocumentCreateRequest dto) {
        // Проверяем существование типа и языка
        DocumentType type = documentTypeRepository.findByCode(dto.getTypeCode())
                .orElseThrow(() -> new DocumentNotFoundException("Не найден тип документа с кодом '" + dto.getTypeCode() +"'."));
        Language language = languageRepository.findByCode(dto.getLangCode())
                .orElseThrow(() -> new DocumentNotFoundException("Не найден язык с кодом '" + dto.getLangCode() + "'."));

        // Собираем все записи для пары typeCode/langCode (независимо от активной)
        List<Document> existing = repo.findAllByType_CodeAndLanguage_Code(dto.getTypeCode(), dto.getLangCode());

        // Деактивируем все существующие записи (если они активны)
        int counterDocumentsDeactivated = 0;
        Integer maxVersion = 0;
        for (Document d : existing) {
            if (d.isActive()) {
                d.setActive(false);
                repo.save(d);
                counterDocumentsDeactivated++;
                if (d.getVersion() > maxVersion) maxVersion = d.getVersion();
            }
        }
        if (counterDocumentsDeactivated > 0) {
            log.info("Деактивировано {} версий документа {}", counterDocumentsDeactivated, dto.getTypeCode());
        }

        // Создаём новую версию
        Document doc = new Document();
        doc.setType(type);
        doc.setLanguage(language);
        doc.setActive(true);
        doc.setContent(dto.getContent());
        doc.setVersion(++maxVersion);
        Document savedDocument = repo.save(doc);

        log.info("Документ {}-{} версия {} сохранен", savedDocument.getType().getName(), savedDocument.getLanguage().getName(), savedDocument.getVersion());
        return EntityToDtoMapper.toDto(savedDocument);
    }
}