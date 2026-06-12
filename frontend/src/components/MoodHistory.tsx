import React, { useState, useEffect } from 'react';
import { moodService } from '../services/moodService';
import type { MoodEntry } from '../types';
import '../styles/MoodHistory.css';

interface MoodHistoryProps {
  onError?: (error: string) => void;
}

const MOOD_EMOJIS: Record<string, string> = {
  happy: '😊',
  sad: '😢',
  anxious: '😰',
  stressed: '😫',
  calm: '😌',
  angry: '😠',
  neutral: '😐',
};

export const MoodHistory: React.FC<MoodHistoryProps> = ({ onError }) => {
  const [entries, setEntries] = useState<MoodEntry[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [expandedEntryId, setExpandedEntryId] = useState<string | null>(null);

  useEffect(() => {
    fetchMoodHistory();
  }, [page]);

  const fetchMoodHistory = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await moodService.getMoodHistory(30, page - 1, 10);
      setEntries(response.entries ?? []);
      setTotalPages(response.pagination?.totalPages ?? 1);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to fetch mood history';
      setError(errorMessage);
      onError?.(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const getMoodColor = (rating: number): string => {
    if (rating <= 3) return '#ef4444'; // red
    if (rating <= 5) return '#f97316'; // orange
    if (rating <= 7) return '#eab308'; // yellow
    return '#22c55e'; // green
  };

  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const toggleEntryExpand = (entryId: string) => {
    setExpandedEntryId(expandedEntryId === entryId ? null : entryId);
  };

  if (isLoading && entries.length === 0) {
    return <div className="mood-history-loading">Loading mood history...</div>;
  }

  return (
    <div className="mood-history">
      <div className="mood-history-header">
        <h2>Mood History</h2>
        <p>Track your emotional patterns over time</p>
      </div>

      {error && <div className="mood-history-error">{error}</div>}

      {entries.length === 0 ? (
        <div className="mood-history-empty">
          <p>No mood entries found for the selected date range.</p>
        </div>
      ) : (
        <div className="mood-timeline">
          {entries.map((entry) => (
            <div key={entry.id} className="mood-entry">
              <div
                className="mood-entry-header"
                onClick={() => toggleEntryExpand(entry.id)}
                style={{ cursor: 'pointer' }}
              >
                <div className="mood-entry-left">
                  <div
                    className="mood-indicator"
                    style={{ backgroundColor: getMoodColor(entry.moodRating) }}
                  >
                    <span className="mood-rating">{entry.moodRating}</span>
                  </div>
                  <div className="mood-entry-info">
                    <div className="mood-date">{formatDate(entry.recordedAt)}</div>
                    <div className="mood-emotions">
                      {entry.emotions.slice(0, 3).map((emotion) => (
                        <span key={emotion} className="emotion-tag">
                          {MOOD_EMOJIS[emotion] || '😐'} {emotion}
                        </span>
                      ))}
                      {entry.emotions.length > 3 && (
                        <span className="emotion-tag">+{entry.emotions.length - 3} more</span>
                      )}
                    </div>
                  </div>
                </div>
                <div className="mood-entry-right">
                  <span className="expand-icon">{expandedEntryId === entry.id ? '▼' : '▶'}</span>
                </div>
              </div>

              {expandedEntryId === entry.id && (
                <div className="mood-entry-details">
                  <div className="detail-row">
                    <span className="detail-label">Energy Level:</span>
                    <span className="detail-value">{entry.energyLevel}/5</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">Sleep Quality:</span>
                    <span className="detail-value">{entry.sleepQuality}/5</span>
                  </div>
                  {entry.journalText && (
                    <div className="detail-row">
                      <span className="detail-label">Notes:</span>
                      <p className="detail-text">{entry.journalText}</p>
                    </div>
                  )}
                  {entry.triggers && entry.triggers.length > 0 && (
                    <div className="detail-row">
                      <span className="detail-label">Triggers:</span>
                      <div className="detail-tags">
                        {entry.triggers.map((trigger) => (
                          <span key={trigger} className="tag">
                            {trigger}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}
                  {entry.activities && entry.activities.length > 0 && (
                    <div className="detail-row">
                      <span className="detail-label">Activities:</span>
                      <div className="detail-tags">
                        {entry.activities.map((activity) => (
                          <span key={activity} className="tag">
                            {activity}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="pagination">
          <button
            onClick={() => setPage(Math.max(1, page - 1))}
            disabled={page === 1}
            className="pagination-button"
          >
            Previous
          </button>
          <span className="pagination-info">
            Page {page} of {totalPages}
          </span>
          <button
            onClick={() => setPage(Math.min(totalPages, page + 1))}
            disabled={page === totalPages}
            className="pagination-button"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
};
