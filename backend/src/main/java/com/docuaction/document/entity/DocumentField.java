package com.docuaction.document.entity;

import java.time.Instant;

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
@Table(name = "document_fields")
public class DocumentField {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "field_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "document_id", nullable = false)
	private Document document;

	@Column(nullable = false, length = 100)
	private String fieldKey;

	@Column(nullable = false, length = 100)
	private String fieldLabel;

	@Column(length = 500)
	private String fieldValue;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private DocumentFieldType fieldType;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected DocumentField() {
	}

	public DocumentField(
		Document document,
		String fieldKey,
		String fieldLabel,
		String fieldValue,
		DocumentFieldType fieldType
	) {
		this.document = document;
		this.fieldKey = fieldKey;
		this.fieldLabel = fieldLabel;
		this.fieldValue = fieldValue;
		this.fieldType = fieldType;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Document getDocument() {
		return document;
	}

	public String getFieldKey() {
		return fieldKey;
	}

	public String getFieldLabel() {
		return fieldLabel;
	}

	public String getFieldValue() {
		return fieldValue;
	}

	public DocumentFieldType getFieldType() {
		return fieldType;
	}
}

