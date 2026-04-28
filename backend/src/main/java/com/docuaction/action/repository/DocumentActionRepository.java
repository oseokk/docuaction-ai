package com.docuaction.action.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

	Optional<DocumentAction> findByIdAndUserId(Long id, Long userId);

	void deleteByDocumentId(Long documentId);
}
