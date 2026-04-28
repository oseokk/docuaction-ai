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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "analysis_jobs")
public class AnalysisJob {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "job_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "document_id", nullable = false)
	private Document document;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private AnalysisJobStatus status;

	@Column(nullable = false)
	private int retryCount;

	@Column(nullable = false)
	private int maxRetryCount;

	@Column(length = 100)
	private String errorCode;

	@Column(columnDefinition = "TEXT")
	private String errorMessage;

	private Instant startedAt;

	private Instant completedAt;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected AnalysisJob() {
	}

	public AnalysisJob(Document document, User user) {
		this.document = document;
		this.user = user;
		this.status = AnalysisJobStatus.PENDING;
		this.retryCount = 0;
		this.maxRetryCount = 2;
	}

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		this.updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public AnalysisJobStatus getStatus() {
		return status;
	}

	public Document getDocument() {
		return document;
	}

	public User getUser() {
		return user;
	}

	public void markProcessing() {
		this.status = AnalysisJobStatus.PROCESSING;
		this.startedAt = Instant.now();
	}

	public void markCompleted() {
		this.status = AnalysisJobStatus.COMPLETED;
		this.completedAt = Instant.now();
	}

	public void markFailed(String errorCode, String errorMessage) {
		this.status = AnalysisJobStatus.FAILED;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.completedAt = Instant.now();
	}
}

