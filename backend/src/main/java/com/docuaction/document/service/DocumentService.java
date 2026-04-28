package com.docuaction.document.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.docuaction.analysis.service.AnalysisJobService;
import com.docuaction.common.exception.BusinessException;
import com.docuaction.common.response.ErrorCode;
import com.docuaction.document.dto.DocumentDetailResponse;
import com.docuaction.document.dto.DocumentListResponse;
import com.docuaction.document.dto.DocumentUploadResponse;
import com.docuaction.document.entity.Document;
import com.docuaction.document.repository.DocumentRepository;
import com.docuaction.file.service.StoredFile;
import com.docuaction.file.service.FileStorageService;
import com.docuaction.user.entity.User;
import com.docuaction.user.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class DocumentService {

	private final DocumentRepository documentRepository;
	private final UserRepository userRepository;
	private final FileStorageService fileStorageService;
	private final AnalysisJobService analysisJobService;

	public DocumentService(
		DocumentRepository documentRepository,
		UserRepository userRepository,
		FileStorageService fileStorageService,
		AnalysisJobService analysisJobService
	) {
		this.documentRepository = documentRepository;
		this.userRepository = userRepository;
		this.fileStorageService = fileStorageService;
		this.analysisJobService = analysisJobService;
	}

	@Transactional
	public DocumentUploadResponse uploadDocument(MultipartFile file, Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

		StoredFile storedFile = fileStorageService.store(file, userId);
		Document document = new Document(
			user,
			storedFile.originalFileName(),
			storedFile.storedFileName(),
			storedFile.filePath(),
			storedFile.fileSize(),
			storedFile.mimeType()
		);
		Document savedDocument = documentRepository.save(document);
		analysisJobService.createAndStartAfterCommit(savedDocument, user);

		return new DocumentUploadResponse(
			savedDocument.getId(),
			savedDocument.getAnalysisStatus().name(),
			"Document uploaded. Analysis is ready to start."
		);
	}

	public Page<DocumentListResponse> getDocuments(Long userId, Pageable pageable) {
		return documentRepository.findByUserIdAndDeletedFalse(userId, pageable)
			.map(DocumentListResponse::from);
	}

	public DocumentDetailResponse getDocument(Long documentId, Long userId) {
		Document document = documentRepository.findByIdAndUserIdAndDeletedFalse(documentId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Document not found."));

		return DocumentDetailResponse.from(document);
	}
}
