package com.docuaction.action.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docuaction.action.dto.ActionCompleteResponse;
import com.docuaction.action.entity.DocumentAction;
import com.docuaction.action.repository.DocumentActionRepository;
import com.docuaction.common.exception.BusinessException;
import com.docuaction.common.response.ErrorCode;

@Service
@Transactional(readOnly = true)
public class ActionCommandService {

	private final DocumentActionRepository documentActionRepository;

	public ActionCommandService(DocumentActionRepository documentActionRepository) {
		this.documentActionRepository = documentActionRepository;
	}

	@Transactional
	public ActionCompleteResponse completeAction(Long actionId, Long userId) {
		DocumentAction action = documentActionRepository.findByIdAndUserId(actionId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Action not found."));

		action.complete();

		return new ActionCompleteResponse(
			action.getId(),
			action.getStatus().name(),
			"Action completed."
		);
	}
}

