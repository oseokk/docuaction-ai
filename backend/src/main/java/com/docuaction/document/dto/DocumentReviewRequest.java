package com.docuaction.document.dto;

import java.util.List;

import com.docuaction.document.entity.DocumentFieldType;
import com.docuaction.document.entity.DocumentType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DocumentReviewRequest(
	@NotNull
	DocumentType documentType,

	@NotBlank
	@Size(max = 255)
	String title,

	String summary,

	@Valid
	List<DocumentReviewFieldRequest> fields
) {

	public List<DocumentReviewFieldRequest> fieldsOrEmpty() {
		return fields == null ? List.of() : fields;
	}

	public record DocumentReviewFieldRequest(
		@NotBlank
		@Size(max = 100)
		String key,

		@NotBlank
		@Size(max = 100)
		String label,

		@Size(max = 500)
		String value,

		@NotNull
		DocumentFieldType type
	) {
	}
}

