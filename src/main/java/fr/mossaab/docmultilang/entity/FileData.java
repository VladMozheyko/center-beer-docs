package fr.mossaab.docmultilang.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Сущность для хранения данных файла.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "file_data")
public class FileData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "уникальное имя файла (без расширения)")
    private String name;

    @Schema(description = "исходное имя файла (загруженное пользователем)")
    private String originalName;

    @Schema(description = "расширение: '.pdf', '.png'" )
    private String type;

    @Schema(description = "MIME-тип: 'application/pdf', 'image/png'")
    private String mimeType;

    @Schema(description = "абсолютный или относительный путь к файлу")
    private String filePath;

    @Schema(description = "размер в байтах")
    private Long size;

    @Schema(description = "дата создания")
    private LocalDateTime createdAt;
}
