package fr.mossaab.docmultilang.dto;

import fr.mossaab.docmultilang.entity.FileData;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileDataDTO {
    private Long id;
    private String name;
    private String originalName;
    private String type;
    private Long size;
    private LocalDateTime uploadedAt;

    public static FileDataDTO fromFileData(FileData fileData) {
        FileDataDTO dto = new FileDataDTO();
        dto.id = fileData.getId();
        dto.name = fileData.getName();
        dto.originalName = fileData.getOriginalName();
        dto.type = fileData.getType();
        dto.size = fileData.getSize();
        dto.uploadedAt = fileData.getCreatedAt();
        return dto;
    }
}
