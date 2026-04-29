package com.docuaction.document.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.docuaction.action.dto.ActionResponse;
import com.docuaction.action.repository.DocumentActionRepository;
import com.docuaction.analysis.service.AnalysisJobService;
import com.docuaction.common.exception.BusinessException;
import com.docuaction.common.response.ErrorCode;
import com.docuaction.document.dto.DocumentDetailResponse;
import com.docuaction.document.dto.DocumentDeleteResponse;
import com.docuaction.document.dto.DocumentListResponse;
import com.docuaction.document.dto.DocumentUploadResponse;
import com.docuaction.document.entity.Document;
import com.docuaction.document.entity.DocumentAnalysisStatus;
import com.docuaction.document.entity.DocumentType;
import com.docuaction.document.repository.DocumentRepository;
import com.docuaction.document.repository.DocumentFieldRepository;
import com.docuaction.file.service.StoredFile;
import com.docuaction.file.service.FileStorageService;
import com.docuaction.user.entity.User;
import com.docuaction.user.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class DocumentService {

	private final DocumentRepository documentRepository;
	private final DocumentFieldRepository documentFieldRepository;
	private final DocumentActionRepository documentActionRepository;
	private final UserRepository userRepository;
	private final FileStorageService fileStorageService;
	private final AnalysisJobService analysisJobService;

	public DocumentService(
		DocumentRepository documentRepository,
		DocumentFieldRepository documentFieldRepository,
		DocumentActionRepository documentActionRepository,
		UserRepository userRepository,
		FileStorageService fileStorageService,
		AnalysisJobService analysisJobService
	) {
		this.documentRepository = documentRepository;
		this.documentFieldRepository = documentFieldRepository;
		this.documentActionRepository = documentActionRepository;
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

	public Page<DocumentListResponse> getDocuments(
		Long userId,
		DocumentType documentType,
		DocumentAnalysisStatus analysisStatus,
		Pageable pageable
	) {
		return documentRepository.findUserDocuments(userId, documentType, analysisStatus, pageable)
			.map(DocumentListResponse::from);
	}

	public DocumentDetailResponse getDocument(Long documentId, Long userId) {
		Document document = documentRepository.findByIdAndUserIdAndDeletedFalse(documentId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Document not found."));

		return DocumentDetailResponse.from(
			document,
			documentFieldRepository.findByDocumentIdOrderByIdAsc(document.getId()),
			documentActionRepository.findByDocumentIdOrderByActionDateAscIdAsc(document.getId())
				.stream()
				.map(ActionResponse::from)
				.toList()
		);
	}

	@Transactional
	public DocumentDeleteResponse deleteDocument(Long documentId, Long userId) {
		Document document = documentRepository.findByIdAndUserIdAndDeletedFalse(documentId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Document not found."));

		document.delete();

		return new DocumentDeleteResponse(document.getId(), "Document deleted.");
	}

	@Transactional
	public DocumentUploadResponse reanalyzeDocument(Long documentId, Long userId) {
		Document document = documentRepository.findByIdAndUserIdAndDeletedFalse(documentId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Document not found."));

		document.prepareReanalysis();
		documentFieldRepository.deleteByDocumentId(document.getId());
		documentActionRepository.deleteByDocumentId(document.getId());
		analysisJobService.createAndStartAfterCommit(document, document.getUser());

		return new DocumentUploadResponse(
			document.getId(),
			document.getAnalysisStatus().name(),
			"Document reanalysis is ready to start."
		);
	}
}
