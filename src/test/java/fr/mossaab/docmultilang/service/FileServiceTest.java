package fr.mossaab.docmultilang.service;

import static org.junit.jupiter.api.Assertions.*;

import fr.mossaab.docmultilang.config.FileUploadConfig;
import fr.mossaab.docmultilang.entity.FileData;
import fr.mossaab.docmultilang.exception.BadRequestException;
import fr.mossaab.docmultilang.exception.NotFoundException;
import fr.mossaab.docmultilang.repository.FileDataRepository;
import fr.mossaab.docmultilang.service.FileService;
import fr.mossaab.docmultilang.service.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
@DisplayName("Юнит‑тесты для FileService")
class FileServiceTest {

    @InjectMocks
    private FileService service;

    @Mock
    private FileDataRepository fileDataRepository;

    @Mock
    private FileUploadConfig fileUploadConfig;

    @Mock
    private FileStorageService fileStorageService;

    private final String extension = ".pdf";
    private final String mimeType = "application/pdf";

    @BeforeEach
    void init() {
        service.setFileStorageService(fileStorageService);

    }

    @Test
    @DisplayName("saveFile: cохраняет файл при валидных параметрах")
    void saveFile_whenValidFile_thenSavesAndReturnsFileData() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "file.pdf", mimeType, "abc".getBytes());
        String filePath = "test/uuid.pdf";
        String uuidName = "uuid-test";
        FileData fileData = FileData.builder()
                .name(uuidName)
                .originalName("file.pdf")
                .type(extension)
                .mimeType(mimeType)
                .filePath(filePath)
                .size((long) file.getBytes().length)
                .createdAt(LocalDateTime.now())
                .build();

        when(fileStorageService.saveFile(any(), any(), any())).thenReturn(filePath);
        when(fileDataRepository.save(any())).thenReturn(fileData);

        when(fileUploadConfig.getAllowedExtensions()).thenReturn(List.of(".pdf", ".png"));
        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf", "image/png"));

        FileData result = service.saveFile(file);

        assertEquals("file.pdf", result.getOriginalName());
        assertEquals(".pdf", result.getType());
        assertEquals(filePath, result.getFilePath());
        verify(fileDataRepository, times(1)).save(any(FileData.class));
    }

    @Test
    @DisplayName("saveFile: выбрасывает ошибку, если файл пустой")
    void saveFile_whenFileIsNullOrEmpty_thenThrowException() {
        BadRequestException ex1 = assertThrows(BadRequestException.class, () -> service.saveFile(null));
        assertTrue(ex1.getMessage().contains("Файл не может быть пустым"));
        MockMultipartFile emptyFile = new MockMultipartFile("file", "doc.pdf", mimeType, new byte[0]);
        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> service.saveFile(emptyFile));
        assertTrue(ex2.getMessage().contains("Файл не может быть пустым"));
    }

    @Test
    @DisplayName("saveFile: валидация запрещенного расширения выбрасывает BadRequestException")
    void saveFile_whenWrongExtension_thenThrowException() {
        MockMultipartFile file = new MockMultipartFile("file", "img.exe", "application/octet-stream", "123".getBytes());
        when(fileUploadConfig.getAllowedExtensions()).thenReturn(List.of(".pdf"));
        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.saveFile(file));
        assertTrue(ex.getMessage().contains("Поддерживаются только"));
    }

    @Test
    @DisplayName("saveFile: валидация запрещенного MIME-типа выбрасывает BadRequestException")
    void saveFile_whenWrongMimeType_thenThrowException() {
        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("image/png", "application/pdf"));
        when(fileUploadConfig.getAllowedExtensions()).thenReturn(List.of(".pdf", ".png"));

        MockMultipartFile file = new MockMultipartFile("file", "file.pdf", "image/gif", "123".getBytes());
        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.saveFile(file));
        assertTrue(ex.getMessage().contains("MIME-тип не поддерживается"));
    }

    @Test
    @DisplayName("getFileDataByName: находит и возвращает FileData (по имени и типу)")
    void getFileDataByName_whenExists_returnsFileData() {
        FileData fileData = FileData.builder().name("doc").type(".pdf").build();
        when(fileDataRepository.findByNameAndType(eq("doc"), eq(".pdf"))).thenReturn(Optional.of(fileData));
        FileData found = service.getFileDataByName("doc", ".pdf");
        assertEquals("doc", found.getName());
    }

    @Test
    @DisplayName("getFileDataByName: выбрасывает NotFoundException, если не найден")
    void getFileDataByName_whenNotFound_throwsNotFound() {
        when(fileDataRepository.findByNameAndType(anyString(), anyString())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getFileDataByName("no", ".pdf"));
    }

    @Test
    @DisplayName("getFileDataById: находит и возвращает FileData (по id и типу)")
    void getFileDataById_whenExists_returnsFileData() {
        FileData fileData = FileData.builder().id(1L).type(".pdf").build();
        when(fileDataRepository.findById(eq(1L))).thenReturn(Optional.of(fileData));
        FileData found = service.getFileDataById(1L, ".pdf");
        assertEquals(1L, found.getId());
    }

    @Test
    @DisplayName("getFileDataById: выбрасывает NotFoundException, если не найден")
    void getFileDataById_whenNotFound_throwsNotFound() {
        when(fileDataRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getFileDataById(77L, ".pdf"));
    }

    @Test
    @DisplayName("getFileDataById: выбрасывает BadRequestException, если тип не совпадает")
    void getFileDataById_whenTypeMismatch_throwsBadRequest() {
        FileData fileData = FileData.builder().id(2L).type(".pdf").build();
        when(fileDataRepository.findById(eq(2L))).thenReturn(Optional.of(fileData));
        assertThrows(BadRequestException.class, () -> service.getFileDataById(2L, ".png"));
    }

    @Test
    @DisplayName("asResource: успешно возвращает ресурс, если файл найден и доступен")
    void asResource_whenFileExistsAndReadable_returnsResource() throws IOException, NoSuchFieldException, IllegalAccessException {
        FileData fileData = FileData.builder().filePath("docs/file.pdf").build();
        String basePath = System.getProperty("java.io.tmpdir");

        Field field = service.getClass().getDeclaredField("basePath");
        field.setAccessible(true);
        field.set(service, basePath);

        java.nio.file.Path filePath = Paths.get(basePath, "docs/file.pdf");
        java.nio.file.Files.createDirectories(filePath.getParent());
        java.nio.file.Files.write(filePath, "test".getBytes());

        Resource resource = service.asResource(fileData);
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());

        java.nio.file.Files.deleteIfExists(filePath); // cleanup
    }

    @Test
    @DisplayName("asResource: выбрасывает NotFoundException, если файл не существует или не читается")
    void asResource_whenFileNotExists_throwsNotFound() throws IllegalAccessException, NoSuchFieldException {
        FileData fileData = FileData.builder().filePath("not/exist/file.pdf").build();
        String basePath = System.getProperty("java.io.tmpdir");

        Field field = service.getClass().getDeclaredField("basePath");
        field.setAccessible(true);
        field.set(service, basePath);

        assertThrows(NotFoundException.class, () -> service.asResource(fileData));
    }

    @Test
    @DisplayName("getExtension: выбрасывает BadRequestException, если нет расширения")
    void getExtension_whenNoDot_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> {
            // Вызовет private метод через Reflection (или можно вызвать saveFile с некорректным именем)
            service.saveFile(new MockMultipartFile("file", "file", mimeType, "xyz".getBytes()));
        });
    }

    @Test
    @DisplayName("getFiles: если type передан, вызывает findAllByType; иначе findAll")
    void getFiles_callCorrectRepositoryMethod() {
        Page<FileData> mockPage = new PageImpl<>(List.of(FileData.builder().build()));
        when(fileDataRepository.findAllByType(eq(".pdf"), any(Pageable.class))).thenReturn(mockPage);
        when(fileDataRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        Pageable pageable = Pageable.ofSize(5);

        // запрос с типом
        Page<FileData> result1 = service.getFiles(".pdf", pageable);
        assertEquals(1, result1.getContent().size());
        verify(fileDataRepository, times(1)).findAllByType(".pdf", pageable);

        // запрос без типа
        Page<FileData> result2 = service.getFiles(null, pageable);
        assertEquals(1, result2.getContent().size());
        verify(fileDataRepository, times(1)).findAll(pageable);
    }
}
