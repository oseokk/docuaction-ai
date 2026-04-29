package com.docuaction.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docuaction.auth.dto.LoginRequest;
import com.docuaction.auth.dto.LoginResponse;
import com.docuaction.auth.dto.SignupRequest;
import com.docuaction.auth.dto.SignupResponse;
import com.docuaction.common.exception.BusinessException;
import com.docuaction.common.response.ErrorCode;
import com.docuaction.user.entity.User;
import com.docuaction.user.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;

	public AuthService(
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		JwtTokenProvider jwtTokenProvider,
		RefreshTokenService refreshTokenService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.refreshTokenService = refreshTokenService;
	}

	@Transactional
	public SignupResponse signup(SignupRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
		}

		User user = new User(
			request.email(),
			passwordEncoder.encode(request.password()),
			request.name()
		);
		User savedUser = userRepository.save(user);

		return new SignupResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getName());
	}

	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
		}

		String accessToken = jwtTokenProvider.createAccessToken(user);
		String refreshToken = refreshTokenService.createRefreshToken(user);
		return new LoginResponse(accessToken, refreshToken, "Bearer");
	}

	@Transactional
	public LoginResponse refresh(String refreshToken) {
		User user = refreshTokenService.consumeAndRotate(refreshToken);
		String newAccessToken = jwtTokenProvider.createAccessToken(user);
		String newRefreshToken = refreshTokenService.createRefreshToken(user);
		return new LoginResponse(newAccessToken, newRefreshToken, "Bearer");
	}
}

