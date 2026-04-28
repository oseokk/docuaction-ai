package com.docuaction.action.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.docuaction.action.entity.DocumentAction;
import com.docuaction.action.entity.DocumentActionStatus;

public interface DocumentActionRepository extends JpaRepository<DocumentAction, Long> {

	List<DocumentAction> findByDocumentIdOrderByActionDateAscIdAsc(Long documentId);

	List<DocumentAction> findByUserIdAndStatusAndActionDateGreaterThanEqualOrderByActionDateAscIdAsc(
		Long userId,
		DocumentActionStatus status,
		LocalDate actionDate
	);

	void deleteByDocumentId(Long documentId);
}

