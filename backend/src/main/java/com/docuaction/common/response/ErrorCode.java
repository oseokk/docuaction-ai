package com.docuaction.common.response;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "Invalid request."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "Authentication is required."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403", "Access is denied."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "Resource not found."),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "Internal server error."),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_409", "Email already exists."),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_401", "Invalid email or password."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_402", "Invalid or expired token."),
	INVALID_FILE(HttpStatus.BAD_REQUEST, "DOCUMENT_400", "Invalid file."),
	FILE_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DOCUMENT_500", "Failed to store file.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}
