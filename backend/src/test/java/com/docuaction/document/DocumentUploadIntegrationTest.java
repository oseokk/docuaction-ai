package com.docuaction.document;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
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
import com.docuaction.analysis.entity.AnalysisJobStatus;
import com.docuaction.analysis.repository.AnalysisJobRepository;
import com.docuaction.document.entity.Document;
import com.docuaction.document.entity.DocumentAnalysisStatus;
import com.docuaction.document.entity.DocumentType;
import com.docuaction.document.repository.DocumentFieldRepository;
import com.docuaction.document.repository.DocumentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentUploadIntegrationTest {

	@TempDir
	static Path storagePath;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DocumentRepository documentRepository;

	@Autowired
	private DocumentFieldRepository documentFieldRepository;

	@Autowired
	private AnalysisJobRepository analysisJobRepository;

	@DynamicPropertySource
	static void configureStoragePath(DynamicPropertyRegistry registry) {
		registry.add("docuaction.storage.document-path", () -> storagePath.toString());
	}

	@Test
	void uploadDocumentStoresFileAndMetadata() throws Exception {
		String accessToken = signupAndLogin("upload@example.com");
		MockMultipartFile file = new MockMultipartFile(
			"file",
			"bill.pdf",
			"application/pdf",
			createPdfBytes("Electric bill amount 72300 due 2026-05-10")
		);

		String response = mockMvc.perform(multipart("/api/documents/upload")
				.file(file)
				.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.status").value("UPLOADED"))
			.andReturn()
			.getResponse()
			.getContentAsString();

		Long documentId = objectMapper.readTree(response).path("data").path("documentId").asLong();
		Document document = documentRepository.findById(documentId).orElseThrow();

		assertThat(document.getOriginalFileName()).isEqualTo("bill.pdf");
		assertThat(document.getMimeType()).isEqualTo("application/pdf");
		assertThat(document.getDocumentType()).isEqualTo(DocumentType.UNKNOWN);
		assertThat(document.getAnalysisStatus()).isIn(
			DocumentAnalysisStatus.UPLOADED,
			DocumentAnalysisStatus.PROCESSING,
			DocumentAnalysisStatus.NEEDS_REVIEW
		);
		assertThat(Files.exists(Path.of(document.getFilePath()))).isTrue();
	}

	@Test
	void uploadDocumentEventuallyStartsAnalysis() throws Exception {
		String accessToken = signupAndLogin("analysis@example.com");
		Long documentId = upload(accessToken, "analysis.pdf", "Electric bill amount 72300 due 2026-05-10");

		Document document = awaitDocumentStatus(documentId, DocumentAnalysisStatus.NEEDS_REVIEW);
		AnalysisJob analysisJob = analysisJobRepository.findTopByDocumentIdOrderByCreatedAtDesc(documentId).orElseThrow();

		assertThat(document.getAnalysisStatus()).isEqualTo(DocumentAnalysisStatus.NEEDS_REVIEW);
		assertThat(document.getOcrText()).contains("Electric bill amount 72300 due 2026-05-10");
		assertThat(document.getDocumentType()).isEqualTo(DocumentType.BILL);
		assertThat(document.getSummary()).contains("납부");
		assertThat(documentFieldRepository.findByDocumentIdOrderByIdAsc(documentId))
			.extracting("fieldKey", "fieldValue")
			.contains(
				org.assertj.core.groups.Tuple.tuple("amount", "72300"),
				org.assertj.core.groups.Tuple.tuple("dueDate", "2026-05-10")
			);
		assertThat(analysisJob.getStatus()).isEqualTo(AnalysisJobStatus.COMPLETED);
	}

	@Test
	void getDocumentsReturnsOnlyCurrentUsersDocuments() throws Exception {
		String firstUserToken = signupAndLogin("first-user@example.com");
		String secondUserToken = signupAndLogin("second-user@example.com");

		upload(firstUserToken, "first-bill.pdf");
		upload(firstUserToken, "first-receipt.png");
		upload(secondUserToken, "second-bill.pdf");

		mockMvc.perform(get("/api/documents")
				.param("page", "0")
				.param("size", "10")
				.header("Authorization", "Bearer " + firstUserToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.totalElements").value(2))
			.andExpect(jsonPath("$.data.content[0].documentId").exists())
			.andExpect(jsonPath("$.data.content[0].analysisStatus").exists());
	}

	@Test
	void getDocumentReturnsCurrentUsersDocumentDetail() throws Exception {
		String accessToken = signupAndLogin("detail@example.com");
		Long documentId = upload(accessToken, "contract.pdf");

		mockMvc.perform(get("/api/documents/{documentId}", documentId)
				.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.documentId").value(documentId))
			.andExpect(jsonPath("$.data.originalFileName").value("contract.pdf"))
			.andExpect(jsonPath("$.data.documentType").exists())
			.andExpect(jsonPath("$.data.fields").isArray())
			.andExpect(jsonPath("$.data.analysisStatus").exists());
	}

	@Test
	void getOtherUsersDocumentReturnsNotFound() throws Exception {
		String ownerToken = signupAndLogin("owner@example.com");
		String otherToken = signupAndLogin("other@example.com");
		Long documentId = upload(ownerToken, "private.pdf");

		mockMvc.perform(get("/api/documents/{documentId}", documentId)
				.header("Authorization", "Bearer " + otherToken))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error.code").value("COMMON_404"));
	}

	@Test
	void reviewDocumentCompletesAnalysisAndReplacesFields() throws Exception {
		String accessToken = signupAndLogin("review@example.com");
		Long documentId = upload(accessToken, "review.pdf", "Electric bill amount 72300 due 2026-05-10");
		awaitDocumentStatus(documentId, DocumentAnalysisStatus.NEEDS_REVIEW);

		mockMvc.perform(post("/api/documents/{documentId}/review", documentId)
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "documentType": "BILL",
					  "title": "4월 전기요금 고지서",
					  "summary": "사용자가 검수한 고지서입니다.",
					  "fields": [
					    {
					      "key": "issuer",
					      "label": "기관명",
					      "value": "한국전력",
					      "type": "STRING"
					    },
					    {
					      "key": "amount",
					      "label": "납부금액",
					      "value": "80000",
					      "type": "NUMBER"
					    },
					    {
					      "key": "dueDate",
					      "label": "납부기한",
					      "value": "2026-05-11",
					      "type": "DATE"
					    }
					  ]
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.status").value("COMPLETED"));

		mockMvc.perform(get("/api/documents/{documentId}", documentId)
				.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.analysisStatus").value("COMPLETED"))
			.andExpect(jsonPath("$.data.title").value("4월 전기요금 고지서"))
			.andExpect(jsonPath("$.data.summary").value("사용자가 검수한 고지서입니다."))
			.andExpect(jsonPath("$.data.fields.length()").value(3))
			.andExpect(jsonPath("$.data.fields[1].key").value("amount"))
			.andExpect(jsonPath("$.data.fields[1].value").value("80000"))
			.andExpect(jsonPath("$.data.actions.length()").value(1))
			.andExpect(jsonPath("$.data.actions[0].actionType").value("REMINDER"))
			.andExpect(jsonPath("$.data.actions[0].actionDate").value("2026-05-08"))
			.andExpect(jsonPath("$.data.actions[0].status").value("PENDING"));

		mockMvc.perform(get("/api/actions/upcoming")
				.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].documentId").value(documentId))
			.andExpect(jsonPath("$.data[0].title").value("납부기한 알림"));
	}

	@Test
	void completeActionChangesStatusAndRemovesFromUpcoming() throws Exception {
		String accessToken = signupAndLogin("complete-action@example.com");
		Long documentId = upload(accessToken, "complete-action.pdf", "Electric bill amount 72300 due 2026-05-10");
		awaitDocumentStatus(documentId, DocumentAnalysisStatus.NEEDS_REVIEW);
		reviewBill(accessToken, documentId);

		String detailResponse = mockMvc.perform(get("/api/documents/{documentId}", documentId)
				.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		Long actionId = objectMapper.readTree(detailResponse).path("data").path("actions").get(0).path("actionId").asLong();

		mockMvc.perform(post("/api/actions/{actionId}/complete", actionId)
				.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.actionId").value(actionId))
			.andExpect(jsonPath("$.data.status").value("COMPLETED"));

		mockMvc.perform(get("/api/actions/upcoming")
				.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(0));

		mockMvc.perform(get("/api/documents/{documentId}", documentId)
				.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.actions[0].status").value("COMPLETED"));
	}

	@Test
	void completeOtherUsersActionReturnsNotFound() throws Exception {
		String ownerToken = signupAndLogin("action-owner@example.com");
		String otherToken = signupAndLogin("action-other@example.com");
		Long documentId = upload(ownerToken, "owner-action.pdf", "Electric bill amount 72300 due 2026-05-10");
		awaitDocumentStatus(documentId, DocumentAnalysisStatus.NEEDS_REVIEW);
		reviewBill(ownerToken, documentId);

		String detailResponse = mockMvc.perform(get("/api/documents/{documentId}", documentId)
				.header("Authorization", "Bearer " + ownerToken))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		Long actionId = objectMapper.readTree(detailResponse).path("data").path("actions").get(0).path("actionId").asLong();

		mockMvc.perform(post("/api/actions/{actionId}/complete", actionId)
				.header("Authorization", "Bearer " + otherToken))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error.code").value("COMMON_404"));
	}

	@Test
	void reviewOtherUsersDocumentReturnsNotFound() throws Exception {
		String ownerToken = signupAndLogin("review-owner@example.com");
		String otherToken = signupAndLogin("review-other@example.com");
		Long documentId = upload(ownerToken, "private-review.pdf");
		awaitDocumentStatus(documentId, DocumentAnalysisStatus.NEEDS_REVIEW);

		mockMvc.perform(post("/api/documents/{documentId}/review", documentId)
				.header("Authorization", "Bearer " + otherToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "documentType": "BILL",
					  "title": "잘못된 접근",
					  "summary": "권한 없음",
					  "fields": []
					}
					"""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error.code").value("COMMON_404"));
	}

	@Test
	void uploadWithoutTokenReturnsUnauthorized() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
			"file",
			"bill.pdf",
			"application/pdf",
			createPdfBytes("Unauthorized PDF")
		);

		mockMvc.perform(multipart("/api/documents/upload").file(file))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void uploadUnsupportedFileReturnsBadRequest() throws Exception {
		String accessToken = signupAndLogin("invalid-file@example.com");
		MockMultipartFile file = new MockMultipartFile(
			"file",
			"memo.txt",
			"text/plain",
			"hello".getBytes()
		);

		mockMvc.perform(multipart("/api/documents/upload")
				.file(file)
				.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error.code").value("DOCUMENT_400"));
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

	private Long upload(String accessToken, String fileName) throws Exception {
		return upload(accessToken, fileName, "Sample PDF text");
	}

	private Long upload(String accessToken, String fileName, String text) throws Exception {
		String contentType = fileName.endsWith(".png") ? "image/png" : "application/pdf";
		byte[] content = fileName.endsWith(".png") ? new byte[] {1, 2, 3} : createPdfBytes(text);
		MockMultipartFile file = new MockMultipartFile("file", fileName, contentType, content);

		String response = mockMvc.perform(multipart("/api/documents/upload")
				.file(file)
				.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		return objectMapper.readTree(response).path("data").path("documentId").asLong();
	}

	private void reviewBill(String accessToken, Long documentId) throws Exception {
		mockMvc.perform(post("/api/documents/{documentId}/review", documentId)
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "documentType": "BILL",
					  "title": "전기요금 고지서",
					  "summary": "검수 완료",
					  "fields": [
					    {
					      "key": "amount",
					      "label": "납부금액",
					      "value": "72300",
					      "type": "NUMBER"
					    },
					    {
					      "key": "dueDate",
					      "label": "납부기한",
					      "value": "2026-05-10",
					      "type": "DATE"
					    }
					  ]
					}
					"""))
			.andExpect(status().isOk());
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

	private byte[] createPdfBytes(String text) throws Exception {
		try (
			PDDocument document = new PDDocument();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
		) {
			PDPage page = new PDPage();
			document.addPage(page);

			try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
				contentStream.beginText();
				contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
				contentStream.newLineAtOffset(50, 700);
				contentStream.showText(text);
				contentStream.endText();
			}

			document.save(outputStream);
			return outputStream.toByteArray();
		}
	}
}
