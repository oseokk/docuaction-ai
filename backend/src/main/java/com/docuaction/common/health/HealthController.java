package com.docuaction.common.health;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.docuaction.common.response.ApiResponse;

@RestController
@RequestMapping("/api/health")
public class HealthController {

	@GetMapping
	public ApiResponse<HealthResponse> health() {
		return ApiResponse.success(new HealthResponse("UP", Instant.now()));
	}

	public record HealthResponse(String status, Instant checkedAt) {
	}
}
