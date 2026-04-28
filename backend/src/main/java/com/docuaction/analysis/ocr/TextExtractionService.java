package com.docuaction.analysis.ocr;

import org.springframework.stereotype.Service;

import com.docuaction.document.entity.Document;

@Service
public class TextExtractionService {

	private static final String PDF_MIME_TYPE = "application/pdf";

	private final PdfTextExtractor pdfTextExtractor;

	public TextExtractionService(PdfTextExtractor pdfTextExtractor) {
		this.pdfTextExtractor = pdfTextExtractor;
	}

	public String extract(Document document) {
		if (PDF_MIME_TYPE.equalsIgnoreCase(document.getMimeType())) {
			return pdfTextExtractor.extract(document.getFilePath());
		}

		// Image OCR provider integration will be added in a later phase.
		return "";
	}
}

