package com.docuaction.analysis.dto;

import com.docuaction.document.entity.DocumentFieldType;

public record AiAnalysisField(
	String key,
	String label,
	String value,
	DocumentFieldType type
) {
}

