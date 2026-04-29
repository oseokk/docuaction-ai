package com.docuaction.analysis.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docuaction.analysis.ai.AiAnalysisException;
import com.docuaction.analysis.ai.AiAnalysisService;
import com.docuaction.analysis.dto.AiAnalysisResult;
import com.docuaction.analysis.entity.AnalysisJob;
import com.docuaction.analysis.entity.AnalysisUsageOperation;
import com.docuaction.analysis.ocr.TextExtractionException;
import com.docuaction.analysis.ocr.TextExtractionResult;
import com.docuaction.analysis.ocr.TextExtractionService;
import com.docuaction.analysis.repository.AnalysisJobRepository;
import com.docuaction.document.entity.Document;
import com.docuaction.document.entity.DocumentAnalysisStatus;

@Service
public class DocumentAnalysisAsyncService {

	private final AnalysisJobRepository analysisJobRepository;
	private final TextExtractionService textExtractionService;
	private final AiAnalysisService aiAnalysisService;
	private final DocumentAnalysisResultService documentAnalysisResultService;
	private final AnalysisUsageLogService analysisUsageLogService;
	private final String aiProvider;

	public DocumentAnalysisAsyncService(
		AnalysisJobRepository analysisJobRepository,
		TextExtractionService textExtractionService,
		AiAnalysisService aiAnalysisService,
		DocumentAnalysisResultService documentAnalysisResultService,
		AnalysisUsageLogService analysisUsageLogService,
		@Value("${docuaction.ai.provider}") String aiProvider
	) {
		this.analysisJobRepository = analysisJobRepository;
		this.textExtractionService = textExtractionService;
		this.aiAnalysisService = aiAnalysisService;
		this.documentAnalysisResultService = documentAnalysisResultService;
		this.analysisUsageLogService = analysisUsageLogService;
		this.aiProvider = aiProvider;
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

			String ocrText = extractTextWithUsageLog(analysisJob, document);
			document.updateOcrText(ocrText);

			AiAnalysisResult analysisResult = analyzeWithUsageLog(analysisJob, ocrText);
			documentAnalysisResultService.saveAnalysisResult(document, analysisResult);

			document.markNeedsReview();
			analysisJob.markCompleted();
		} catch (TextExtractionException exception) {
			document.markFailed(DocumentAnalysisStatus.OCR_FAILED);
			analysisJob.markFailed("OCR_FAILED", exception.getMessage());
		} catch (AiAnalysisException exception) {
			document.markFailed(DocumentAnalysisStatus.AI_FAILED);
			analysisJob.markFailed("AI_FAILED", exception.getMessage());
		} catch (RuntimeException exception) {
			document.markFailed(DocumentAnalysisStatus.FAILED);
			analysisJob.markFailed("ANALYSIS_FAILED", exception.getMessage());
		}
	}

	private String extractTextWithUsageLog(AnalysisJob analysisJob, Document document) {
		long startedAt = System.nanoTime();
		String provider = textExtractionService.providerName(document);
		try {
			TextExtractionResult extractionResult = textExtractionService.extract(document);
			String ocrText = extractionResult.text();
			analysisUsageLogService.logSuccess(
				analysisJob,
				AnalysisUsageOperation.OCR,
				provider,
				elapsedMillis(startedAt),
				safeLongToInt(document.getFileSize()),
				textSize(ocrText)
			);
			return ocrText;
		} catch (TextExtractionException exception) {
			analysisUsageLogService.logFailure(
				analysisJob,
				AnalysisUsageOperation.OCR,
				provider,
				elapsedMillis(startedAt),
				safeLongToInt(document.getFileSize()),
				exception.getMessage()
			);
			throw exception;
		}
	}

	private AiAnalysisResult analyzeWithUsageLog(AnalysisJob analysisJob, String ocrText) {
		long startedAt = System.nanoTime();
		try {
			AiAnalysisResult analysisResult = aiAnalysisService.analyze(ocrText);
			analysisUsageLogService.logSuccess(
				analysisJob,
				AnalysisUsageOperation.AI_ANALYSIS,
				aiProvider,
				elapsedMillis(startedAt),
				textSize(ocrText),
				analysisResult.summary() == null ? 0 : analysisResult.summary().length()
			);
			return analysisResult;
		} catch (AiAnalysisException exception) {
			analysisUsageLogService.logFailure(
				analysisJob,
				AnalysisUsageOperation.AI_ANALYSIS,
				aiProvider,
				elapsedMillis(startedAt),
				textSize(ocrText),
				exception.getMessage()
			);
			throw exception;
		}
	}

	private long elapsedMillis(long startedAt) {
		return (System.nanoTime() - startedAt) / 1_000_000;
	}

	private int textSize(String text) {
		return text == null ? 0 : text.length();
	}

	private int safeLongToInt(long value) {
		return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
	}
}
