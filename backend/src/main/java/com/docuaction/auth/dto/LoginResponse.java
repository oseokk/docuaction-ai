package com.docuaction.auth.dto;

public record LoginResponse(
	String accessToken,
	String refreshToken,
	String tokenType
) {
}

