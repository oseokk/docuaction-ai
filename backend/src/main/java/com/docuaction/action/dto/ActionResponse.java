package com.docuaction.action.dto;

import java.time.LocalDate;

import com.docuaction.action.entity.DocumentAction;

public record ActionResponse(
	Long actionId,
	Long documentId,
	String actionType,
	String title,
	String description,
	LocalDate actionDate,
	String status
) {

	public static ActionResponse from(DocumentAction action) {
		return new ActionResponse(
			action.getId(),
			action.getDocument().getId(),
			action.getActionType().name(),
			action.getTitle(),
			action.getDescription(),
			action.getActionDate(),
			action.getStatus().name()
		);
	}
}

