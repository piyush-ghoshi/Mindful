-- =============================================================================
-- Phase 4: Crisis Intervention & Emergency Response
-- V8__create_crisis_interventions.sql
-- =============================================================================
-- Creates tables for crisis detection, emergency contacts, and interventions
-- Enables immediate response to suicide/self-harm risks
-- =============================================================================

-- ── Emergency Contacts ─────────────────────────────────────────────────────────
CREATE TABLE emergency_contacts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL,
    
    -- Contact Information
    contact_name        VARCHAR(200) NOT NULL,
    relationship        VARCHAR(100), -- Parent, Friend, Sibling, Partner, etc.
    phone_number        VARCHAR(20) NOT NULL,
    email               VARCHAR(255),
    
    -- Contact Preferences
    priority_order      INTEGER DEFAULT 1, -- Order to contact (1 = first)
    can_contact_crisis  BOOLEAN DEFAULT true, -- Can be notified in crisis
    preferred_method    VARCHAR(20) DEFAULT 'PHONE', -- PHONE, SMS, EMAIL
    
    -- Metadata
    is_verified         BOOLEAN DEFAULT false,
    verified_at         TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ── Crisis Interventions ───────────────────────────────────────────────────────
CREATE TABLE crisis_interventions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL,
    session_id          UUID,
    risk_assessment_id  UUID REFERENCES risk_assessments(id),
    message_id          UUID,
    
    -- Crisis Details
    crisis_type         VARCHAR(50) NOT NULL, -- SUICIDAL_IDEATION, SELF_HARM, SEVERE_DISTRESS, SUBSTANCE_CRISIS
    severity_level      INTEGER NOT NULL CHECK (severity_level >= 0 AND severity_level <= 10),
    trigger_reason      TEXT, -- What triggered this crisis intervention
    
    -- Intervention Actions
    intervention_type   VARCHAR(50) NOT NULL, -- AUTO_HELPLINE, COUNSELLOR_ALERT, EMERGENCY_CONTACT, SAFETY_PLAN
    status              VARCHAR(20) NOT NULL DEFAULT 'INITIATED', -- INITIATED, IN_PROGRESS, RESOLVED, ESCALATED
    
    -- Actions Taken
    helpline_provided   BOOLEAN DEFAULT false,
    emergency_contacted BOOLEAN DEFAULT false,
    counsellor_alerted  BOOLEAN DEFAULT false,
    safety_plan_created BOOLEAN DEFAULT false,
    
    -- Contact Attempts
    contact_attempts    JSONB, -- Log of contact attempts {"attempts": [{"time": "...", "method": "...", "status": "..."}]}
    
    -- Resolution
    resolved_at         TIMESTAMP,
    resolution_notes    TEXT,
    follow_up_required  BOOLEAN DEFAULT true,
    follow_up_date      TIMESTAMP,
    
    -- Metadata
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE SET NULL
);

-- ── Crisis Hotlines (Reference Data) ───────────────────────────────────────────
CREATE TABLE crisis_hotlines (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country_code        VARCHAR(5) DEFAULT 'IN',
    
    -- Hotline Information
    hotline_name        VARCHAR(200) NOT NULL,
    phone_number        VARCHAR(20) NOT NULL,
    description         TEXT,
    
    -- Availability
    is_24_7             BOOLEAN DEFAULT true,
    operating_hours     VARCHAR(200),
    languages           VARCHAR(200), -- Comma-separated: "English, Hindi, Tamil"
    
    -- Specialty
    specialty           VARCHAR(100), -- General, Youth, LGBTQ+, Veterans, Elderly, etc.
    
    -- Contact Methods
    has_phone           BOOLEAN DEFAULT true,
    has_sms             BOOLEAN DEFAULT false,
    has_chat            BOOLEAN DEFAULT false,
    has_email           BOOLEAN DEFAULT false,
    website_url         VARCHAR(500),
    
    -- Metadata
    is_active           BOOLEAN DEFAULT true,
    display_order       INTEGER DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for emergency_contacts
CREATE INDEX idx_emergency_user ON emergency_contacts(user_id);
CREATE INDEX idx_emergency_priority ON emergency_contacts(user_id, priority_order);
CREATE INDEX idx_emergency_crisis_flag ON emergency_contacts(user_id, can_contact_crisis);

-- Indexes for crisis_interventions
CREATE INDEX idx_crisis_user ON crisis_interventions(user_id);
CREATE INDEX idx_crisis_session ON crisis_interventions(session_id);
CREATE INDEX idx_crisis_risk ON crisis_interventions(risk_assessment_id);
CREATE INDEX idx_crisis_type ON crisis_interventions(crisis_type);
CREATE INDEX idx_crisis_status ON crisis_interventions(status);
CREATE INDEX idx_crisis_severity ON crisis_interventions(severity_level DESC);
CREATE INDEX idx_crisis_created_at ON crisis_interventions(created_at DESC);
CREATE INDEX idx_crisis_follow_up ON crisis_interventions(follow_up_required, follow_up_date);

-- Indexes for crisis_hotlines
CREATE INDEX idx_hotline_country ON crisis_hotlines(country_code);
CREATE INDEX idx_hotline_active ON crisis_hotlines(is_active);
CREATE INDEX idx_hotline_order ON crisis_hotlines(display_order);

-- Triggers for updated_at
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER emergency_contact_updated
BEFORE UPDATE ON emergency_contacts
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER crisis_intervention_updated
BEFORE UPDATE ON crisis_interventions
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER crisis_hotline_updated
BEFORE UPDATE ON crisis_hotlines
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

-- Insert default Indian crisis hotlines
INSERT INTO crisis_hotlines (hotline_name, phone_number, description, is_24_7, languages, country_code, display_order) VALUES
('iCall - TISS', '9152987821', 'Psychosocial helpline by Tata Institute of Social Sciences. Professional counselors available.', true, 'English, Hindi, Marathi', 'IN', 1),
('Vandrevala Foundation', '1860-2662-345', '24/7 mental health support and crisis intervention helpline', true, 'English, Hindi', 'IN', 2),
('Vandrevala Foundation', '1800-2333-330', 'Toll-free mental health helpline', true, 'English, Hindi', 'IN', 3),
('AASRA', '91-9820466726', '24/7 crisis intervention center providing emotional support', true, 'English, Hindi', 'IN', 4),
('MPower 1on1', '1800-120-820-050', 'Mental health helpline by Mpower', false, 'English, Hindi', 'IN', 5),
('Snehi', '91-22-27546669', 'Crisis intervention center in Mumbai', false, 'English, Hindi, Marathi', 'IN', 6),
('Connecting Trust', '91-11-41198666', 'Mental health support helpline in Delhi', false, 'English, Hindi', 'IN', 7),
('Fortis Stress Helpline', '91-8376804102', 'Stress and mental health support', false, 'English, Hindi', 'IN', 8);

-- Comments
COMMENT ON TABLE emergency_contacts IS 'User-defined emergency contacts for crisis situations';
COMMENT ON TABLE crisis_interventions IS 'Log of crisis interventions and actions taken';
COMMENT ON TABLE crisis_hotlines IS 'Database of crisis hotlines and mental health helplines';

COMMENT ON COLUMN crisis_interventions.severity_level IS 'Crisis severity from 0-10 (same scale as risk assessment)';
COMMENT ON COLUMN crisis_interventions.status IS 'Current status: INITIATED, IN_PROGRESS, RESOLVED, ESCALATED';
COMMENT ON COLUMN crisis_interventions.follow_up_required IS 'Whether follow-up check-in is needed';
