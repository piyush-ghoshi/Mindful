-- Migrate sentiment_score from DECIMAL to DOUBLE PRECISION to match JPA Double type
ALTER TABLE mood_entries 
ALTER COLUMN sentiment_score TYPE DOUBLE PRECISION 
USING sentiment_score::DOUBLE PRECISION;
