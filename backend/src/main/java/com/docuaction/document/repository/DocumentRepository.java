package com.docuaction.document.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.docuaction.document.entity.Document;
import com.docuaction.document.entity.DocumentAnalysisStatus;
import com.docuaction.document.entity.DocumentType;

public interface DocumentRepository extends JpaRepository<Document, Long> {

	Page<Document> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

	@Query("""
		select d
		from Document d
		where d.user.id = :userId
			and d.deleted = false
			and (:documentType is null or d.documentType = :documentType)
			and (:analysisStatus is null or d.analysisStatus = :analysisStatus)
		""")
	Page<Document> findUserDocuments(
		@Param("userId") Long userId,
		@Param("documentType") DocumentType documentType,
		@Param("analysisStatus") DocumentAnalysisStatus analysisStatus,
		Pageable pageable
	);

	Optional<Document> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);
}
