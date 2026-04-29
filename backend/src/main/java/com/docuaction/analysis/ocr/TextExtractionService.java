package com.docuaction.analysis.ocr;

import java.util.List;

import org.springframework.stereotype.Service;

import com.docuaction.document.entity.Document;

@Service
public class TextExtractionService {

	private final List<TextExtractionProvider> providers;

	public TextExtractionService(List<TextExtractionProvider> providers) {
		this.providers = providers;
	}

	public TextExtractionResult extract(Document document) {
		TextExtractionProvider provider = findProvider(document);

		return new TextExtractionResult(provider.extract(document), provider.providerName());
	}

	public String providerName(Document document) {
		return findProvider(document).providerName();
	}

	private TextExtractionProvider findProvider(Document document) {
		return providers.stream()
			.filter(candidate -> candidate.supports(document))
			.findFirst()
			.orElseThrow(() -> new TextExtractionException("No text extraction provider supports this file."));
	}
}

