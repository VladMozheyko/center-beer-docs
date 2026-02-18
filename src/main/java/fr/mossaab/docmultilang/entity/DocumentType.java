package fr.mossaab.docmultilang.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_types", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Data
public class DocumentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code; // "rules", "offer", "eula"

    @Column(nullable = false)
    private String name; // "Правила", "Оферта"

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
