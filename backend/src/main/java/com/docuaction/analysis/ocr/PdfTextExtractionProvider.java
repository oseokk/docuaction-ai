package com.docuaction.analysis.ocr;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.docuaction.document.entity.Document;

@Order(10)
@Component
public class PdfTextExtractionProvider implements TextExtractionProvider {

	private static final String PDF_MIME_TYPE = "application/pdf";

	private final PdfTextExtractor pdfTextExtractor;

	public PdfTextExtractionProvider(PdfTextExtractor pdfTextExtractor) {
		this.pdfTextExtractor = pdfTextExtractor;
	}

	@Override
	public boolean supports(Document document) {
		return PDF_MIME_TYPE.equalsIgnoreCase(document.getMimeType());
	}

	@Override
	public String providerName() {
		return "PDFBOX";
	}

	@Override
	public String extract(Document document) {
		return pdfTextExtractor.extract(document.getFilePath());
	}
}
