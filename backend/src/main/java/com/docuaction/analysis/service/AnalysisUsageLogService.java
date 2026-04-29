package com.docuaction.analysis.service;

import org.springframework.stereotype.Service;

import com.docuaction.analysis.entity.AnalysisJob;
import com.docuaction.analysis.entity.AnalysisUsageLog;
import com.docuaction.analysis.entity.AnalysisUsageOperation;
import com.docuaction.analysis.entity.AnalysisUsageStatus;
import com.docuaction.analysis.repository.AnalysisUsageLogRepository;
import com.docuaction.document.entity.Document;

@Service
public class AnalysisUsageLogService {

	private final AnalysisUsageLogRepository analysisUsageLogRepository;

	public AnalysisUsageLogService(AnalysisUsageLogRepository analysisUsageLogRepository) {
		this.analysisUsageLogRepository = analysisUsageLogRepository;
	}

	public void logSuccess(
		AnalysisJob analysisJob,
		AnalysisUsageOperation operation,
		String provider,
		long durationMs,
		int inputSize,
		int outputSize
	) {
		save(analysisJob, operation, provider, AnalysisUsageStatus.SUCCESS, durationMs, inputSize, outputSize, null);
	}

	public void logFailure(
		AnalysisJob analysisJob,
		AnalysisUsageOperation operation,
		String provider,
		long durationMs,
		int inputSize,
		String errorMessage
	) {
		save(analysisJob, operation, provider, AnalysisUsageStatus.FAILED, durationMs, inputSize, 0, errorMessage);
	}

	private void save(
		AnalysisJob analysisJob,
		AnalysisUsageOperation operation,
		String provider,
		AnalysisUsageStatus status,
		long durationMs,
		int inputSize,
		int outputSize,
		String errorMessage
	) {
		Document document = analysisJob.getDocument();
		analysisUsageLogRepository.save(new AnalysisUsageLog(
			analysisJob,
			document,
			analysisJob.getUser(),
			operation,
			provider,
			status,
			durationMs,
			inputSize,
			outputSize,
			errorMessage
		));
	}
}
