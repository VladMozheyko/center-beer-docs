package fr.mossaab.docmultilang.repository;

import fr.mossaab.docmultilang.entity.FileData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileDataRepository extends JpaRepository<FileData, Long> {
    Page<FileData> findAllByType(String type, Pageable pageable);
    Optional<FileData> findByNameAndType(String name, String type);
}
