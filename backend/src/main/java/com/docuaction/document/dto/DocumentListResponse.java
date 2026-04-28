package com.docuaction.document.dto;

import java.time.Instant;

import com.docuaction.document.entity.Document;

public record DocumentListResponse(
	Long documentId,
	String documentType,
	String title,
	String originalFileName,
	String analysisStatus,
	Instant createdAt
) {

	public static DocumentListResponse from(Document document) {
		return new DocumentListResponse(
			document.getId(),
			document.getDocumentType().name(),
			document.getTitle(),
			document.getOriginalFileName(),
			document.getAnalysisStatus().name(),
			document.getCreatedAt()
		);
	}
}

