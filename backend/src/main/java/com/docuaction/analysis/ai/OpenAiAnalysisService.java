package com.docuaction.analysis.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.docuaction.analysis.dto.AiAnalysisField;
import com.docuaction.analysis.dto.AiAnalysisResult;
import com.docuaction.document.entity.DocumentFieldType;
import com.docuaction.document.entity.DocumentType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@ConditionalOnProperty(prefix = "docuaction.ai", name = "provider", havingValue = "openai")
public class OpenAiAnalysisService implements AiAnalysisService {

	private static final URI RESPONSES_API_URI = URI.create("https://api.openai.com/v1/responses");

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;
	private final String apiKey;
	private final String model;

	public OpenAiAnalysisService(
		ObjectMapper objectMapper,
		@Value("${docuaction.openai.api-key}") String apiKey,
		@Value("${docuaction.openai.model}") String model
	) {
		this.objectMapper = objectMapper;
		this.apiKey = apiKey;
		this.model = model;
		this.httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.build();
	}

	@Override
	public AiAnalysisResult analyze(String ocrText) {
		if (apiKey == null || apiKey.isBlank()) {
			throw new AiAnalysisException("OpenAI API key is not configured.");
		}

		try {
			String requestBody = objectMapper.writeValueAsString(createRequestBody(ocrText));
			HttpRequest request = HttpRequest.newBuilder(RESPONSES_API_URI)
				.timeout(Duration.ofSeconds(60))
				.header("Authorization", "Bearer " + apiKey)
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
				.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw new AiAnalysisException("OpenAI API request failed with status " + response.statusCode());
			}

			String outputText = extractOutputText(response.body());
			return parseAnalysisResult(outputText);
		} catch (AiAnalysisException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new AiAnalysisException("Failed to analyze document with OpenAI.", exception);
		}
	}

	private Map<String, Object> createRequestBody(String ocrText) {
		return Map.of(
			"model", model,
			"input", List.of(
				Map.of(
					"role", "system",
					"content", """
						You are a document analysis system.
						Analyze OCR text and return JSON that matches the provided schema.
						Use yyyy-MM-dd for dates. Return numeric amounts as digits only.
						If a value is unknown, use an empty string for field value.
						"""
				),
				Map.of(
					"role", "user",
					"content", "OCR text:\n" + safeText(ocrText)
				)
			),
			"text", Map.of(
				"format", Map.of(
					"type", "json_schema",
					"name", "document_analysis",
					"strict", true,
					"schema", createSchema()
				)
			)
		);
	}

	private Map<String, Object> createSchema() {
		return Map.of(
			"type", "object",
			"additionalProperties", false,
			"required", List.of("documentType", "confidence", "title", "summary", "fields"),
			"properties", Map.of(
				"documentType", Map.of(
					"type", "string",
					"enum", List.of("BILL", "RECEIPT", "CONTRACT", "CERTIFICATE", "ETC")
				),
				"confidence", Map.of(
					"type", "number"
				),
				"title", Map.of("type", "string"),
				"summary", Map.of("type", "string"),
				"fields", Map.of(
					"type", "array",
					"items", Map.of(
						"type", "object",
						"additionalProperties", false,
						"required", List.of("key", "label", "value", "type"),
						"properties", Map.of(
							"key", Map.of("type", "string"),
							"label", Map.of("type", "string"),
							"value", Map.of("type", "string"),
							"type", Map.of(
								"type", "string",
								"enum", List.of("STRING", "DATE", "NUMBER")
							)
						)
					)
				)
			)
		);
	}

	private String extractOutputText(String responseBody) {
		try {
			JsonNode root = objectMapper.readTree(responseBody);
			StringBuilder outputText = new StringBuilder();

			for (JsonNode output : root.path("output")) {
				for (JsonNode content : output.path("content")) {
					if ("output_text".equals(content.path("type").asText())) {
						outputText.append(content.path("text").asText());
					}
				}
			}

			if (outputText.isEmpty()) {
				throw new AiAnalysisException("OpenAI response did not contain output text.");
			}
			return outputText.toString();
		} catch (AiAnalysisException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new AiAnalysisException("Failed to parse OpenAI response.", exception);
		}
	}

	private AiAnalysisResult parseAnalysisResult(String outputText) {
		try {
			JsonNode root = objectMapper.readTree(outputText);
			List<AiAnalysisField> fields = new ArrayList<>();
			for (JsonNode field : root.path("fields")) {
				fields.add(new AiAnalysisField(
					field.path("key").asText(),
					field.path("label").asText(),
					field.path("value").asText(),
					DocumentFieldType.valueOf(field.path("type").asText())
				));
			}

			return new AiAnalysisResult(
				DocumentType.valueOf(root.path("documentType").asText()),
				root.path("confidence").asDouble(),
				root.path("title").asText(),
				root.path("summary").asText(),
				fields
			);
		} catch (Exception exception) {
			throw new AiAnalysisException("OpenAI structured output could not be mapped.", exception);
		}
	}

	private String safeText(String ocrText) {
		if (ocrText == null || ocrText.isBlank()) {
			return "";
		}
		return ocrText.length() > 12000 ? ocrText.substring(0, 12000) : ocrText;
	}
}
