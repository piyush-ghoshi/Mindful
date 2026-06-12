import React, { useState } from 'react';
import { moodService } from '../services/moodService';
import type { MoodEntry } from '../types';
import '../styles/MoodTracker.css';

interface MoodTrackerProps {
  onMoodSubmitted?: (entry: MoodEntry) => void;
  onError?: (error: string) => void;
}

const MOOD_CATEGORIES = ['happy', 'sad', 'anxious', 'stressed', 'calm', 'angry', 'neutral'];
const MOOD_EMOJIS: Record<string, string> = {
  happy: '😊',
  sad: '😢',
  anxious: '😰',
  stressed: '😫',
  calm: '😌',
  angry: '😠',
  neutral: '😐',
};

export const MoodTracker: React.FC<MoodTrackerProps> = ({ onMoodSubmitted, onError }) => {
  const [moodRating, setMoodRating] = useState<number>(5);
  const [selectedEmotions, setSelectedEmotions] = useState<string[]>([]);
  const [energyLevel, setEnergyLevel] = useState<number>(5);
  const [sleepQuality, setSleepQuality] = useState<number>(5);
  const [journalText, setJournalText] = useState<string>('');
  const [triggers, setTriggers] = useState<string>('');
  const [activities, setActivities] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleEmotionToggle = (emotion: string) => {
    setSelectedEmotions((prev) =>
      prev.includes(emotion) ? prev.filter((e) => e !== emotion) : [...prev, emotion]
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);
    setSuccess(false);

    try {
      if (selectedEmotions.length === 0) {
        throw new Error('Please select at least one emotion');
      }

      const moodData = {
        moodRating,
        emotions: selectedEmotions,
        energyLevel,
        sleepQuality,
        journalText: journalText.trim() || undefined,
        triggers: triggers
          .split(',')
          .map((t) => t.trim())
          .filter((t) => t),
        activities: activities
          .split(',')
          .map((a) => a.trim())
          .filter((a) => a),
        sentimentScore: 0, // Will be calculated by backend
        recordedAt: new Date().toISOString(),
        isPrivate: false,
      };

      const entry = await moodService.recordMoodEntry(moodData);

      setSuccess(true);
      setMoodRating(5);
      setSelectedEmotions([]);
      setEnergyLevel(5);
      setSleepQuality(5);
      setJournalText('');
      setTriggers('');
      setActivities('');

      onMoodSubmitted?.(entry);

      // Clear success message after 3 seconds
      setTimeout(() => setSuccess(false), 3000);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to record mood entry';
      setError(errorMessage);
      onError?.(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="mood-tracker">
      <div className="mood-tracker-header">
        <h2>Daily Mood Check-In</h2>
        <p>How are you feeling today?</p>
      </div>

      {error && <div className="mood-tracker-error">{error}</div>}
      {success && <div className="mood-tracker-success">Mood entry recorded successfully!</div>}

      <form onSubmit={handleSubmit} className="mood-tracker-form">
        {/* Mood Rating Slider */}
        <div className="form-group">
          <label htmlFor="mood-rating">Overall Mood (1-10)</label>
          <div className="mood-rating-container">
            <input
              id="mood-rating"
              type="range"
              min="1"
              max="10"
              value={moodRating}
              onChange={(e) => setMoodRating(Number(e.target.value))}
              className="mood-slider"
            />
            <div className="mood-rating-display">
              <span className="mood-value">{moodRating}</span>
              <span className="mood-label">
                {moodRating <= 3 ? 'Poor' : moodRating <= 5 ? 'Fair' : moodRating <= 7 ? 'Good' : 'Excellent'}
              </span>
            </div>
          </div>
        </div>

        {/* Emotion Categories */}
        <div className="form-group">
          <label>What emotions are you experiencing?</label>
          <div className="emotions-grid">
            {MOOD_CATEGORIES.map((emotion) => (
              <button
                key={emotion}
                type="button"
                className={`emotion-button ${selectedEmotions.includes(emotion) ? 'selected' : ''}`}
                onClick={() => handleEmotionToggle(emotion)}
              >
                <span className="emotion-emoji">{MOOD_EMOJIS[emotion]}</span>
                <span className="emotion-label">{emotion}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Energy Level */}
        <div className="form-group">
          <label htmlFor="energy-level">Energy Level (1-5)</label>
          <div className="level-container">
            <input
              id="energy-level"
              type="range"
              min="1"
              max="5"
              value={energyLevel}
              onChange={(e) => setEnergyLevel(Number(e.target.value))}
              className="level-slider"
            />
            <span className="level-value">{energyLevel}</span>
          </div>
        </div>

        {/* Sleep Quality */}
        <div className="form-group">
          <label htmlFor="sleep-quality">Sleep Quality (1-5)</label>
          <div className="level-container">
            <input
              id="sleep-quality"
              type="range"
              min="1"
              max="5"
              value={sleepQuality}
              onChange={(e) => setSleepQuality(Number(e.target.value))}
              className="level-slider"
            />
            <span className="level-value">{sleepQuality}</span>
          </div>
        </div>

        {/* Journal Notes */}
        <div className="form-group">
          <label htmlFor="journal-text">Journal Notes (Optional)</label>
          <textarea
            id="journal-text"
            value={journalText}
            onChange={(e) => setJournalText(e.target.value)}
            placeholder="Write about your day, thoughts, or feelings..."
            maxLength={500}
            rows={4}
            className="journal-textarea"
          />
          <div className="char-count">{journalText.length}/500</div>
        </div>

        {/* Triggers */}
        <div className="form-group">
          <label htmlFor="triggers">Triggers (Optional, comma-separated)</label>
          <input
            id="triggers"
            type="text"
            value={triggers}
            onChange={(e) => setTriggers(e.target.value)}
            placeholder="e.g., work stress, lack of sleep, social interaction"
            className="text-input"
          />
        </div>

        {/* Activities */}
        <div className="form-group">
          <label htmlFor="activities">Activities (Optional, comma-separated)</label>
          <input
            id="activities"
            type="text"
            value={activities}
            onChange={(e) => setActivities(e.target.value)}
            placeholder="e.g., exercise, meditation, socializing"
            className="text-input"
          />
        </div>

        {/* Submit Button */}
        <button type="submit" className="submit-button" disabled={isLoading}>
          {isLoading ? 'Recording...' : 'Record Mood'}
        </button>
      </form>
    </div>
  );
};
