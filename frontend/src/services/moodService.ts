import { apiClient } from './api';
import type { MoodEntry } from '../types';

export interface MoodHistoryResponse {
  entries: MoodEntry[];
  averageMoodRating: number | null;
  trendDirection: string;
  totalEntries: number;
  pagination: {
    currentPage: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
}

export interface MoodStatsResponse {
  averageMoodRating: number | null;
  averageEnergyLevel: number | null;
  averageSleepQuality: number | null;
  trendDirection: string;
  totalEntries: number;
  period: string;
  moodDistribution: Record<number, number>;
  recommendations: string[];
  concerningPatterns: string[];
}

export const moodService = {
  /**
   * Record a new mood entry.
   * Backend accepts moodRating 1-5, emotions as JSON array.
   */
  async recordMoodEntry(
    moodData: Omit<MoodEntry, 'id' | 'createdAt' | 'studentId' | 'sentimentScore'>
  ): Promise<MoodEntry> {
    // Send arrays directly to match MoodEntryDto
    const payload = {
      moodRating: moodData.moodRating,
      energyLevel: moodData.energyLevel,
      sleepQuality: moodData.sleepQuality,
      emotions: moodData.emotions ?? [],
      triggers: moodData.triggers ?? [],
      activities: moodData.activities ?? [],
      journalText: moodData.journalText ?? null,
      isPrivate: false,
      recordedAt: moodData.recordedAt,
    };
    // Backend returns MoodEntryDto directly (no wrapper)
    return apiClient.post<MoodEntry>('/mood/entries', payload);
  },

  /**
   * Get mood history — backend accepts ?days=30&page=0&size=10
   */
  async getMoodHistory(days = 30, page = 0, size = 20): Promise<MoodHistoryResponse> {
    return apiClient.get<MoodHistoryResponse>(
      `/mood/history?days=${days}&page=${page}&size=${size}`
    );
  },

  /**
   * Get mood stats
   */
  async getMoodStats(days = 30): Promise<MoodStatsResponse> {
    return apiClient.get<MoodStatsResponse>(`/mood/stats?days=${days}`);
  },
};
