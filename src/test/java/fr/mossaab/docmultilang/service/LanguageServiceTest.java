package fr.mossaab.docmultilang.service;

import fr.mossaab.docmultilang.dto.response.LanguageResponseDto;
import fr.mossaab.docmultilang.entity.Language;
import fr.mossaab.docmultilang.exception.ConflictException;
import fr.mossaab.docmultilang.repository.LanguageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Юнит-тесты для сервиса LanguageService")
class LanguageServiceTest {

    @InjectMocks
    private LanguageService service;

    @Mock
    private LanguageRepository repository;

    @Test
    @DisplayName("createNew: когда язык не найден, сохраняет новый")
    public void createNewWhenLanguageNotExistThenCreateNewLanguage() {
        String code = "en";
        String name = "English";

        Language language = Language.builder().code(code).name(name).build();

        when(repository.findByCode(code)).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(language);

        LanguageResponseDto responseDto = service.createNew(code, name);

        assertEquals(name, responseDto.getName());
        verify(repository, times(1)).save(any(Language.class));
    }

    @Test
    @DisplayName("createNew: когда язык найден бросает исключение ConflictException")
    public void createNewWhenLanguageExistThenReturnConflictException() {
        String code = "en";
        String name = "English";

        Language language = Language.builder().code(code).name(name).build();

        when(repository.findByCode(code)).thenReturn(Optional.of(language));

        assertThrows(ConflictException.class, () -> service.createNew(code, name));
        verify(repository, times(1)).findByCode(code);
        verify(repository, times(0)).save(any());
    }

    @Test
    @DisplayName("getAll: Возвращает список всех языков или пустой лист")
    public void getAllReturnEmptyList() {
        Language language = Language.builder().code("en").name("English").build();

        when(repository.findAll()).thenReturn(List.of(language));

        List<Language> list= service.getAll();

        assertFalse(list.isEmpty());
        assertEquals(list.size(), 1);
        Language lang = list.getFirst();
        assertEquals(lang.getCode(), "en");
        assertEquals(lang.getName(), "English");
    }

    @Test
    @DisplayName("getAll: если в базе нет языков, возвращает пустой список")
    public void getAllWhenNoLanguagesReturnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());
        List<Language> result = service.getAll();
        assertTrue(result.isEmpty());
    }
}