package com.docuaction.action.dto;

public record ActionCompleteResponse(
	Long actionId,
	String status,
	String message
) {
}

