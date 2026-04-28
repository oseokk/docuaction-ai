package com.docuaction.file.service;

public record StoredFile(
	String originalFileName,
	String storedFileName,
	String filePath,
	long fileSize,
	String mimeType
) {
}

