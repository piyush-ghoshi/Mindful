-- =============================================================================
-- Mindful Wellness Platform — Complete Database Schema
-- All enum columns use VARCHAR so Hibernate @Enumerated(EnumType.STRING) works
-- without any casting. Matches every JPA entity exactly.
-- =============================================================================

-- ── Users ─────────────────────────────────────────────────────────────────────
CREATE TABLE users (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    firebase_uid         VARCHAR(255) UNIQUE,
    email                VARCHAR(255) NOT NULL UNIQUE,
    username             VARCHAR(100) UNIQUE,
    password_hash        VARCHAR(255) NOT NULL,
    first_name           VARCHAR(100),
    last_name            VARCHAR(100),
    phone_number         VARCHAR(20),
    profile_picture_url  VARCHAR(500),
    role                 VARCHAR(50)  NOT NULL DEFAULT 'STUDENT',
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,
    is_email_verified    BOOLEAN      NOT NULL DEFAULT FALSE,
    language_preference  VARCHAR(10)  DEFAULT 'en',
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    last_login_at        TIMESTAMP,
    CONSTRAINT users_email_format CHECK (
        email ~ '^[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}$'
    )
);

CREATE INDEX idx_users_email       ON users(email);
CREATE INDEX idx_users_firebase    ON users(firebase_uid);
CREATE INDEX idx_users_role        ON users(role);
CREATE INDEX idx_users_is_active   ON users(is_active);

