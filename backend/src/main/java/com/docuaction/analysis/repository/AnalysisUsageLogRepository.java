package com.docuaction.analysis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.docuaction.analysis.entity.AnalysisUsageLog;

public interface AnalysisUsageLogRepository extends JpaRepository<AnalysisUsageLog, Long> {

	List<AnalysisUsageLog> findByAnalysisJobIdOrderByIdAsc(Long analysisJobId);
}
