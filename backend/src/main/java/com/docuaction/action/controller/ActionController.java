package com.docuaction.action.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.docuaction.action.dto.ActionResponse;
import com.docuaction.action.service.ActionQueryService;
import com.docuaction.common.response.ApiResponse;
import com.docuaction.common.security.SecurityUtils;

@RestController
@RequestMapping("/api/actions")
public class ActionController {

	private final ActionQueryService actionQueryService;

	public ActionController(ActionQueryService actionQueryService) {
		this.actionQueryService = actionQueryService;
	}

	@GetMapping("/upcoming")
	public ApiResponse<List<ActionResponse>> getUpcomingActions() {
		Long userId = SecurityUtils.currentUser().userId();
		return ApiResponse.success(actionQueryService.getUpcomingActions(userId));
	}
}

