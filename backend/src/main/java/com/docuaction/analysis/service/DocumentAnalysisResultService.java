package com.docuaction.analysis.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.docuaction.analysis.dto.AiAnalysisResult;
import com.docuaction.document.entity.Document;
import com.docuaction.document.entity.DocumentField;
import com.docuaction.document.repository.DocumentFieldRepository;

@Service
public class DocumentAnalysisResultService {

	private final DocumentFieldRepository documentFieldRepository;

	public DocumentAnalysisResultService(DocumentFieldRepository documentFieldRepository) {
		this.documentFieldRepository = documentFieldRepository;
	}

	public void saveAnalysisResult(Document document, AiAnalysisResult result) {
		document.applyAnalysis(
			result.documentType(),
			result.title(),
			result.summary(),
			result.confidence()
		);

		documentFieldRepository.deleteByDocumentId(document.getId());
		List<DocumentField> fields = result.fields().stream()
			.map(field -> new DocumentField(
				document,
				field.key(),
				field.label(),
				field.value(),
				field.type()
			))
			.toList();
		documentFieldRepository.saveAll(fields);
	}
}

