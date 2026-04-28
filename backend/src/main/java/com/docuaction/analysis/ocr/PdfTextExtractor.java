package com.docuaction.analysis.ocr;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

@Component
public class PdfTextExtractor {

	public String extract(String filePath) {
		try (PDDocument document = Loader.loadPDF(new File(filePath))) {
			PDFTextStripper textStripper = new PDFTextStripper();
			return normalize(textStripper.getText(document));
		} catch (IOException exception) {
			throw new TextExtractionException("Failed to extract PDF text.", exception);
		}
	}

	private String normalize(String text) {
		if (text == null) {
			return "";
		}
		return text.replace("\r\n", "\n").trim();
	}
}

