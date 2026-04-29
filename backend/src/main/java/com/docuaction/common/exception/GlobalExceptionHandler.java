package com.docuaction.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.docuaction.common.response.ApiError;
import com.docuaction.common.response.ApiResponse;
import com.docuaction.common.response.ErrorCode;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.error(ApiError.of(errorCode, exception.getMessage())));
	}

	@ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
	public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception exception) {
		return ResponseEntity
			.status(ErrorCode.INVALID_REQUEST.getStatus())
			.body(ApiResponse.error(ApiError.of(ErrorCode.INVALID_REQUEST, "Request validation failed.")));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException() {
		return ResponseEntity
			.status(ErrorCode.INVALID_REQUEST.getStatus())
			.body(ApiResponse.error(ApiError.of(ErrorCode.INVALID_REQUEST, "Invalid request parameter.")));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException() {
		return ResponseEntity
			.status(ErrorCode.FORBIDDEN.getStatus())
			.body(ApiResponse.error(ApiError.of(ErrorCode.FORBIDDEN)));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException() {
		return ResponseEntity
			.status(ErrorCode.INTERNAL_ERROR.getStatus())
			.body(ApiResponse.error(ApiError.of(ErrorCode.INTERNAL_ERROR)));
	}
}

