package com.docuaction.auth.service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.docuaction.auth.entity.RefreshToken;
import com.docuaction.auth.repository.RefreshTokenRepository;
import com.docuaction.common.exception.BusinessException;
import com.docuaction.common.response.ErrorCode;
import com.docuaction.user.entity.User;

@Service
public class RefreshTokenService {

	private static final int TOKEN_BYTE_LENGTH = 32;

	private final RefreshTokenRepository refreshTokenRepository;
	private final SecureRandom secureRandom = new SecureRandom();
	private final long refreshTokenExpirationSeconds;

	public RefreshTokenService(
		RefreshTokenRepository refreshTokenRepository,
		@Value("${docuaction.jwt.refresh-token-expiration-seconds}") long refreshTokenExpirationSeconds
	) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
	}

	public String createRefreshToken(User user) {
		String plainToken = generateToken();
		String tokenHash = hash(plainToken);
		RefreshToken refreshToken = new RefreshToken(
			user,
			tokenHash,
			Instant.now().plusSeconds(refreshTokenExpirationSeconds)
		);
		refreshTokenRepository.save(refreshToken);
		return plainToken;
	}

	public User consumeAndRotate(String plainToken) {
		RefreshToken refreshToken = refreshTokenRepository.findByTokenHashAndRevokedFalse(hash(plainToken))
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN, "Invalid refresh token."));

		if (refreshToken.isExpired() || refreshToken.isRevoked()) {
			refreshToken.revoke();
			throw new BusinessException(ErrorCode.INVALID_TOKEN, "Invalid refresh token.");
		}

		refreshToken.revoke();
		return refreshToken.getUser();
	}

	private String generateToken() {
		byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String hash(String plainToken) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(plainToken.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to hash refresh token.", exception);
		}
	}
}
