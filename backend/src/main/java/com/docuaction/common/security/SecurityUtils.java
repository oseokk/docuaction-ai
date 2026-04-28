package com.docuaction.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.docuaction.common.exception.BusinessException;
import com.docuaction.common.response.ErrorCode;

public final class SecurityUtils {

	private SecurityUtils() {
	}

	public static AuthenticatedUser currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
		return authenticatedUser;
	}
}

