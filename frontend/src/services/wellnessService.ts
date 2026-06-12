/**
 * Wellness service — stores goals in localStorage for now.
 * The backend has /api/wellness/* endpoints defined but not yet implemented.
 * When the backend is ready, swap localStorage calls for apiClient calls.
 */

export interface WellnessGoal {
  id: string;
  title: string;
  progress: number;
  status: 'ACTIVE' | 'COMPLETED';
  targetDate: string;
  createdAt: string;
}

const KEY = 'mindful_wellness_goals';

export const wellnessService = {
  getGoals(): WellnessGoal[] {
    try {
      return JSON.parse(localStorage.getItem(KEY) ?? '[]');
    } catch {
      return [];
    }
  },

  saveGoals(goals: WellnessGoal[]): void {
    localStorage.setItem(KEY, JSON.stringify(goals));
  },

  addGoal(title: string, targetDate: string): WellnessGoal {
    const goal: WellnessGoal = {
      id: crypto.randomUUID(),
      title,
      progress: 0,
      status: 'ACTIVE',
      targetDate,
      createdAt: new Date().toISOString(),
    };
    const goals = wellnessService.getGoals();
    goals.unshift(goal);
    wellnessService.saveGoals(goals);
    return goal;
  },

  updateGoal(id: string, updates: Partial<WellnessGoal>): void {
    const goals = wellnessService.getGoals().map(g => g.id === id ? { ...g, ...updates } : g);
    wellnessService.saveGoals(goals);
  },

  deleteGoal(id: string): void {
    wellnessService.saveGoals(wellnessService.getGoals().filter(g => g.id !== id));
  },
};
