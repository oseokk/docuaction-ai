package com.docuaction.analysis.ocr;

import java.util.Set;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.docuaction.document.entity.Document;

@Order(100)
@Component
public class UnsupportedImageTextExtractionProvider implements TextExtractionProvider {

	private static final Set<String> IMAGE_MIME_TYPES = Set.of("image/jpeg", "image/png");

	@Override
	public boolean supports(Document document) {
		return IMAGE_MIME_TYPES.contains(document.getMimeType().toLowerCase());
	}

	@Override
	public String providerName() {
		return "UNSUPPORTED_IMAGE";
	}

	@Override
	public String extract(Document document) {
		throw new TextExtractionException("Image OCR provider is not configured yet.");
	}
}
