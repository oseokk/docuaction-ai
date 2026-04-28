package com.docuaction.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.docuaction.common.exception.BusinessException;
import com.docuaction.common.response.ErrorCode;
import com.docuaction.common.security.AuthenticatedUser;
import com.docuaction.user.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtTokenProvider {

	private static final String HMAC_SHA256 = "HmacSHA256";

	private final ObjectMapper objectMapper;
	private final byte[] secret;
	private final long accessTokenExpirationSeconds;
	private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
	private final Base64.Decoder decoder = Base64.getUrlDecoder();

	public JwtTokenProvider(
		ObjectMapper objectMapper,
		@Value("${docuaction.jwt.secret}") String secret,
		@Value("${docuaction.jwt.access-token-expiration-seconds}") long accessTokenExpirationSeconds
	) {
		this.objectMapper = objectMapper;
		this.secret = secret.getBytes(StandardCharsets.UTF_8);
		this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
	}

	public String createAccessToken(User user) {
		Instant now = Instant.now();
		Map<String, Object> header = Map.of(
			"alg", "HS256",
			"typ", "JWT"
		);
		Map<String, Object> payload = Map.of(
			"sub", String.valueOf(user.getId()),
			"email", user.getEmail(),
			"name", user.getName(),
			"iat", now.getEpochSecond(),
			"exp", now.plusSeconds(accessTokenExpirationSeconds).getEpochSecond()
		);

		String unsignedToken = base64Json(header) + "." + base64Json(payload);
		return unsignedToken + "." + sign(unsignedToken);
	}

	public boolean isValid(String token) {
		try {
			Map<String, Object> payload = parsePayload(token);
			long expiration = ((Number) payload.get("exp")).longValue();
			return expiration > Instant.now().getEpochSecond();
		} catch (RuntimeException exception) {
			return false;
		}
	}

	public AuthenticatedUser parseAuthenticatedUser(String token) {
		Map<String, Object> payload = parsePayload(token);
		return new AuthenticatedUser(
			Long.valueOf((String) payload.get("sub")),
			(String) payload.get("email"),
			(String) payload.get("name")
		);
	}

	private Map<String, Object> parsePayload(String token) {
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN);
		}

		String unsignedToken = parts[0] + "." + parts[1];
		String expectedSignature = sign(unsignedToken);
		if (!constantTimeEquals(expectedSignature, parts[2])) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN);
		}

		try {
			byte[] payloadBytes = decoder.decode(parts[1]);
			return objectMapper.readValue(payloadBytes, new TypeReference<>() {
			});
		} catch (Exception exception) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN);
		}
	}

	private String base64Json(Map<String, Object> json) {
		try {
			byte[] bytes = objectMapper.writeValueAsBytes(json);
			return encoder.encodeToString(bytes);
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to create JWT payload.", exception);
		}
	}

	private String sign(String value) {
		try {
			Mac mac = Mac.getInstance(HMAC_SHA256);
			mac.init(new SecretKeySpec(secret, HMAC_SHA256));
			return encoder.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to sign JWT.", exception);
		}
	}

	private boolean constantTimeEquals(String expected, String actual) {
		return MessageDigestUtils.constantTimeEquals(
			expected.getBytes(StandardCharsets.UTF_8),
			actual.getBytes(StandardCharsets.UTF_8)
		);
	}
}

