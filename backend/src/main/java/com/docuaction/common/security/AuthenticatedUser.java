package com.docuaction.common.security;

public record AuthenticatedUser(
	Long userId,
	String email,
	String name
) {
}

