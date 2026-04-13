package fr.mossaab.docmultilang.controller;

import fr.mossaab.docmultilang.api.FileControllerOpenApi;
import fr.mossaab.docmultilang.dto.FileDataDTO;
import fr.mossaab.docmultilang.entity.FileData;
import fr.mossaab.docmultilang.exception.BadRequestException;
import fr.mossaab.docmultilang.service.FileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController implements FileControllerOpenApi {

    private final FileService fileService;

    @Override
    public ResponseEntity<Resource> downloadByName(String type, String fileName) throws IOException {
        MediaType expectedType = getMediaTypeByExtension(type);
        FileData fileData = fileService.getFileDataByName(fileName, type);
        Resource resource = fileService.asResource(fileData);
        String userFileName = fileData.getOriginalName();
        return ResponseEntity.ok()
                .contentType(expectedType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + userFileName + "\"")
                .body(resource);
    }

    @Override
    public ResponseEntity<Resource> downloadById(Long id, String type) throws IOException {
        MediaType expectedType = getMediaTypeByExtension(type);
        FileData fileData = fileService.getFileDataById(id, type);
        Resource resource = fileService.asResource(fileData);
        String userFileName = fileData.getOriginalName();
        return ResponseEntity.ok()
                .contentType(expectedType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + userFileName + "\"")
                .body(resource);
    }

    @Override
    public Page<FileDataDTO> listFiles(String type, Pageable pageable) {
        Page<FileData> page = fileService.getFiles(type, pageable);
        return page.map(FileDataDTO::fromFileData);
    }

    @Override
    public ResponseEntity<FileDataDTO> uploadFile(MultipartFile file) throws Exception {
        FileData fileData = fileService.saveFile(file);
        FileDataDTO dto = FileDataDTO.fromFileData(fileData);
        return ResponseEntity.ok(dto);
    }

    // Приватный вспомогательный метод контроллера
    private MediaType getMediaTypeByExtension(String extension) {
        if (".pdf".equalsIgnoreCase(extension)) {
            return MediaType.APPLICATION_PDF;
        } else if (".png".equalsIgnoreCase(extension)) {
            return MediaType.IMAGE_PNG;
        } else {
            throw new BadRequestException("Поддерживаются только .pdf и .png");
        }
    }
}