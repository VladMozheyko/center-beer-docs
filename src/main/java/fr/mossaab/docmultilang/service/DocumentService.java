package fr.mossaab.docmultilang.service;


import fr.mossaab.docmultilang.dto.DocumentDto;
import fr.mossaab.docmultilang.dto.DocumentTypeListDto;
import fr.mossaab.docmultilang.entity.Document;
import fr.mossaab.docmultilang.entity.DocumentType;
import fr.mossaab.docmultilang.entity.Language;
import fr.mossaab.docmultilang.repository.DocumentRepository;
import fr.mossaab.docmultilang.repository.DocumentTypeRepository;
import fr.mossaab.docmultilang.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository repo;
    private final DocumentTypeRepository documentTypeRepository;
    private final LanguageRepository languageRepository;

    public DocumentDto getDocument(String type, String lang) {
        return repo.findByType_CodeAndLanguage_CodeAndActiveTrue(type, lang)
                .map(DocumentDto::fromEntity)
                .orElseThrow(() -> new NoSuchElementException("Документ не найден: " + type + " / " + lang));
    }

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

    public void saveNewVersion(DocumentDto dto) {
        // 1. Удалить старую активную версию
        repo.findByType_CodeAndLanguage_CodeAndActiveTrue(dto.getTypeCode(), dto.getLangCode())
                .ifPresent(doc -> {
                    doc.setActive(false);
                    repo.save(doc);
                });

        // 2. Сохранить новую
        Document doc = new Document();
        DocumentType type = documentTypeRepository.findByCode(dto.getTypeCode())
                .orElseThrow(() -> new IllegalArgumentException("Тип документа '%s' не найден".formatted(dto.getTypeCode())));

        Language language = languageRepository.findByCode(dto.getLangCode()).orElseThrow(
                () -> new IllegalArgumentException("Тип документа '%s' не найден".formatted(dto.getTypeCode())));

        doc.setType(type);
        doc.setLanguage(language);
        doc.setActive(true);
        doc.setContent(dto.getContent());
        doc.setVersion(dto.getVersion());
        repo.save(doc);


        log.info("Документ {}-{} версия {} сохранен", dto.getTypeCode(), dto.getLangCode(), dto.getVersion());
    }
}