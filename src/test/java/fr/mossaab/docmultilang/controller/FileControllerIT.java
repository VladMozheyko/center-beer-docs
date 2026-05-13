package fr.mossaab.docmultilang.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.mossaab.docmultilang.repository.FileDataRepository;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.util.Objects;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileControllerIT {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    FileDataRepository fileRepo;
    @Autowired
    ObjectMapper objectMapper;

    private final String dataDir = System.getProperty("java.io.tmpdir") + "/test/";

    @BeforeAll
    void prepareFilePath() {
        System.setProperty("app.file-path", dataDir);
        new File(dataDir).mkdirs();
    }

    @AfterAll
    void cleanupDir() throws Exception {
        FileUtils.deleteDirectory(new File(dataDir));
    }

    @AfterEach
    void cleanupDb() {
        fileRepo.deleteAll();
    }

    @Test
    @DisplayName("upload -> download by name -> download by id -> listFiles OK")
    void fileFullFlow_pdf() throws Exception {
        byte[] content = "Test PDF content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", content);

        // Upload
        String json = mockMvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").exists())
                .andReturn().getResponse().getContentAsString();

        String savedName = objectMapper.readTree(json).get("name").asText();

        // Download by name
        mockMvc.perform(get("/api/files/by-name/.pdf/" + savedName))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(content().bytes(content));

        // Download by id
        Long dbFileId = fileRepo.findByNameAndType(savedName, ".pdf").orElseThrow().getId();
        mockMvc.perform(get("/api/files/by-id/" + dbFileId + "/.pdf"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(content));

        // List files
        mockMvc.perform(get("/api/files/all?type=.pdf&size=10&page=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].type").value(".pdf"))
                .andExpect(jsonPath("$.content[0].name").value(savedName));

        // Physical file present
        File savedFile = searchPhysicalFile(dataDir, savedName, ".pdf");
        assertTrue(savedFile != null && savedFile.exists());
    }

    @Test
    @DisplayName("Ошибка — upload with invalid extension")
    void upload_invalidExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "bad.exe", "application/octet-stream", "err".getBytes());
        mockMvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", containsString("BAD_REQUEST")));
    }

    @Test
    @DisplayName("Ошибка — скачивание несуществующего файла")
    void download_nonExistent() throws Exception {
        mockMvc.perform(get("/api/files/by-name/.pdf/fake_name"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("NOT_FOUND")));
    }

    // Вспомогательный метод для поиска файла
    private File searchPhysicalFile(String rootDir, String name, String ext) throws Exception {
        File root = new File(rootDir);
        for (File d : Objects.requireNonNull(root.listFiles())) {
            if (d.isDirectory()) {
                for (File f : Objects.requireNonNull(d.listFiles())) {
                    if (f.getName().startsWith(name) && f.getName().endsWith(ext)) {
                        return f;
                    }
                }
            }
        }
        return null;
    }
}