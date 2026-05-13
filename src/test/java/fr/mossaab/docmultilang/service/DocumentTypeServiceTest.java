package fr.mossaab.docmultilang.service;

import fr.mossaab.docmultilang.dto.response.DocumentTypeResponseDto;
import fr.mossaab.docmultilang.entity.DocumentType;
import fr.mossaab.docmultilang.exception.ConflictException;
import fr.mossaab.docmultilang.repository.DocumentTypeRepository;
import fr.mossaab.docmultilang.service.DocumentTypeService;
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
@DisplayName("Юнит-тесты для сервиса DocumentTypeService")
class DocumentTypeServiceTest {

    @InjectMocks
    private DocumentTypeService service;

    @Mock
    private DocumentTypeRepository repository;

    @Test
    @DisplayName("createNew: если тип не найден — создаёт новый документ")
    public void createNewWhenDocumentTypeNotExistThenCreateNewType() {
        String code = "info";
        String name = "Информация";

        DocumentType documentType = DocumentType.builder().code(code).name(name).build();

        when(repository.findByCode(code)).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(documentType);

        DocumentTypeResponseDto responseDto = service.createNew(code, name);

        assertEquals(name, responseDto.getName());
        verify(repository, times(1)).save(any(DocumentType.class));
    }

    @Test
    @DisplayName("createNew: если тип найден — выбрасывает ConflictException")
    public void createNewWhenDocumentTypeExistThenThrowConflictException() {
        String code = "info";
        String name = "Информация";

        DocumentType documentType = DocumentType.builder().code(code).name(name).build();

        when(repository.findByCode(code)).thenReturn(Optional.of(documentType));

        ConflictException ex = assertThrows(ConflictException.class, () -> service.createNew(code, name));
        assertTrue(ex.getMessage().contains(code));
        verify(repository, times(1)).findByCode(code);
        verify(repository, times(0)).save(any());
    }

    @Test
    @DisplayName("getAll: возвращает все типы или пустой список")
    public void getAllWhenTypesExistReturnList() {
        DocumentType documentType = DocumentType.builder().code("info").name("Информация").build();

        when(repository.findAll()).thenReturn(List.of(documentType));

        List<DocumentType> result = service.getAll();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("info", result.getFirst().getCode());
        assertEquals("Информация", result.getFirst().getName());
    }

    @Test
    @DisplayName("getAll: если ни одного типа нет, вернёт пустой список")
    public void getAllWhenNoTypesReturnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<DocumentType> result = service.getAll();

        assertTrue(result.isEmpty());
    }
}
