package com.docuaction.user.dto;

public record UserMeResponse(
	Long userId,
	String email,
	String name
) {
}

