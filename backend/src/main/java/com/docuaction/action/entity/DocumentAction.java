package com.docuaction.action.entity;

import java.time.Instant;
import java.time.LocalDate;

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
@Table(name = "document_actions")
public class DocumentAction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "action_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "document_id", nullable = false)
	private Document document;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private DocumentActionType actionType;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false)
	private LocalDate actionDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private DocumentActionStatus status;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected DocumentAction() {
	}

	public DocumentAction(
		Document document,
		User user,
		DocumentActionType actionType,
		String title,
		String description,
		LocalDate actionDate,
		DocumentActionStatus status
	) {
		this.document = document;
		this.user = user;
		this.actionType = actionType;
		this.title = title;
		this.description = description;
		this.actionDate = actionDate;
		this.status = status;
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

	public Document getDocument() {
		return document;
	}

	public DocumentActionType getActionType() {
		return actionType;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public LocalDate getActionDate() {
		return actionDate;
	}

	public DocumentActionStatus getStatus() {
		return status;
	}

	public void complete() {
		this.status = DocumentActionStatus.COMPLETED;
	}
}
