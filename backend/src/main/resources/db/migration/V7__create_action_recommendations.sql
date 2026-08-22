-- =============================================================================
-- Phase 3: Action Recommendations
-- V7__create_action_recommendations.sql
-- =============================================================================
-- Creates action_recommendations table for personalized intervention suggestions
-- Links recommendations to risk assessments and tracks implementation
-- =============================================================================

-- ── Action Recommendations ────────────────────────────────────────────────────
CREATE TABLE action_recommendations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL,
    session_id          UUID,
    risk_assessment_id  UUID REFERENCES risk_assessments(id),
    
    -- Recommendation Type
    action_type         VARCHAR(50) NOT NULL, -- BOOK_APPOINTMENT, DO_EXERCISE, MEDITATION, CALL_HELPLINE, etc.
    priority            VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', -- CRITICAL, HIGH, MEDIUM, LOW
    
    -- Recommendation Details
    title               VARCHAR(200) NOT NULL,
    description         TEXT NOT NULL,
    specific_action     TEXT, -- Step-by-step instructions
    
    -- Resources
    resource_type       VARCHAR(50), -- EXERCISE, MEDITATION, ARTICLE, VIDEO, HELPLINE
    resource_id         UUID, -- Foreign key to exercise/meditation tables
    resource_url        VARCHAR(500),
    
    -- Implementation Tracking
    status              VARCHAR(20) DEFAULT 'PENDING', -- PENDING, VIEWED, STARTED, COMPLETED, DISMISSED
    viewed_at           TIMESTAMP,
    started_at          TIMESTAMP,
    completed_at        TIMESTAMP,
    dismissed_at        TIMESTAMP,
    
    -- Metadata
    expires_at          TIMESTAMP, -- Some recommendations are time-sensitive
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE SET NULL
);

-- Indexes
CREATE INDEX idx_action_user ON action_recommendations(user_id);
CREATE INDEX idx_action_session ON action_recommendations(session_id);
CREATE INDEX idx_action_risk ON action_recommendations(risk_assessment_id);
CREATE INDEX idx_action_priority ON action_recommendations(priority);
CREATE INDEX idx_action_status ON action_recommendations(status);
CREATE INDEX idx_action_type ON action_recommendations(action_type);
CREATE INDEX idx_action_user_status ON action_recommendations(user_id, status);
CREATE INDEX idx_action_created_at ON action_recommendations(created_at DESC);

-- Trigger to update updated_at
CREATE OR REPLACE FUNCTION update_action_recommendation_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER action_recommendation_updated
BEFORE UPDATE ON action_recommendations
FOR EACH ROW
EXECUTE FUNCTION update_action_recommendation_timestamp();

-- Comments
COMMENT ON TABLE action_recommendations IS 'Personalized action recommendations based on risk assessments';
COMMENT ON COLUMN action_recommendations.action_type IS 'Type of recommended action';
COMMENT ON COLUMN action_recommendations.priority IS 'Urgency level: CRITICAL (crisis), HIGH (urgent), MEDIUM (soon), LOW (optional)';
COMMENT ON COLUMN action_recommendations.status IS 'Implementation status: PENDING, VIEWED, STARTED, COMPLETED, DISMISSED';
COMMENT ON COLUMN action_recommendations.expires_at IS 'Time-sensitive recommendations expire (e.g., crisis helpline calls)';
