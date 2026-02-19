package fr.mossaab.docmultilang.service;

import fr.mossaab.docmultilang.dto.response.LanguageResponseDto;
import fr.mossaab.docmultilang.entity.Language;
import fr.mossaab.docmultilang.exception.ConflictException;
import fr.mossaab.docmultilang.mapper.EntityToDtoMapper;
import fr.mossaab.docmultilang.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления языками документов.
 * <p>
 * Позволяет создавать новые языки и получать список всех поддерживаемых языков.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LanguageService {

    private final LanguageRepository languageRepository;

    /**
     * Создаёт новый язык документа с указанным кодом и именем.
     * <p>
     * Если язык с указанным кодом уже существует, выбрасывается ConflictException.
     * @param code уникальный код языка
     * @param name отображаемое имя языка
     * @return {@link LanguageResponseDto} представляющий созданный язык
     * @throws ConflictException если язык с указанным кодом уже существует
     */
    public LanguageResponseDto createNew(String code, String name) {

        Optional<Language> language = languageRepository.findByCode(code);

        if (language.isPresent()) {
            throw new ConflictException("Язык документа %s существует".formatted(code));
        }

        Language newLanguage = new Language();
        newLanguage.setCode(code);
        newLanguage.setName(name);
        Language saveLanguage = languageRepository.save(newLanguage);
        log.info("Успешно сохранен новый язык '{}'", code);
        return EntityToDtoMapper.toDto(saveLanguage);
    }

    /**
     * Возвращает список всех языков.
     *
     * @return список всех Language
     */
    public List<Language> getAll() {
        return languageRepository.findAll();
    }
}