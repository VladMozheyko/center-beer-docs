package fr.mossaab.docmultilang.service;

import fr.mossaab.docmultilang.config.FileUploadConfig;
import fr.mossaab.docmultilang.entity.FileData;
import fr.mossaab.docmultilang.exception.BadRequestException;
import fr.mossaab.docmultilang.exception.NotFoundException;
import fr.mossaab.docmultilang.repository.FileDataRepository;
import fr.mossaab.docmultilang.service.storage.FileStorageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileDataRepository fileDataRepository;
    private final FileUploadConfig fileUploadConfig;

    private FileStorageService fileStorageService;

    /* Конструктор для будущих реализаций хранения файлов
    * localFileStorageService - имя реализации хранения реализующий интерфейс FileStorageService
    */
    @Autowired
    @Qualifier("localFileStorageService")
    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public FileData saveFile(MultipartFile file) throws IOException {;
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Файл не может быть пустым.");
        }
        String originalName = file.getOriginalFilename();
        String contentType = file.getContentType();
        String extension = getExtension(originalName);

        validFile(extension, contentType);

        String name = UUID.randomUUID().toString();
        String filePath = fileStorageService.saveFile(file, name, extension);

        FileData fileData = FileData.builder()
                .name(name)
                .originalName(originalName)
                .type(extension)
                .mimeType(contentType)
                .filePath(filePath)
                .size(file.getSize())
                .createdAt(LocalDateTime.now())
                .build();

        return fileDataRepository.save(fileData);
    }

    // Берёт FileData по имени файла и типу (.pdf/.png), гарантируя строгие параметры!
    public FileData getFileDataByName(String fileName, String expectedType) {
        // Всегда ищем по имени И расширению
        Optional<FileData> fileOpt = fileDataRepository.findByNameAndType(fileName, expectedType.toLowerCase());
        FileData fileData = fileOpt.orElseThrow(
                () -> new NotFoundException("Файл с именем " + fileName + " и типом " + expectedType + " не найден")
        );
        if (!fileData.getType().equalsIgnoreCase(expectedType)) {
            throw new BadRequestException("Тип файла не совпадает с ожидаемым: " + expectedType);
        }
        return fileData;
    }

    // Берёт FileData по id и ожидаемому типу
    public FileData getFileDataById(Long id, String expectedType) {
        FileData fileData = fileDataRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Файл по id " + id + " не найден"));
        if (!fileData.getType().equalsIgnoreCase(expectedType)) {
            throw new BadRequestException("Тип файла не совпадает с ожидаемым: " + expectedType);
        }
        return fileData;
    }

    // Получить Resource по FileData с проверкой доступности файла
    public Resource asResource(FileData fileData) throws IOException {
        Path filePath = Paths.get(fileData.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new NotFoundException("Файл не существует или не доступен для чтения");
        }
        return resource;
    }

    public void validFile(String extension, String mimeType) {
        List<String> allowedExtensions = fileUploadConfig.getAllowedExtensions();
        List<String> allowedMimeTypes = fileUploadConfig.getAllowedMimeTypes();

        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new BadRequestException("Поддерживаются только " + allowedExtensions);
        }
        if (!allowedMimeTypes.contains(mimeType)) {
            throw new BadRequestException("MIME-тип не поддерживается: " + mimeType);
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new BadRequestException("Название файла должно содержать расширение");
        }
        int dotIndex = fileName.lastIndexOf(".");
        return fileName.substring(dotIndex).toLowerCase();
    }

    public Page<FileData> getFiles(String type, Pageable pageable) {
        if (type != null && !type.isBlank()) {
            return fileDataRepository.findAllByType(type, pageable);
        } else {
            return fileDataRepository.findAll(pageable);
        }
    }
}
