package com.docuaction.document;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.docuaction.analysis.entity.AnalysisJob;
import com.docuaction.analysis.entity.AnalysisUsageOperation;
import com.docuaction.analysis.entity.AnalysisUsageStatus;
import com.docuaction.analysis.repository.AnalysisJobRepository;
import com.docuaction.analysis.repository.AnalysisUsageLogRepository;
import com.docuaction.document.entity.Document;
import com.docuaction.document.entity.DocumentAnalysisStatus;
import com.docuaction.document.entity.DocumentType;
import com.docuaction.document.repository.DocumentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "docuaction.ocr.image-provider=mock")
@AutoConfigureMockMvc
class MockImageOcrIntegrationTest {

	@TempDir
	static Path storagePath;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DocumentRepository documentRepository;

	@Autowired
	private AnalysisJobRepository analysisJobRepository;

	@Autowired
	private AnalysisUsageLogRepository analysisUsageLogRepository;

	@DynamicPropertySource
	static void configureStoragePath(DynamicPropertyRegistry registry) {
		registry.add("docuaction.storage.document-path", () -> storagePath.toString());
	}

	@Test
	void imageUploadCanReachReviewWithMockOcrProvider() throws Exception {
		String accessToken = signupAndLogin("mock-image@example.com");
		MockMultipartFile file = new MockMultipartFile(
			"file",
			"receipt.png",
			"image/png",
			new byte[] {1, 2, 3}
		);

		String response = mockMvc.perform(multipart("/api/documents/upload")
				.file(file)
				.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		Long documentId = objectMapper.readTree(response).path("data").path("documentId").asLong();
		Document document = awaitDocumentStatus(documentId, DocumentAnalysisStatus.NEEDS_REVIEW);
		AnalysisJob analysisJob = analysisJobRepository.findTopByDocumentIdOrderByCreatedAtDesc(documentId).orElseThrow();

		assertThat(document.getAnalysisStatus()).isEqualTo(DocumentAnalysisStatus.NEEDS_REVIEW);
		assertThat(document.getDocumentType()).isEqualTo(DocumentType.RECEIPT);
		assertThat(document.getOcrText()).contains("Demo Cafe");
		assertThat(analysisUsageLogRepository.findByAnalysisJobIdOrderByIdAsc(analysisJob.getId()))
			.extracting("operation", "provider", "status")
			.contains(
				org.assertj.core.groups.Tuple.tuple(
					AnalysisUsageOperation.OCR,
					"MOCK_IMAGE_OCR",
					AnalysisUsageStatus.SUCCESS
				),
				org.assertj.core.groups.Tuple.tuple(
					AnalysisUsageOperation.AI_ANALYSIS,
					"mock",
					AnalysisUsageStatus.SUCCESS
				)
			);
	}

	private String signupAndLogin(String email) throws Exception {
		mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "email": "%s",
					  "password": "password1234",
					  "name": "김영석"
					}
					""".formatted(email)))
			.andExpect(status().isOk());

		String loginResponse = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "email": "%s",
					  "password": "password1234"
					}
					""".formatted(email)))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		JsonNode response = objectMapper.readTree(loginResponse);
		return response.path("data").path("accessToken").asText();
	}

	private Document awaitDocumentStatus(Long documentId, DocumentAnalysisStatus expectedStatus) throws Exception {
		long deadline = System.currentTimeMillis() + 3000;
		Document document = documentRepository.findById(documentId).orElseThrow();

		while (System.currentTimeMillis() < deadline) {
			document = documentRepository.findById(documentId).orElseThrow();
			if (document.getAnalysisStatus() == expectedStatus) {
				return document;
			}
			Thread.sleep(100);
		}

		return document;
	}
}
