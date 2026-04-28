package com.docuaction.action.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docuaction.action.dto.ActionResponse;
import com.docuaction.action.entity.DocumentActionStatus;
import com.docuaction.action.repository.DocumentActionRepository;

@Service
@Transactional(readOnly = true)
public class ActionQueryService {

	private final DocumentActionRepository documentActionRepository;

	public ActionQueryService(DocumentActionRepository documentActionRepository) {
		this.documentActionRepository = documentActionRepository;
	}

	public List<ActionResponse> getUpcomingActions(Long userId) {
		return documentActionRepository
			.findByUserIdAndStatusAndActionDateGreaterThanEqualOrderByActionDateAscIdAsc(
				userId,
				DocumentActionStatus.PENDING,
				LocalDate.now()
			)
			.stream()
			.map(ActionResponse::from)
			.toList();
	}
}

