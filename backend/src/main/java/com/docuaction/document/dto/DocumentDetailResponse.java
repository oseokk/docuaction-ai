package com.docuaction.document.dto;

import java.time.Instant;
import java.util.List;

import com.docuaction.document.entity.Document;
import com.docuaction.document.entity.DocumentField;

public record DocumentDetailResponse(
	Long documentId,
	String documentType,
	String title,
	String summary,
	Double confidence,
	String originalFileName,
	long fileSize,
	String mimeType,
	String analysisStatus,
	List<DocumentFieldResponse> fields,
	Instant createdAt,
	Instant updatedAt
) {

	public static DocumentDetailResponse from(Document document, List<DocumentField> fields) {
		return new DocumentDetailResponse(
			document.getId(),
			document.getDocumentType().name(),
			document.getTitle(),
			document.getSummary(),
			document.getConfidence(),
			document.getOriginalFileName(),
			document.getFileSize(),
			document.getMimeType(),
			document.getAnalysisStatus().name(),
			fields.stream().map(DocumentFieldResponse::from).toList(),
			document.getCreatedAt(),
			document.getUpdatedAt()
		);
	}

	public record DocumentFieldResponse(
		Long fieldId,
		String key,
		String label,
		String value,
		String type
	) {

		static DocumentFieldResponse from(DocumentField field) {
			return new DocumentFieldResponse(
				field.getId(),
				field.getFieldKey(),
				field.getFieldLabel(),
				field.getFieldValue(),
				field.getFieldType().name()
			);
		}
	}
}
