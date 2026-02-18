package fr.mossaab.docmultilang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentTypeListDto {
    private String type;
    private List<String> languages;
}
