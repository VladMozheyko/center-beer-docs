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
public class DocumentTypeService {

    private final DocumentTypeRepository documentTypeRepository;

    public boolean createNew(String type, String name) {
        DocumentType documentType = new DocumentType();
        documentType.setCode(type);
        documentType.setName(name);
        documentTypeRepository.save(documentType);
        return true;
    }
}