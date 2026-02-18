package fr.mossaab.docmultilang.repository;

import fr.mossaab.docmultilang.entity.DocumentType;
import fr.mossaab.docmultilang.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {
    Optional<Language> findByCode(String code);

}
