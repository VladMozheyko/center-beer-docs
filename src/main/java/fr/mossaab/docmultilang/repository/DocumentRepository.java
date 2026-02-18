package fr.mossaab.docmultilang.repository;

import fr.mossaab.docmultilang.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByType_CodeAndLanguage_CodeAndActiveTrue(String typeCode, String langCode);

    List<Document> findAllByActiveTrue();
}
