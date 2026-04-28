package com.docuaction.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.docuaction.common.response.ApiResponse;
import com.docuaction.common.security.AuthenticatedUser;
import com.docuaction.common.security.SecurityUtils;
import com.docuaction.user.dto.UserMeResponse;

@RestController
@RequestMapping("/api/users")
public class UserController {

	@GetMapping("/me")
	public ApiResponse<UserMeResponse> me() {
		AuthenticatedUser currentUser = SecurityUtils.currentUser();
		return ApiResponse.success(new UserMeResponse(
			currentUser.userId(),
			currentUser.email(),
			currentUser.name()
		));
	}
}

