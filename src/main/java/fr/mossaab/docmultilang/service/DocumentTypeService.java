package fr.mossaab.docmultilang.service;


import fr.mossaab.docmultilang.dto.response.DocumentTypeResponseDto;
import fr.mossaab.docmultilang.entity.DocumentType;
import fr.mossaab.docmultilang.exception.ConflictException;
import fr.mossaab.docmultilang.mapper.EntityToDtoMapper;
import fr.mossaab.docmultilang.repository.DocumentTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления типами документов.
 * Обеспечивает создание новых типов документов и получение всех доступных типов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentTypeService {

    private final DocumentTypeRepository documentTypeRepository;

    /**
     * Создаёт новый тип документа с указанным кодом и именем.
     * Если тип с указанным кодом уже существует, генерируется конфликт (ConflictException).
     * @param code уникальный код типа документа
     * @param name отображаемое имя типа документа
     * @return {@link DocumentTypeResponseDto}, представляющий созданный тип документа
     * @throws ConflictException если тип документа с указанным кодом уже существует
     */
    public DocumentTypeResponseDto createNew(String code, String name) {
        Optional<DocumentType> documentType = documentTypeRepository.findByCode(code);

        if (documentType.isPresent()) {
            throw new ConflictException("Тип документа %s существует".formatted(code));
        }

        DocumentType newDocumentType = new DocumentType();
        newDocumentType.setCode(code);
        newDocumentType.setName(name);
        DocumentType savedDocumentType = documentTypeRepository.save(newDocumentType);
        log.info("Успешно сохранен новый тип документа '{}'", code);
        return EntityToDtoMapper.toDto(savedDocumentType);
    }

    /**
     * Возвращает список всех типов документов.
     *
     * @return список всех DocumentType
     */
    public List<DocumentType> getAll() {
        return documentTypeRepository.findAll();
    }
}