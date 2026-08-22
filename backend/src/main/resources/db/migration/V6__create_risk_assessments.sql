-- =============================================================================
-- Phase 2: Real-Time Severity Assessment
-- V6__create_risk_assessments.sql
-- =============================================================================
-- Creates risk_assessments table for multi-factor risk scoring
-- Tracks risk levels, factors, and protective elements
-- =============================================================================

-- ── Risk Assessments ──────────────────────────────────────────────────────────
CREATE TABLE risk_assessments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id          UUID NOT NULL,
    message_id          UUID REFERENCES chat_messages(id),
    user_id             UUID NOT NULL,
    
    -- Risk Scoring (0-10 scale)
    total_risk_score    INTEGER NOT NULL CHECK (total_risk_score >= 0 AND total_risk_score <= 10),
    
    -- Risk Factors (JSON: {"suicidal_ideation": 10, "hopelessness": 7, ...})
    risk_factors        JSONB,
    
    -- Protective Factors (JSON: {"social_support": true, "coping_skills": 0.6, ...})
    protective_factors  JSONB,
    
    -- Analysis Metadata
    assessment_method   VARCHAR(20) DEFAULT 'HYBRID', -- AI, KEYWORD, HYBRID
    confidence_score    NUMERIC(3,2),
    
    -- Recommended Action
    recommended_action  VARCHAR(50), -- CRISIS_INTERVENTION, URGENT_APPOINTMENT, etc.
    rationale           TEXT,
    
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_risk_session ON risk_assessments(session_id);
CREATE INDEX idx_risk_user ON risk_assessments(user_id);
CREATE INDEX idx_risk_score ON risk_assessments(total_risk_score DESC);
CREATE INDEX idx_risk_created_at ON risk_assessments(created_at DESC);
CREATE INDEX idx_risk_action ON risk_assessments(recommended_action);

-- Composite index for user risk history
CREATE INDEX idx_risk_user_time ON risk_assessments(user_id, created_at DESC);

-- Update chat_sessions to add risk tracking
ALTER TABLE chat_sessions ADD COLUMN IF NOT EXISTS highest_risk_score INTEGER DEFAULT 0 
    CHECK (highest_risk_score >= 0 AND highest_risk_score <= 10);

-- Comments
COMMENT ON TABLE risk_assessments IS 'Multi-factor risk assessments for mental health crisis detection';
COMMENT ON COLUMN risk_assessments.total_risk_score IS 'Overall risk score from 0 (minimal) to 10 (critical/imminent danger)';
COMMENT ON COLUMN risk_assessments.risk_factors IS 'JSON object with detected risk factors and their weights';
COMMENT ON COLUMN risk_assessments.protective_factors IS 'JSON object with protective factors that reduce risk';
COMMENT ON COLUMN risk_assessments.recommended_action IS 'Suggested intervention based on risk level';
