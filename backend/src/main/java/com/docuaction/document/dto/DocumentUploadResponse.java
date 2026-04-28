package com.docuaction.document.dto;

public record DocumentUploadResponse(
	Long documentId,
	String status,
	String message
) {
}

