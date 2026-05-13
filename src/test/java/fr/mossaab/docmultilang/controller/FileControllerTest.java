package fr.mossaab.docmultilang.controller;

import fr.mossaab.docmultilang.entity.FileData;
import fr.mossaab.docmultilang.exception.BadRequestException;
import fr.mossaab.docmultilang.service.FileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    FileService fileService;

    @Nested
    @DisplayName("Скачивание файлов")
    class DownloadTests {

        @Test
        @DisplayName("downloadByName: корректно возвращает файл с pdf mime-type и верными headers")
        void downloadByName_returnsPdfResource() throws Exception {
            byte[] fileBytes = "test content".getBytes(StandardCharsets.UTF_8);
            FileData fileData = FileData.builder()
                    .originalName("Документ.pdf")
                    .name("doc_1")
                    .type(".pdf")
                    .mimeType("application/pdf")
                    .filePath("documents/1.pdf")
                    .size((long) fileBytes.length)
                    .build();

            when(fileService.getFileDataByName("doc_1", ".pdf")).thenReturn(fileData);
            when(fileService.asResource(fileData)).thenReturn(new ByteArrayResource(fileBytes));

            mockMvc.perform(get("/api/files/by-name/.pdf/doc_1"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "application/pdf"))
                    .andExpect(header().string("Content-Disposition", containsString("attachment")))
                    .andExpect(content().bytes(fileBytes));
        }

        @Test
        @DisplayName("downloadById: корректно возвращает файл с png mime-type")
        void downloadById_returnsPngResource() throws Exception {
            byte[] fileBytes = new byte[]{1,2,3};
            FileData fileData = FileData.builder()
                    .id(42L)
                    .originalName("Картинка.png")
                    .name("img_42")
                    .type(".png")
                    .mimeType("image/png")
                    .filePath("images/img42.png")
                    .size((long) fileBytes.length)
                    .build();

            when(fileService.getFileDataById(42L, ".png")).thenReturn(fileData);
            when(fileService.asResource(fileData)).thenReturn(new ByteArrayResource(fileBytes));

            mockMvc.perform(get("/api/files/by-id/42/.png"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "image/png"))
                    .andExpect(header().string("Content-Disposition", containsString(".png")))
                    .andExpect(content().bytes(fileBytes));
        }

        @Test
        @DisplayName("downloadByName: отдаёт 400, если расширение не поддерживается")
        void downloadByName_badType_returns400() throws Exception {
            mockMvc.perform(get("/api/files/by-name/.exe/somefile")
                            .param("type", ".exe")
                            .param("fileName", "somefile"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Загрузка файлов")
    class UploadTests {

        @Test
        @DisplayName("uploadFile: успешная загрузка и возвращается DTO с полями")
        void uploadFile_whenValid_returnsFileDataDto() throws Exception {
            MockMultipartFile multipartFile = new MockMultipartFile("file", "document.pdf", "application/pdf", "hello".getBytes());
            FileData fileData = FileData.builder()
                    .originalName("document.pdf")
                    .name("uuid-test")
                    .type(".pdf")
                    .mimeType("application/pdf")
                    .size(5L)
                    .filePath("documents/uuid-test.pdf")
                    .build();

            when(fileService.saveFile(any())).thenReturn(fileData);

            mockMvc.perform(multipart("/api/files/upload")
                            .file(multipartFile))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("uuid-test"))
                    .andExpect(jsonPath("$.type").value(".pdf"))
                    .andExpect(jsonPath("$.originalName").value("document.pdf"));
        }
    }

    @Nested
    @DisplayName("Список файлов с пагинацией")
    class ListFilesTests {

        @Test
        @DisplayName("listFiles: возвращает страницу DTO")
        void listFiles_returnsFileDataDtoPage() throws Exception {
            FileData fileData = FileData.builder()
                    .id(2L)
                    .originalName("test.png")
                    .name("img_2")
                    .type(".png")
                    .mimeType("image/png")
                    .size(15L)
                    .build();

            Page<FileData> page = new PageImpl<>(List.of(fileData));
            when(fileService.getFiles(eq(".png"), any())).thenReturn(page);

            mockMvc.perform(get("/api/files/all")
                            .param("type", ".png")
                            .param("size", "10")
                            .param("page", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].type").value(".png"))
                    .andExpect(jsonPath("$.content[0].originalName").value("test.png"));
        }
    }

    @Nested
    @DisplayName("Private util методы контроллера")
    class PrivateUtilMethodTests {
        @Test
        void getMediaTypeByExtension_whenPdfOrPng_returnsMediaType() {
            FileController ctrl = new FileController(fileService);
            assertEquals(MediaType.APPLICATION_PDF,
                    org.springframework.test.util.ReflectionTestUtils.invokeMethod(ctrl, "getMediaTypeByExtension", ".pdf"));
            assertEquals(MediaType.IMAGE_PNG,
                    org.springframework.test.util.ReflectionTestUtils.invokeMethod(ctrl, "getMediaTypeByExtension", ".png"));
        }

        @Test
        void getMediaTypeByExtension_whenNotSupported_throwsBadRequest() {
            FileController ctrl = new FileController(fileService);
            assertThrows(BadRequestException.class,
                    () -> org.springframework.test.util.ReflectionTestUtils.invokeMethod(ctrl, "getMediaTypeByExtension", ".exe"));
        }

        @Test
        void toAsciiFileName_variousNames() {
            FileController ctrl = new FileController(fileService);
            String name = "Документ 1.pdf";
            String ascii = org.springframework.test.util.ReflectionTestUtils.invokeMethod(ctrl, "toAsciiFileName", name, ".pdf");
            assert ascii != null;
            assertTrue(ascii.matches("^[A-Za-z0-9_.-]+\\.pdf$"));
        }
    }
}
