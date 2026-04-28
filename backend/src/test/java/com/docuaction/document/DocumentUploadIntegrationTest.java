package com.docuaction.document;

import java.nio.file.Files;
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
import org.springframework.transaction.annotation.Transactional;

import com.docuaction.document.entity.Document;
import com.docuaction.document.entity.DocumentAnalysisStatus;
import com.docuaction.document.entity.DocumentType;
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
@Transactional
class DocumentUploadIntegrationTest {

	@TempDir
	static Path storagePath;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DocumentRepository documentRepository;

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
			"%PDF-1.4 sample".getBytes()
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
		assertThat(document.getAnalysisStatus()).isEqualTo(DocumentAnalysisStatus.UPLOADED);
		assertThat(Files.exists(Path.of(document.getFilePath()))).isTrue();
	}

	@Test
	void uploadWithoutTokenReturnsUnauthorized() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
			"file",
			"bill.pdf",
			"application/pdf",
			"%PDF-1.4 sample".getBytes()
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
}

