-- =============================================================================
-- Phase 1: Enhanced Mood & Sentiment Analysis
-- V5__alter_chat_tables_mood.sql
-- =============================================================================
-- Adds mood-related columns to existing chat_messages and chat_sessions tables
-- These columns provide quick access to mood data without joining mood_assessments
-- =============================================================================

-- ── Update chat_messages table ────────────────────────────────────────────────
-- Add mood detection columns for quick reference
ALTER TABLE chat_messages ADD COLUMN IF NOT EXISTS mood_detected VARCHAR(50);
ALTER TABLE chat_messages ADD COLUMN IF NOT EXISTS sentiment_score NUMERIC(3,2);
ALTER TABLE chat_messages ADD COLUMN IF NOT EXISTS risk_indicators TEXT;

-- Add check constraint for sentiment_score
ALTER TABLE chat_messages ADD CONSTRAINT check_sentiment_score 
    CHECK (sentiment_score IS NULL OR (sentiment_score >= -1.0 AND sentiment_score <= 1.0));

-- Create index for mood queries
CREATE INDEX IF NOT EXISTS idx_chat_messages_mood ON chat_messages(mood_detected);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sentiment ON chat_messages(sentiment_score);

-- Comments
COMMENT ON COLUMN chat_messages.mood_detected IS 'Quick reference to detected mood (denormalized from mood_assessments)';
COMMENT ON COLUMN chat_messages.sentiment_score IS 'Quick reference to sentiment score (denormalized from mood_assessments)';
COMMENT ON COLUMN chat_messages.risk_indicators IS 'JSON array of detected risk indicators for this message';

-- ── Update chat_sessions table ────────────────────────────────────────────────
-- Add session-level mood and risk aggregates
ALTER TABLE chat_sessions ADD COLUMN IF NOT EXISTS average_mood_score NUMERIC(3,2);
ALTER TABLE chat_sessions ADD COLUMN IF NOT EXISTS average_risk_score NUMERIC(3,1);
ALTER TABLE chat_sessions ADD COLUMN IF NOT EXISTS crisis_detected BOOLEAN DEFAULT FALSE;
ALTER TABLE chat_sessions ADD COLUMN IF NOT EXISTS action_plan_generated BOOLEAN DEFAULT FALSE;

-- Add check constraints
ALTER TABLE chat_sessions ADD CONSTRAINT check_avg_mood_score 
    CHECK (average_mood_score IS NULL OR (average_mood_score >= -1.0 AND average_mood_score <= 1.0));
    
ALTER TABLE chat_sessions ADD CONSTRAINT check_avg_risk_score 
    CHECK (average_risk_score IS NULL OR (average_risk_score >= 0.0 AND average_risk_score <= 10.0));

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_chat_sessions_crisis ON chat_sessions(crisis_detected);
CREATE INDEX IF NOT EXISTS idx_chat_sessions_avg_risk ON chat_sessions(average_risk_score DESC);

-- Comments
COMMENT ON COLUMN chat_sessions.average_mood_score IS 'Average sentiment across all messages in this session';
COMMENT ON COLUMN chat_sessions.average_risk_score IS 'Average risk score (0-10) across all assessments in this session';
COMMENT ON COLUMN chat_sessions.crisis_detected IS 'Flag indicating if any crisis-level risk was detected in this session';
COMMENT ON COLUMN chat_sessions.action_plan_generated IS 'Flag indicating if action recommendations have been generated';
