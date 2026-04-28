package com.docuaction.analysis.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docuaction.analysis.entity.AnalysisJob;
import com.docuaction.analysis.repository.AnalysisJobRepository;
import com.docuaction.document.entity.Document;
import com.docuaction.document.entity.DocumentAnalysisStatus;

@Service
public class DocumentAnalysisAsyncService {

	private final AnalysisJobRepository analysisJobRepository;

	public DocumentAnalysisAsyncService(AnalysisJobRepository analysisJobRepository) {
		this.analysisJobRepository = analysisJobRepository;
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

			// OCR and AI integration will replace this stub in the next phase.
			document.markNeedsReview();
			analysisJob.markCompleted();
		} catch (RuntimeException exception) {
			document.markFailed(DocumentAnalysisStatus.FAILED);
			analysisJob.markFailed("ANALYSIS_FAILED", exception.getMessage());
			throw exception;
		}
	}
}

