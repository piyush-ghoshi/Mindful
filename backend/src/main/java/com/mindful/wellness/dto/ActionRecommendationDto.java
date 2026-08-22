package com.mindful.wellness.dto;

import com.mindful.wellness.entity.ActionPriority;
import com.mindful.wellness.entity.ActionStatus;
import com.mindful.wellness.entity.ActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for ActionRecommendation entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionRecommendationDto {
    
    private UUID id;
    private UUID userId;
    private UUID sessionId;
    private UUID riskAssessmentId;
    
    // Type & Priority
    private ActionType actionType;
    private ActionPriority priority;
    
    // Details
    private String title;
    private String description;
    private String specificAction;
    
    // Resources
    private String resourceType;
    private UUID resourceId;
    private String resourceUrl;
    
    // Status
    private ActionStatus status;
    private LocalDateTime viewedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime dismissedAt;
    
    // Metadata
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private boolean isExpired;
    private boolean isCritical;
    private boolean isUrgent;
}
