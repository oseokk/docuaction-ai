package com.docuaction.document.entity;

import java.time.Instant;

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
@Table(name = "documents")
public class Document {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "document_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 255)
	private String originalFileName;

	@Column(nullable = false, length = 255)
	private String storedFileName;

	@Column(nullable = false, length = 500)
	private String filePath;

	@Column(nullable = false)
	private long fileSize;

	@Column(nullable = false, length = 100)
	private String mimeType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private DocumentType documentType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private DocumentAnalysisStatus analysisStatus;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(nullable = false)
	private boolean deleted;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected Document() {
	}

	public Document(
		User user,
		String originalFileName,
		String storedFileName,
		String filePath,
		long fileSize,
		String mimeType
	) {
		this.user = user;
		this.originalFileName = originalFileName;
		this.storedFileName = storedFileName;
		this.filePath = filePath;
		this.fileSize = fileSize;
		this.mimeType = mimeType;
		this.documentType = DocumentType.UNKNOWN;
		this.analysisStatus = DocumentAnalysisStatus.UPLOADED;
		this.title = originalFileName;
		this.deleted = false;
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

	public User getUser() {
		return user;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public String getStoredFileName() {
		return storedFileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public long getFileSize() {
		return fileSize;
	}

	public String getMimeType() {
		return mimeType;
	}

	public DocumentType getDocumentType() {
		return documentType;
	}

	public DocumentAnalysisStatus getAnalysisStatus() {
		return analysisStatus;
	}

	public String getTitle() {
		return title;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void markProcessing() {
		this.analysisStatus = DocumentAnalysisStatus.PROCESSING;
	}

	public void markNeedsReview() {
		this.analysisStatus = DocumentAnalysisStatus.NEEDS_REVIEW;
	}

	public void markFailed(DocumentAnalysisStatus failedStatus) {
		this.analysisStatus = failedStatus;
	}
}
