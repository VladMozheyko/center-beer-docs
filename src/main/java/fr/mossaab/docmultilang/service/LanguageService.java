package fr.mossaab.docmultilang.service;

import fr.mossaab.docmultilang.entity.Language;
import fr.mossaab.docmultilang.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LanguageService {

    private final LanguageRepository languageRepository;

    public boolean createNew(String type, String name) {
        Language language = new Language();
        language.setCode(type);
        language.setName(name);
        languageRepository.save(language);
        return true;
    }
}
