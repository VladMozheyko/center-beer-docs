package fr.mossaab.docmultilang.service.storage;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileStorageService {
    String saveFile(MultipartFile file, String fileName, String extension) throws IOException;
}
