package fr.mossaab.docmultilang.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "languages", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Data
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 5)
    private String code; // "ru", "en", "de"

    @Column(nullable = false)
    private String name; // "Русский", "English", "Deutsch"

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
