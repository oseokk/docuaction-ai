package com.docuaction.analysis.dto;

import java.util.List;

import com.docuaction.document.entity.DocumentType;

public record AiAnalysisResult(
	DocumentType documentType,
	double confidence,
	String title,
	String summary,
	List<AiAnalysisField> fields
) {
}

