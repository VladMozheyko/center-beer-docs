package fr.mossaab.docmultilang.repository;

import fr.mossaab.docmultilang.entity.Document;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Document> findAllByType_CodeAndLanguage_Code(String typeCode, String languageCode);

    Optional<Document> findByType_CodeAndLanguage_CodeAndActiveTrue(String typeCode, String langCode);

    List<Document> findAllByActiveTrue();
}
