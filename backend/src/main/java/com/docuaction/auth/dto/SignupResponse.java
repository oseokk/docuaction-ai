package com.docuaction.auth.dto;

public record SignupResponse(
	Long userId,
	String email,
	String name
) {
}

