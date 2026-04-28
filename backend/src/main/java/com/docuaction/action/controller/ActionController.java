package com.docuaction.action.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.docuaction.action.dto.ActionCompleteResponse;
import com.docuaction.action.dto.ActionResponse;
import com.docuaction.action.service.ActionCommandService;
import com.docuaction.action.service.ActionQueryService;
import com.docuaction.common.response.ApiResponse;
import com.docuaction.common.security.SecurityUtils;

@RestController
@RequestMapping("/api/actions")
public class ActionController {

	private final ActionQueryService actionQueryService;
	private final ActionCommandService actionCommandService;

	public ActionController(
		ActionQueryService actionQueryService,
		ActionCommandService actionCommandService
	) {
		this.actionQueryService = actionQueryService;
		this.actionCommandService = actionCommandService;
	}

	@GetMapping("/upcoming")
	public ApiResponse<List<ActionResponse>> getUpcomingActions() {
		Long userId = SecurityUtils.currentUser().userId();
		return ApiResponse.success(actionQueryService.getUpcomingActions(userId));
	}

	@PostMapping("/{actionId}/complete")
	public ApiResponse<ActionCompleteResponse> completeAction(@PathVariable Long actionId) {
		Long userId = SecurityUtils.currentUser().userId();
		return ApiResponse.success(actionCommandService.completeAction(actionId, userId));
	}
}
