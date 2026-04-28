package com.docuaction.document.dto;

import java.time.Instant;

import com.docuaction.document.entity.Document;

public record DocumentDetailResponse(
	Long documentId,
	String documentType,
	String title,
	String originalFileName,
	long fileSize,
	String mimeType,
	String analysisStatus,
	Instant createdAt,
	Instant updatedAt
) {

	public static DocumentDetailResponse from(Document document) {
		return new DocumentDetailResponse(
			document.getId(),
			document.getDocumentType().name(),
			document.getTitle(),
			document.getOriginalFileName(),
			document.getFileSize(),
			document.getMimeType(),
			document.getAnalysisStatus().name(),
			document.getCreatedAt(),
			document.getUpdatedAt()
		);
	}
}

