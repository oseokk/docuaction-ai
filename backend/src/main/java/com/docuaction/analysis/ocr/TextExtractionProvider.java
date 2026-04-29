package com.docuaction.analysis.ocr;

import com.docuaction.document.entity.Document;

public interface TextExtractionProvider {

	boolean supports(Document document);

	String providerName();

	String extract(Document document);
}
