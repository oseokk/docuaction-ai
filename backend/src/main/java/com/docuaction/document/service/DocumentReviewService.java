package com.docuaction.document.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docuaction.common.exception.BusinessException;
import com.docuaction.common.response.ErrorCode;
import com.docuaction.document.dto.DocumentReviewRequest;
import com.docuaction.document.dto.DocumentReviewResponse;
import com.docuaction.document.entity.Document;
import com.docuaction.document.entity.DocumentField;
import com.docuaction.document.repository.DocumentFieldRepository;
import com.docuaction.document.repository.DocumentRepository;

@Service
@Transactional(readOnly = true)
public class DocumentReviewService {

	private final DocumentRepository documentRepository;
	private final DocumentFieldRepository documentFieldRepository;

	public DocumentReviewService(
		DocumentRepository documentRepository,
		DocumentFieldRepository documentFieldRepository
	) {
		this.documentRepository = documentRepository;
		this.documentFieldRepository = documentFieldRepository;
	}

	@Transactional
	public DocumentReviewResponse review(Long documentId, Long userId, DocumentReviewRequest request) {
		Document document = documentRepository.findByIdAndUserIdAndDeletedFalse(documentId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Document not found."));

		document.completeReview(request.documentType(), request.title(), request.summary());
		replaceFields(document, request.fieldsOrEmpty());

		return new DocumentReviewResponse(
			document.getId(),
			document.getAnalysisStatus().name(),
			"Document review completed."
		);
	}

	private void replaceFields(Document document, List<DocumentReviewRequest.DocumentReviewFieldRequest> fields) {
		documentFieldRepository.deleteByDocumentId(document.getId());
		List<DocumentField> newFields = fields.stream()
			.map(field -> new DocumentField(
				document,
				field.key(),
				field.label(),
				field.value(),
				field.type()
			))
			.toList();
		documentFieldRepository.saveAll(newFields);
	}
}

