package com.docuaction.action.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.docuaction.action.entity.DocumentAction;
import com.docuaction.action.entity.DocumentActionStatus;
import com.docuaction.action.entity.DocumentActionType;
import com.docuaction.action.repository.DocumentActionRepository;
import com.docuaction.document.entity.Document;
import com.docuaction.document.entity.DocumentField;
import com.docuaction.document.entity.DocumentType;

@Service
public class ActionCreationService {

	private final DocumentActionRepository documentActionRepository;

	public ActionCreationService(DocumentActionRepository documentActionRepository) {
		this.documentActionRepository = documentActionRepository;
	}

	public List<DocumentAction> recreateActions(Document document, List<DocumentField> fields) {
		documentActionRepository.deleteByDocumentId(document.getId());

		List<DocumentAction> actions = createActions(document, fields);
		return documentActionRepository.saveAll(actions);
	}

	private List<DocumentAction> createActions(Document document, List<DocumentField> fields) {
		Map<String, String> fieldValues = fields.stream()
			.collect(Collectors.toMap(
				DocumentField::getFieldKey,
				DocumentField::getFieldValue,
				(first, second) -> first
			));

		if (document.getDocumentType() == DocumentType.BILL) {
			return createBillActions(document, fieldValues);
		}
		if (document.getDocumentType() == DocumentType.RECEIPT) {
			return createReceiptActions(document, fieldValues);
		}
		if (document.getDocumentType() == DocumentType.CONTRACT) {
			return createContractActions(document, fieldValues);
		}
		return List.of();
	}

	private List<DocumentAction> createBillActions(Document document, Map<String, String> fields) {
		return parseDate(fields.get("dueDate"))
			.map(dueDate -> List.of(new DocumentAction(
				document,
				document.getUser(),
				DocumentActionType.REMINDER,
				"납부기한 알림",
				document.getTitle() + " 납부기한 3일 전 알림입니다.",
				dueDate.minusDays(3),
				DocumentActionStatus.PENDING
			)))
			.orElse(List.of());
	}

	private List<DocumentAction> createReceiptActions(Document document, Map<String, String> fields) {
		if (fields.get("amount") == null || fields.get("amount").isBlank()) {
			return List.of();
		}

		LocalDate actionDate = parseDate(fields.get("issueDate")).orElse(LocalDate.now());
		return List.of(new DocumentAction(
			document,
			document.getUser(),
			DocumentActionType.EXPENSE_RECORD,
			"지출 기록",
			document.getTitle() + " 지출 기록입니다.",
			actionDate,
			DocumentActionStatus.PENDING
		));
	}

	private List<DocumentAction> createContractActions(Document document, Map<String, String> fields) {
		return parseDate(fields.get("endDate"))
			.map(endDate -> List.of(new DocumentAction(
				document,
				document.getUser(),
				DocumentActionType.REMINDER,
				"계약 만료 알림",
				document.getTitle() + " 계약 만료 30일 전 알림입니다.",
				endDate.minusDays(30),
				DocumentActionStatus.PENDING
			)))
			.orElse(List.of());
	}

	private Optional<LocalDate> parseDate(String value) {
		if (value == null || value.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(LocalDate.parse(value));
		} catch (RuntimeException exception) {
			return Optional.empty();
		}
	}
}

