package com.docuaction.file.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.docuaction.common.exception.BusinessException;
import com.docuaction.common.response.ErrorCode;

@Service
public class FileStorageService {

	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "pdf");
	private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "application/pdf");

	private final Path documentStoragePath;

	public FileStorageService(@Value("${docuaction.storage.document-path}") String documentStoragePath) {
		this.documentStoragePath = Path.of(documentStoragePath).toAbsolutePath().normalize();
	}

	public StoredFile store(MultipartFile file, Long userId) {
		validate(file);

		String originalFileName = sanitizeOriginalFileName(file.getOriginalFilename());
		String extension = extractExtension(originalFileName);
		String storedFileName = UUID.randomUUID() + "." + extension;
		Path userDirectory = documentStoragePath.resolve(String.valueOf(userId));
		Path targetPath = userDirectory.resolve(storedFileName).normalize();

		try {
			Files.createDirectories(userDirectory);
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception exception) {
			throw new BusinessException(ErrorCode.FILE_STORAGE_FAILED);
		}

		return new StoredFile(
			originalFileName,
			storedFileName,
			targetPath.toString(),
			file.getSize(),
			file.getContentType()
		);
	}

	private void validate(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_FILE, "File is required.");
		}
		if (file.getSize() > MAX_FILE_SIZE) {
			throw new BusinessException(ErrorCode.INVALID_FILE, "File size must be 10MB or less.");
		}

		String originalFileName = sanitizeOriginalFileName(file.getOriginalFilename());
		String extension = extractExtension(originalFileName);
		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw new BusinessException(ErrorCode.INVALID_FILE, "Only JPG, PNG, and PDF files are supported.");
		}

		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
			throw new BusinessException(ErrorCode.INVALID_FILE, "Unsupported MIME type.");
		}
	}

	private String sanitizeOriginalFileName(String originalFileName) {
		if (originalFileName == null || originalFileName.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_FILE, "Original filename is required.");
		}
		return Path.of(originalFileName).getFileName().toString();
	}

	private String extractExtension(String fileName) {
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
			throw new BusinessException(ErrorCode.INVALID_FILE, "File extension is required.");
		}
		return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
	}
}

