package com.docuaction.document.dto;

public record DocumentReviewResponse(
	Long documentId,
	String status,
	String message
) {
}

