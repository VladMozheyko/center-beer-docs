package fr.mossaab.docmultilang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentResponseDto {
    private Long id;
    private String typeName;
    private String languageName;
    private Integer version;
    private String content;
    private LocalDateTime createAt;
}
