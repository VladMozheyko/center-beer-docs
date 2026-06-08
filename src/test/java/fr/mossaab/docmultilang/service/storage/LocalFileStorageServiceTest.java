package fr.mossaab.docmultilang.service.storage;

import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Юнит тесты для LocalFileStorageService")
class LocalFileStorageServiceTest {

    private LocalFileStorageService storageService;
    private final String tmpDir = System.getProperty("java.io.tmpdir") + "/test-file-storage/";

    @BeforeEach
    void setUp() {
        storageService = new LocalFileStorageService();
        ReflectionTestUtils.setField(storageService, "basePath", tmpDir);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Удаляем все созданные тестовые файлы и каталоги
        Path baseDir = Paths.get(tmpDir);
        if (Files.exists(baseDir)) {
            Files.walk(baseDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try { Files.delete(path); } catch (IOException ignore) {}
                    });
        }
    }

    @Test
    @DisplayName("saveFile: успешно сохраняет файл и возвращает относительный путь")
    void saveFile_validFile_success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "hello-world".getBytes()
        );
        String resultPath = storageService.saveFile(file, "testdoc", ".pdf");

        assertTrue(resultPath.startsWith("documents"));
        // Убедиться файл реально теперь существует
        Path absPath = Paths.get(tmpDir).resolve(resultPath);
        assertTrue(Files.exists(absPath));
        assertEquals("hello-world".length(), Files.size(absPath));
    }

    @Test
    @DisplayName("saveFile: выбрасывает исключение при пустом расширении")
    void saveFile_emptyExtension_throws() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.bin", "application/octet-stream", "1234".getBytes()
        );
        assertThrows(IllegalArgumentException.class,
                () -> storageService.saveFile(file, "f", null));
    }

    @Test
    @DisplayName("normalizeExtension: нормализует расширение с точкой и без")
    void normalizeExtension_variousCases() {
        assertEquals("pdf", ReflectionTestUtils.invokeMethod(storageService, "normalizeExtension", ".pdf"));
        assertEquals("pdf", ReflectionTestUtils.invokeMethod(storageService, "normalizeExtension", "pdf"));
        assertEquals("png", ReflectionTestUtils.invokeMethod(storageService, "normalizeExtension", ".PNG"));
    }

    @Test
    @DisplayName("normalizeExtension: выбрасывает если extension пустой")
    void normalizeExtension_empty_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(storageService, "normalizeExtension", ""));
        assertThrows(IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(storageService, "normalizeExtension", (String) null));
    }

    @Test
    @DisplayName("getDirectoryNameByMimeType: корректно определяет каталог для MIME‑типа")
    void getDirectoryNameByMimeType_various() {
        assertEquals("images", ReflectionTestUtils.invokeMethod(storageService, "getDirectoryNameByMimeType", "image/png"));
        assertEquals("documents", ReflectionTestUtils.invokeMethod(storageService, "getDirectoryNameByMimeType", "application/pdf"));
        assertEquals("text-files", ReflectionTestUtils.invokeMethod(storageService, "getDirectoryNameByMimeType", "text/plain"));
        assertEquals("archives", ReflectionTestUtils.invokeMethod(storageService, "getDirectoryNameByMimeType", "application/zip"));
        assertEquals("others", ReflectionTestUtils.invokeMethod(storageService, "getDirectoryNameByMimeType", "foo/bar"));
        assertEquals("others", ReflectionTestUtils.invokeMethod(storageService, "getDirectoryNameByMimeType", (String) null));
    }

    @Test
    @DisplayName("generateUniqueFileName: всегда возвращает уникальное имя с расширением")
    void generateUniqueFileName_generatesUnique() {
        String one = ReflectionTestUtils.invokeMethod(storageService, "generateUniqueFileName", "docu", "pdf");
        String two = ReflectionTestUtils.invokeMethod(storageService, "generateUniqueFileName", "docu", "pdf");
        assertNotEquals(one, two);
        assert one != null;
        assertTrue(one.startsWith("docu_"));
        assertTrue(one.endsWith(".pdf"));
    }

    @Test
    @DisplayName("getRelativePath: возвращает только разницу между basePath и абсолютным путём")
    void getRelativePath_calculate() {
        Path abs = Paths.get(tmpDir, "documents/abc.pdf");
        String rel = storageService.getRelativePath(abs);
        assertEquals(Paths.get("documents/abc.pdf").toString(), rel);
    }
}
