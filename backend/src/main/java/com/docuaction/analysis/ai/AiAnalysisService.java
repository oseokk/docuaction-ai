package com.docuaction.analysis.ai;

import com.docuaction.analysis.dto.AiAnalysisResult;

public interface AiAnalysisService {

	AiAnalysisResult analyze(String ocrText);
}

