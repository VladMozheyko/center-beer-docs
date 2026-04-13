package fr.mossaab.docmultilang.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

@Service("localFileStorageService")
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.file-path:data/files/}")
    private String basePath;

    @Override
    public String saveFile(MultipartFile file, String fileName, String extension) throws IOException {
        // Нормализуем расширение: убираем точку и приводим к нижнему регистру
        String normalizedExtension = normalizeExtension(extension);

        // Определяем директорию для сохранения на основе MIME‑типа
        String directoryName = getDirectoryNameByMimeType(file.getContentType());
        Path dir = Paths.get(basePath, directoryName);
        Files.createDirectories(dir);

        // Генерируем уникальное имя файла, чтобы избежать перезаписи
        String uniqueFileName = generateUniqueFileName(fileName, normalizedExtension);
        Path filePath = dir.resolve(uniqueFileName);

        // Сохраняем файл
        file.transferTo(filePath);

        // Возвращаем относительный путь от базового каталога
        return getRelativePath(filePath);
    }

    /**
     * Нормализует расширение файла: убирает начальную точку и приводит к нижнему регистру.
     */
    private String normalizeExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            throw new IllegalArgumentException("Расширение файла не может быть пустым");
        }
        return extension.startsWith(".")
                ? extension.substring(1).toLowerCase(Locale.ROOT)
                : extension.toLowerCase(Locale.ROOT);
    }

    /**
     * Определяет имя директории на основе MIME‑типа файла.
     */
    private String getDirectoryNameByMimeType(String mimeType) {
        if (mimeType == null) {
            return "others";
        }

        if (mimeType.startsWith("image/")) {
            return "images";
        } else if (mimeType.startsWith("application/pdf")) {
            return "documents";
        } else if (mimeType.startsWith("text/")) {
            return "text-files";
        } else if (mimeType.contains("archive") || mimeType.contains("zip")) {
            return "archives";
        } else {
            return "others";
        }
    }

    /**
     * Генерирует уникальное имя файла с использованием UUID, чтобы избежать конфликтов имён.
     */
    private String generateUniqueFileName(String originalName, String extension) {
        String baseName = originalName != null && !originalName.isEmpty()
                ? originalName
                : UUID.randomUUID().toString();
        return baseName + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;
    }

    /**
     * Возвращает относительный путь файла от базового каталога.
     * Пример: если basePath = "data/files/", а filePath = "data/files/images/abc123.png",
     * то вернётся "images/abc123.png".
     */
    public String getRelativePath(Path filePath) {
        Path baseDir = Paths.get(basePath).toAbsolutePath();
        Path absoluteFilePath = filePath.toAbsolutePath();
        return baseDir.relativize(absoluteFilePath).toString();
    }
}
