package com.docuaction.analysis.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.docuaction.analysis.dto.AiAnalysisField;
import com.docuaction.analysis.dto.AiAnalysisResult;
import com.docuaction.document.entity.DocumentFieldType;
import com.docuaction.document.entity.DocumentType;

@Service
@ConditionalOnProperty(prefix = "docuaction.ai", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockAiAnalysisService implements AiAnalysisService {

	private static final Pattern DATE_PATTERN = Pattern.compile("\\b(20\\d{2}-\\d{2}-\\d{2})\\b");
	private static final Pattern AMOUNT_PATTERN = Pattern.compile("(?i)(?:amount|금액|납부금액)\\s*:?\\s*([0-9,]+)");

	@Override
	public AiAnalysisResult analyze(String ocrText) {
		String text = ocrText == null ? "" : ocrText;
		String normalized = text.toLowerCase();

		if (normalized.contains("bill") || normalized.contains("due") || normalized.contains("고지")) {
			return analyzeBill(text);
		}
		if (normalized.contains("receipt") || normalized.contains("영수증")) {
			return analyzeReceipt(text);
		}
		if (normalized.contains("contract") || normalized.contains("계약")) {
			return analyzeContract(text);
		}
		return analyzeEtc(text);
	}

	private AiAnalysisResult analyzeBill(String text) {
		List<AiAnalysisField> fields = new ArrayList<>();
		addField(fields, "issuer", "기관명", "Unknown issuer", DocumentFieldType.STRING);
		addField(fields, "amount", "납부금액", findAmount(text), DocumentFieldType.NUMBER);
		addField(fields, "dueDate", "납부기한", findFirstDate(text), DocumentFieldType.DATE);

		return new AiAnalysisResult(
			DocumentType.BILL,
			0.80,
			"납부 고지서",
			"납부 관련 문서로 분석되었습니다.",
			fields
		);
	}

	private AiAnalysisResult analyzeReceipt(String text) {
		List<AiAnalysisField> fields = new ArrayList<>();
		addField(fields, "issuer", "상호명", "Unknown merchant", DocumentFieldType.STRING);
		addField(fields, "amount", "금액", findAmount(text), DocumentFieldType.NUMBER);
		addField(fields, "issueDate", "결제일", findFirstDate(text), DocumentFieldType.DATE);

		return new AiAnalysisResult(
			DocumentType.RECEIPT,
			0.75,
			"영수증",
			"지출 기록 후보 문서로 분석되었습니다.",
			fields
		);
	}

	private AiAnalysisResult analyzeContract(String text) {
		List<AiAnalysisField> fields = new ArrayList<>();
		addField(fields, "title", "계약명", "계약서", DocumentFieldType.STRING);
		addField(fields, "endDate", "종료일", findFirstDate(text), DocumentFieldType.DATE);

		return new AiAnalysisResult(
			DocumentType.CONTRACT,
			0.75,
			"계약서",
			"계약 관련 문서로 분석되었습니다.",
			fields
		);
	}

	private AiAnalysisResult analyzeEtc(String text) {
		return new AiAnalysisResult(
			DocumentType.ETC,
			0.50,
			"기타 문서",
			text.isBlank() ? "추출된 텍스트가 없습니다." : "일반 문서로 분석되었습니다.",
			List.of()
		);
	}

	private void addField(
		List<AiAnalysisField> fields,
		String key,
		String label,
		String value,
		DocumentFieldType type
	) {
		if (value != null && !value.isBlank()) {
			fields.add(new AiAnalysisField(key, label, value, type));
		}
	}

	private String findFirstDate(String text) {
		Matcher matcher = DATE_PATTERN.matcher(text);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	private String findAmount(String text) {
		Matcher matcher = AMOUNT_PATTERN.matcher(text);
		if (matcher.find()) {
			return matcher.group(1).replace(",", "");
		}
		return null;
	}
}
