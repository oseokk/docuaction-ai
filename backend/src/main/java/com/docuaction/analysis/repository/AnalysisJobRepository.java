package com.docuaction.analysis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.docuaction.analysis.entity.AnalysisJob;

public interface AnalysisJobRepository extends JpaRepository<AnalysisJob, Long> {

	Optional<AnalysisJob> findTopByDocumentIdOrderByCreatedAtDesc(Long documentId);

	long countByDocumentId(Long documentId);
}

