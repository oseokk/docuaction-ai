package com.docuaction.analysis.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docuaction.analysis.entity.AnalysisJob;
import com.docuaction.analysis.ocr.TextExtractionException;
import com.docuaction.analysis.ocr.TextExtractionService;
import com.docuaction.analysis.repository.AnalysisJobRepository;
import com.docuaction.document.entity.Document;
import com.docuaction.document.entity.DocumentAnalysisStatus;

@Service
public class DocumentAnalysisAsyncService {

	private final AnalysisJobRepository analysisJobRepository;
	private final TextExtractionService textExtractionService;

	public DocumentAnalysisAsyncService(
		AnalysisJobRepository analysisJobRepository,
		TextExtractionService textExtractionService
	) {
		this.analysisJobRepository = analysisJobRepository;
		this.textExtractionService = textExtractionService;
	}

	@Async
	@Transactional
	public void analyze(Long jobId) {
		AnalysisJob analysisJob = analysisJobRepository.findById(jobId)
			.orElseThrow();
		Document document = analysisJob.getDocument();

		try {
			analysisJob.markProcessing();
			document.markProcessing();

			String ocrText = textExtractionService.extract(document);
			document.updateOcrText(ocrText);

			// AI classification and field extraction will replace this stub in the next phase.
			document.markNeedsReview();
			analysisJob.markCompleted();
		} catch (TextExtractionException exception) {
			document.markFailed(DocumentAnalysisStatus.OCR_FAILED);
			analysisJob.markFailed("OCR_FAILED", exception.getMessage());
		} catch (RuntimeException exception) {
			document.markFailed(DocumentAnalysisStatus.FAILED);
			analysisJob.markFailed("ANALYSIS_FAILED", exception.getMessage());
			throw exception;
		}
	}
}
