package com.docuaction.analysis.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.docuaction.analysis.entity.AnalysisJob;
import com.docuaction.analysis.repository.AnalysisJobRepository;
import com.docuaction.document.entity.Document;
import com.docuaction.user.entity.User;

@Service
public class AnalysisJobService {

	private final AnalysisJobRepository analysisJobRepository;
	private final DocumentAnalysisAsyncService documentAnalysisAsyncService;

	public AnalysisJobService(
		AnalysisJobRepository analysisJobRepository,
		DocumentAnalysisAsyncService documentAnalysisAsyncService
	) {
		this.analysisJobRepository = analysisJobRepository;
		this.documentAnalysisAsyncService = documentAnalysisAsyncService;
	}

	public AnalysisJob createAndStartAfterCommit(Document document, User user) {
		AnalysisJob analysisJob = analysisJobRepository.save(new AnalysisJob(document, user));
		Long jobId = analysisJob.getId();

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					documentAnalysisAsyncService.analyze(jobId);
				}
			});
		} else {
			documentAnalysisAsyncService.analyze(jobId);
		}

		return analysisJob;
	}
}

