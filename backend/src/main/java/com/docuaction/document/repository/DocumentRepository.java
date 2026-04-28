package com.docuaction.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.docuaction.document.entity.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}

