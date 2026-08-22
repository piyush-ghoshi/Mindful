package com.mindful.wellness.dto;

import com.mindful.wellness.entity.AnalysisMethod;
import com.mindful.wellness.entity.RecommendedAction;
import com.mindful.wellness.entity.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for RiskAssessment entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessmentDto {
    
    private UUID id;
    private UUID sessionId;
    private UUID messageId;
    private UUID userId;
    
    // Risk Scoring
    private Integer totalRiskScore;
    private RiskLevel riskLevel;
    private Map<String, Integer> riskFactors;
    private Map<String, Object> protectiveFactors;
    
    // Analysis
    private AnalysisMethod assessmentMethod;
    private BigDecimal confidenceScore;
    
    // Recommendations
    private RecommendedAction recommendedAction;
    private String rationale;
    
    private LocalDateTime createdAt;
    
    // Computed fields
    private boolean isCrisis;
    private boolean requiresImmediateAction;
    private String description;
}
