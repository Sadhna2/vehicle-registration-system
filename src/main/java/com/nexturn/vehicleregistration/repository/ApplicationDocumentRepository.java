package com.nexturn.vehicleregistration.repository;

import com.nexturn.vehicleregistration.entity.ApplicationDocument;
import com.nexturn.vehicleregistration.enums.DocumentType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationDocumentRepository extends JpaRepository<ApplicationDocument, Long> {

    List<ApplicationDocument> findByApplicationApplicationRefNoOrderByDocumentTypeAsc(String ref);

    Optional<ApplicationDocument> findByApplicationApplicationRefNoAndDocumentType(
            String ref, DocumentType type);

    Optional<ApplicationDocument> findByDocumentIdAndApplicationApplicationRefNo(
            Long documentId, String ref);
}
