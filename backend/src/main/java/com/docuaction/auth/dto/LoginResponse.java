package com.docuaction.auth.dto;

public record LoginResponse(
	String accessToken,
	String tokenType
) {
}