-- ── Student Profiles ──────────────────────────────────────────────────────────
CREATE TABLE student_profiles (
    id                              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                         UUID NOT NULL UNIQUE,
    student_id                      VARCHAR(50) NOT NULL,
    institution_id                  UUID,
    date_of_birth                   DATE,
    gender                          VARCHAR(50),
    emergency_contact_name          VARCHAR(100),
    emergency_contact_phone         VARCHAR(20),
    consent_for_data_sharing        BOOLEAN NOT NULL DEFAULT FALSE,
    consent_for_anonymous_analytics BOOLEAN NOT NULL DEFAULT FALSE,
    preferred_counsellor_gender     VARCHAR(50),
    wellness_goals                  TEXT,
    notification_preferences        TEXT,
    privacy_settings                TEXT,
    created_at                      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_student_profiles_user_id    ON student_profiles(user_id);
CREATE INDEX idx_student_profiles_student_id ON student_profiles(student_id);

-- ── Counsellor Profiles ───────────────────────────────────────────────────────
CREATE TABLE counsellor_profiles (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                  UUID NOT NULL UNIQUE,
    counsellor_id            VARCHAR(50) NOT NULL,
    institution_id           UUID,
    license_number           VARCHAR(100) NOT NULL UNIQUE,
    specializations          TEXT,
    qualifications           TEXT,
    years_of_experience      INTEGER,
    bio                      TEXT,
    availability_schedule    TEXT,
    max_appointments_per_day INTEGER NOT NULL DEFAULT 8,
    appointment_duration     INTEGER NOT NULL DEFAULT 30,
    rating                   DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total_appointments       INTEGER NOT NULL DEFAULT 0,
    is_accepting_new_students BOOLEAN NOT NULL DEFAULT TRUE,
    created_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_counsellor_profiles_user_id  ON counsellor_profiles(user_id);
CREATE INDEX idx_counsellor_profiles_license  ON counsellor_profiles(license_number);

-- ── Appointments ──────────────────────────────────────────────────────────────
-- status is VARCHAR — Hibernate @Enumerated(EnumType.STRING) maps directly
CREATE TABLE appointments (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id            UUID NOT NULL,
    counsellor_id         UUID NOT NULL,
    scheduled_start_time  TIMESTAMP NOT NULL,
    scheduled_end_time    TIMESTAMP NOT NULL,
    status                VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    appointment_type      VARCHAR(50) DEFAULT 'IN_PERSON',
    reason                TEXT,
    student_notes         TEXT,
    counsellor_notes      TEXT,
    cancelled_by          UUID,
    cancellation_reason   TEXT,
    created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at          TIMESTAMP,
    FOREIGN KEY (student_id)   REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (counsellor_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT valid_appointment_times CHECK (scheduled_end_time > scheduled_start_time)
);

CREATE INDEX idx_appointments_student_id     ON appointments(student_id);
CREATE INDEX idx_appointments_counsellor_id  ON appointments(counsellor_id);
CREATE INDEX idx_appointments_status         ON appointments(status);
CREATE INDEX idx_appointments_scheduled_start ON appointments(scheduled_start_time);

-- ── Availability Schedules ────────────────────────────────────────────────────
CREATE TABLE availability_schedules (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    counsellor_id UUID NOT NULL,
    day_of_week   VARCHAR(20) NOT NULL,
    start_time    TIME NOT NULL,
    end_time      TIME NOT NULL,
    is_available  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (counsellor_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_availability_counsellor_day UNIQUE (counsellor_id, day_of_week)
);

CREATE INDEX idx_availability_schedules_counsellor ON availability_schedules(counsellor_id);

-- ── Availability Exceptions ───────────────────────────────────────────────────
CREATE TABLE availability_exceptions (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    counsellor_id  UUID NOT NULL,
    exception_date DATE NOT NULL,
    is_available   BOOLEAN NOT NULL DEFAULT FALSE,
    reason         VARCHAR(255),
    custom_slots   TEXT,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (counsellor_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_availability_exception UNIQUE (counsellor_id, exception_date)
);

CREATE INDEX idx_availability_exceptions_counsellor ON availability_exceptions(counsellor_id);

-- ── Mood Entries ──────────────────────────────────────────────────────────────
CREATE TABLE mood_entries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id      UUID NOT NULL,
    mood_rating     INTEGER NOT NULL,
    energy_level    INTEGER,
    sleep_quality   INTEGER,
    journal_text    TEXT,
    sentiment_score DECIMAL(4,3),
    triggers        TEXT,
    activities      TEXT,
    emotions        TEXT,
    is_private      BOOLEAN NOT NULL DEFAULT FALSE,
    recorded_at     TIMESTAMP NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT valid_mood_rating  CHECK (mood_rating  BETWEEN 1 AND 5),
    CONSTRAINT valid_energy_level CHECK (energy_level  IS NULL OR energy_level  BETWEEN 1 AND 5),
    CONSTRAINT valid_sleep_quality CHECK (sleep_quality IS NULL OR sleep_quality BETWEEN 1 AND 5),
    CONSTRAINT valid_sentiment    CHECK (sentiment_score IS NULL OR sentiment_score BETWEEN -1.0 AND 1.0)
);

CREATE INDEX idx_mood_entries_student_id  ON mood_entries(student_id);
CREATE INDEX idx_mood_entries_recorded_at ON mood_entries(recorded_at);
CREATE INDEX idx_mood_entries_mood_rating ON mood_entries(mood_rating);

-- ── Conversations ─────────────────────────────────────────────────────────────
CREATE TABLE conversations (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    participant1_id      UUID NOT NULL,
    participant2_id      UUID NOT NULL,
    last_message_at      TIMESTAMP,
    last_message_preview VARCHAR(255),
    unread_count1        INTEGER NOT NULL DEFAULT 0,
    unread_count2        INTEGER NOT NULL DEFAULT 0,
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (participant1_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (participant2_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT different_participants CHECK (participant1_id <> participant2_id)
);

CREATE INDEX idx_conversations_participant1 ON conversations(participant1_id);
CREATE INDEX idx_conversations_participant2 ON conversations(participant2_id);

-- ── Messages ──────────────────────────────────────────────────────────────────
CREATE TABLE messages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL,
    sender_id       UUID NOT NULL,
    receiver_id     UUID NOT NULL,
    content         TEXT NOT NULL,
    message_type    VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    sent_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at         TIMESTAMP,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id)       REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id)     REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_messages_sender_id       ON messages(sender_id);
CREATE INDEX idx_messages_sent_at         ON messages(sent_at);

-- ── Forum Posts ───────────────────────────────────────────────────────────────
CREATE TABLE forum_posts (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id         UUID NOT NULL,
    title             VARCHAR(255) NOT NULL,
    content           TEXT NOT NULL,
    category          VARCHAR(100),
    is_anonymous      BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted        BOOLEAN NOT NULL DEFAULT FALSE,
    moderation_status VARCHAR(50) DEFAULT 'APPROVED',
    like_count        INTEGER NOT NULL DEFAULT 0,
    comment_count     INTEGER NOT NULL DEFAULT 0,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_forum_posts_author_id  ON forum_posts(author_id);
CREATE INDEX idx_forum_posts_category   ON forum_posts(category);
CREATE INDEX idx_forum_posts_created_at ON forum_posts(created_at);

-- ── Forum Comments ────────────────────────────────────────────────────────────
CREATE TABLE forum_comments (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id      UUID NOT NULL,
    author_id    UUID NOT NULL,
    content      TEXT NOT NULL,
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id)   REFERENCES forum_posts(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_forum_comments_post_id   ON forum_comments(post_id);
CREATE INDEX idx_forum_comments_author_id ON forum_comments(author_id);

-- ── Resources ─────────────────────────────────────────────────────────────────
CREATE TABLE resources (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title         VARCHAR(255) NOT NULL,
    description   TEXT,
    category      VARCHAR(100),
    resource_type VARCHAR(50),
    content_url   VARCHAR(500),
    is_featured   BOOLEAN NOT NULL DEFAULT FALSE,
    view_count    INTEGER NOT NULL DEFAULT 0,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_resources_category   ON resources(category);
CREATE INDEX idx_resources_is_featured ON resources(is_featured);

-- ── Wellness Goals ────────────────────────────────────────────────────────────
CREATE TABLE wellness_goals (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id    UUID NOT NULL,
    title         VARCHAR(255) NOT NULL,
    description   TEXT,
    goal_type     VARCHAR(100),
    target_value  DECIMAL(10,2),
    current_value DECIMAL(10,2) DEFAULT 0,
    status        VARCHAR(50) DEFAULT 'ACTIVE',
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at  TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_wellness_goals_student_id ON wellness_goals(student_id);
CREATE INDEX idx_wellness_goals_status     ON wellness_goals(status);

-- ── Achievements ──────────────────────────────────────────────────────────────
CREATE TABLE achievements (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id    UUID NOT NULL,
    badge_name    VARCHAR(100) NOT NULL,
    description   TEXT,
    points_earned INTEGER DEFAULT 0,
    earned_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_achievements_student_id ON achievements(student_id);

-- ── Notifications ─────────────────────────────────────────────────────────────
-- status is VARCHAR — Hibernate @Enumerated(EnumType.STRING) works directly
CREATE TABLE notifications (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID NOT NULL,
    title                VARCHAR(255) NOT NULL,
    message              TEXT NOT NULL,
    notification_type    VARCHAR(100),
    channel              VARCHAR(20) DEFAULT 'IN_APP',
    status               VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    related_entity_id    UUID,
    related_entity_type  VARCHAR(100),
    scheduled_for        TIMESTAMP,
    retry_count          INTEGER DEFAULT 0,
    error_message        TEXT,
    sent_at              TIMESTAMP,
    read_at              TIMESTAMP,
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_id    ON notifications(user_id);
CREATE INDEX idx_notifications_status     ON notifications(status);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- ── Password Reset Tokens ─────────────────────────────────────────────────────
CREATE TABLE password_reset_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    is_used     BOOLEAN NOT NULL DEFAULT FALSE,
    used_at     TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_tokens_token   ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);

-- ── Audit Logs ────────────────────────────────────────────────────────────────
CREATE TABLE audit_logs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID,
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id   UUID,
    old_values  TEXT,
    new_values  TEXT,
    ip_address  VARCHAR(45),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_logs_user_id    ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

-- ── Crisis Events ─────────────────────────────────────────────────────────────
CREATE TABLE crisis_events (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id  UUID NOT NULL,
    severity    VARCHAR(20) DEFAULT 'MEDIUM',
    description TEXT,
    context     TEXT,
    status      VARCHAR(50) DEFAULT 'OPEN',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_crisis_events_student_id ON crisis_events(student_id);
CREATE INDEX idx_crisis_events_status     ON crisis_events(status);

-- ── MindBot Chat Sessions ─────────────────────────────────────────────────────
CREATE TABLE chat_sessions (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID NOT NULL,
    session_type      VARCHAR(20) NOT NULL DEFAULT 'CASUAL',
    message_count     INTEGER NOT NULL DEFAULT 0,
    report_generated  BOOLEAN NOT NULL DEFAULT FALSE,
    detected_severity VARCHAR(20),
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at          TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_chat_sessions_user_id    ON chat_sessions(user_id);
CREATE INDEX idx_chat_sessions_created_at ON chat_sessions(created_at);
CREATE INDEX idx_chat_sessions_active     ON chat_sessions(user_id, is_active);

-- ── MindBot Chat Messages ─────────────────────────────────────────────────────
CREATE TABLE chat_messages (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id    UUID NOT NULL,
    user_id       UUID NOT NULL,
    role          VARCHAR(10) NOT NULL,
    content       TEXT NOT NULL,
    severity_flag VARCHAR(20),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)    REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_chat_messages_session_id ON chat_messages(session_id);
CREATE INDEX idx_chat_messages_created_at ON chat_messages(created_at);

-- ── Mental Health Reports ─────────────────────────────────────────────────────
CREATE TABLE mental_health_reports (
    id                           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                      UUID NOT NULL,
    session_id                   UUID,
    phase_number                 INTEGER NOT NULL DEFAULT 0,
    title                        VARCHAR(200) NOT NULL,
    mental_state_level           VARCHAR(20) NOT NULL,
    wellness_score               INTEGER NOT NULL DEFAULT 50,
    condition_points             TEXT,
    recommended_exercises        TEXT,
    recommended_meditations      TEXT,
    conclusion                   TEXT,
    counsellor_referral_suggested BOOLEAN NOT NULL DEFAULT FALSE,
    report_json                  TEXT,
    created_at                   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)    REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE SET NULL,
    CONSTRAINT valid_wellness_score CHECK (wellness_score BETWEEN 0 AND 100)
);

CREATE INDEX idx_reports_user_id    ON mental_health_reports(user_id);
CREATE INDEX idx_reports_created_at ON mental_health_reports(created_at);

-- ── Chatbot Interactions (legacy table — kept for compatibility) ───────────────
CREATE TABLE chatbot_interactions (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id        UUID NOT NULL,
    session_id        UUID,
    user_message      TEXT NOT NULL,
    bot_response      TEXT,
    sentiment_score   DECIMAL(4,3),
    crisis_risk_score DECIMAL(4,3),
    detected_intent   VARCHAR(100),
    was_helpful       BOOLEAN,
    feedback_rating   INTEGER,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ── Seed resources ────────────────────────────────────────────────────────────
INSERT INTO resources (title, description, category, resource_type, content_url, is_featured)
VALUES
  ('Understanding Anxiety', 'A comprehensive guide to recognising and managing anxiety symptoms.', 'ANXIETY', 'ARTICLE', 'https://www.mind.org.uk/information-support/types-of-mental-health-problems/anxiety-and-panic-attacks/', TRUE),
  ('5-Minute Breathing Exercise', 'Box breathing technique to calm your nervous system in minutes.', 'MINDFULNESS', 'EXERCISE', 'https://www.healthline.com/health/box-breathing', TRUE),
  ('Sleep Hygiene Tips', 'Evidence-based strategies to improve your sleep quality.', 'SLEEP', 'GUIDE', 'https://www.sleepfoundation.org/sleep-hygiene', FALSE),
  ('Managing Academic Stress', 'Practical techniques for students dealing with exam pressure.', 'STRESS', 'ARTICLE', 'https://www.verywellmind.com/tips-to-reduce-stress-3145195', TRUE),
  ('Building Resilience', 'How to bounce back from setbacks and build emotional strength.', 'RESILIENCE', 'GUIDE', 'https://www.apa.org/topics/resilience', FALSE),
  ('Depression: Signs & Support', 'Recognising depression and knowing when to seek professional help.', 'DEPRESSION', 'ARTICLE', 'https://www.nhs.uk/mental-health/conditions/depression-in-adults/overview/', TRUE);
