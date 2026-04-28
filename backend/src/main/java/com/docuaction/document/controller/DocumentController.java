package com.docuaction.document.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.docuaction.common.response.ApiResponse;
import com.docuaction.common.response.PageResponse;
import com.docuaction.common.security.SecurityUtils;
import com.docuaction.document.dto.DocumentDetailResponse;
import com.docuaction.document.dto.DocumentDeleteResponse;
import com.docuaction.document.dto.DocumentListResponse;
import com.docuaction.document.dto.DocumentReviewRequest;
import com.docuaction.document.dto.DocumentReviewResponse;
import com.docuaction.document.dto.DocumentUploadResponse;
import com.docuaction.document.service.DocumentReviewService;
import com.docuaction.document.service.DocumentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

	private final DocumentService documentService;
	private final DocumentReviewService documentReviewService;

	public DocumentController(
		DocumentService documentService,
		DocumentReviewService documentReviewService
	) {
		this.documentService = documentService;
		this.documentReviewService = documentReviewService;
	}

	@PostMapping("/upload")
	public ApiResponse<DocumentUploadResponse> upload(@RequestPart("file") MultipartFile file) {
		Long userId = SecurityUtils.currentUser().userId();
		return ApiResponse.success(documentService.uploadDocument(file, userId));
	}

	@GetMapping
	public ApiResponse<PageResponse<DocumentListResponse>> getDocuments(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size
	) {
		Long userId = SecurityUtils.currentUser().userId();
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<DocumentListResponse> documents = documentService.getDocuments(userId, pageRequest);
		return ApiResponse.success(PageResponse.from(documents));
	}

	@GetMapping("/{documentId}")
	public ApiResponse<DocumentDetailResponse> getDocument(@PathVariable Long documentId) {
		Long userId = SecurityUtils.currentUser().userId();
		return ApiResponse.success(documentService.getDocument(documentId, userId));
	}

	@PostMapping("/{documentId}/review")
	public ApiResponse<DocumentReviewResponse> review(
		@PathVariable Long documentId,
		@Valid @RequestBody DocumentReviewRequest request
	) {
		Long userId = SecurityUtils.currentUser().userId();
		return ApiResponse.success(documentReviewService.review(documentId, userId, request));
	}

	@DeleteMapping("/{documentId}")
	public ApiResponse<DocumentDeleteResponse> deleteDocument(@PathVariable Long documentId) {
		Long userId = SecurityUtils.currentUser().userId();
		return ApiResponse.success(documentService.deleteDocument(documentId, userId));
	}
}
