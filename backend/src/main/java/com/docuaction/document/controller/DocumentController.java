package com.docuaction.document.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.docuaction.common.response.ApiResponse;
import com.docuaction.common.security.SecurityUtils;
import com.docuaction.document.dto.DocumentUploadResponse;
import com.docuaction.document.service.DocumentService;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

	private final DocumentService documentService;

	public DocumentController(DocumentService documentService) {
		this.documentService = documentService;
	}

	@PostMapping("/upload")
	public ApiResponse<DocumentUploadResponse> upload(@RequestPart("file") MultipartFile file) {
		Long userId = SecurityUtils.currentUser().userId();
		return ApiResponse.success(documentService.uploadDocument(file, userId));
	}
}

