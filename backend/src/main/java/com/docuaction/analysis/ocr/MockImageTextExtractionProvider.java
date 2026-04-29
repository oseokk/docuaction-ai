package com.docuaction.analysis.ocr;

import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.docuaction.document.entity.Document;

@Order(90)
@Component
@ConditionalOnProperty(prefix = "docuaction.ocr", name = "image-provider", havingValue = "mock")
public class MockImageTextExtractionProvider implements TextExtractionProvider {

	private static final Set<String> IMAGE_MIME_TYPES = Set.of("image/jpeg", "image/png");

	@Override
	public boolean supports(Document document) {
		return IMAGE_MIME_TYPES.contains(document.getMimeType().toLowerCase());
	}

	@Override
	public String providerName() {
		return "MOCK_IMAGE_OCR";
	}

	@Override
	public String extract(Document document) {
		return """
			Receipt
			Merchant: Demo Cafe
			Amount: 6500
			Date: 2026-04-28
			""".trim();
	}
}
