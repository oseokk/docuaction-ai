package com.docuaction.document.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.docuaction.document.entity.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {

	Page<Document> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

	Optional<Document> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);
}
