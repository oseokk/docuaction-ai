package com.docuaction.analysis.entity;

import java.time.Instant;

import com.docuaction.document.entity.Document;
import com.docuaction.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "analysis_usage_logs")
public class AnalysisUsageLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "usage_log_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "job_id", nullable = false)
	private AnalysisJob analysisJob;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "document_id", nullable = false)
	private Document document;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private AnalysisUsageOperation operation;

	@Column(nullable = false, length = 50)
	private String provider;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private AnalysisUsageStatus status;

	@Column(nullable = false)
	private long durationMs;

	@Column(nullable = false)
	private int inputSize;

	@Column(nullable = false)
	private int outputSize;

	@Column(columnDefinition = "TEXT")
	private String errorMessage;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected AnalysisUsageLog() {
	}

	public AnalysisUsageLog(
		AnalysisJob analysisJob,
		Document document,
		User user,
		AnalysisUsageOperation operation,
		String provider,
		AnalysisUsageStatus status,
		long durationMs,
		int inputSize,
		int outputSize,
		String errorMessage
	) {
		this.analysisJob = analysisJob;
		this.document = document;
		this.user = user;
		this.operation = operation;
		this.provider = provider;
		this.status = status;
		this.durationMs = durationMs;
		this.inputSize = inputSize;
		this.outputSize = outputSize;
		this.errorMessage = errorMessage;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = Instant.now();
	}

	public AnalysisUsageOperation getOperation() {
		return operation;
	}

	public String getProvider() {
		return provider;
	}

	public AnalysisUsageStatus getStatus() {
		return status;
	}
}
