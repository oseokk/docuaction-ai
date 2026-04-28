package com.docuaction.document.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.docuaction.document.entity.DocumentField;

public interface DocumentFieldRepository extends JpaRepository<DocumentField, Long> {

	List<DocumentField> findByDocumentIdOrderByIdAsc(Long documentId);

	void deleteByDocumentId(Long documentId);
}

