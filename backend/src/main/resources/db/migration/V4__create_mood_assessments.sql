-- =============================================================================
-- Phase 1: Enhanced Mood & Sentiment Analysis
-- V4__create_mood_assessments.sql
-- =============================================================================
-- Creates mood_assessments table for storing AI-powered mood analysis results
-- Tracks mood, sentiment, and intensity for each chat message
-- =============================================================================

-- ── Mood Assessments ──────────────────────────────────────────────────────────
CREATE TABLE mood_assessments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id          UUID NOT NULL,
    message_id          UUID NOT NULL,
    user_id             UUID NOT NULL,
    
    -- Mood Analysis
    detected_mood       VARCHAR(50) NOT NULL,  -- HAPPY, SAD, ANXIOUS, ANGRY, etc.
    mood_intensity      NUMERIC(3,2) NOT NULL CHECK (mood_intensity >= 0.0 AND mood_intensity <= 1.0),
    sentiment_score     NUMERIC(3,2) NOT NULL CHECK (sentiment_score >= -1.0 AND sentiment_score <= 1.0),
    
    -- Secondary Emotions (JSON array of detected emotions)
    dominant_emotions   TEXT,  -- JSON: ["sadness", "fatigue", "worry"]
    
    -- Analysis Metadata
    analysis_method     VARCHAR(20) NOT NULL DEFAULT 'KEYWORD',  -- AI, KEYWORD, HYBRID
    confidence_score    NUMERIC(3,2) CHECK (confidence_score >= 0.0 AND confidence_score <= 1.0),
    
    -- Risk Indicators (will be used in Phase 2, but storing early)
    risk_indicators     TEXT,  -- JSON: ["hopelessness", "isolation"]
    
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (message_id) REFERENCES chat_messages(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_mood_session ON mood_assessments(session_id);
CREATE INDEX idx_mood_message ON mood_assessments(message_id);
CREATE INDEX idx_mood_user ON mood_assessments(user_id);
CREATE INDEX idx_mood_created_at ON mood_assessments(created_at);
CREATE INDEX idx_mood_detected ON mood_assessments(detected_mood);

-- Index for querying by mood intensity (find high-intensity moods)
CREATE INDEX idx_mood_intensity ON mood_assessments(mood_intensity DESC);

-- Index for sentiment analysis queries
CREATE INDEX idx_mood_sentiment ON mood_assessments(sentiment_score);

-- Composite index for user mood history queries
CREATE INDEX idx_mood_user_time ON mood_assessments(user_id, created_at DESC);

-- Comment
COMMENT ON TABLE mood_assessments IS 'AI-powered mood and sentiment analysis for each chat message';
COMMENT ON COLUMN mood_assessments.detected_mood IS 'Primary mood detected: HAPPY, SAD, ANXIOUS, ANGRY, FRUSTRATED, HOPEFUL, HOPELESS, CALM, OVERWHELMED, NEUTRAL';
COMMENT ON COLUMN mood_assessments.mood_intensity IS 'Intensity of the detected mood from 0.0 (barely detectable) to 1.0 (very strong)';
COMMENT ON COLUMN mood_assessments.sentiment_score IS 'Overall sentiment from -1.0 (very negative) to 1.0 (very positive)';
COMMENT ON COLUMN mood_assessments.analysis_method IS 'How mood was detected: AI (LLM-based), KEYWORD (rule-based), HYBRID (both)';
COMMENT ON COLUMN mood_assessments.confidence_score IS 'AI confidence in the analysis from 0.0 to 1.0';
