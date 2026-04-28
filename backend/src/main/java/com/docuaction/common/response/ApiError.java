package com.docuaction.common.response;

public record ApiError(
	String code,
	String message
) {

	public static ApiError of(ErrorCode errorCode) {
		return new ApiError(errorCode.getCode(), errorCode.getMessage());
	}

	public static ApiError of(ErrorCode errorCode, String message) {
		return new ApiError(errorCode.getCode(), message);
	}
}

