package fr.mossaab.docmultilang.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "file")
@Data
public class FileUploadConfig {
    private List<String> allowedExtensions;
    private List<String> allowedMimeTypes;
}
