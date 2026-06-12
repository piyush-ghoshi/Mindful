import { apiClient } from './api';

export type SeverityLevel = 'LOW' | 'MODERATE' | 'HIGH' | 'SEVERE';
export type SessionType   = 'ASSESSMENT' | 'CASUAL';

export interface ChatMessageDto {
  id?: string;
  sessionId?: string;
  role: 'USER' | 'BOT';
  content: string;
  severityFlag?: SeverityLevel | null;
  createdAt?: string;
}

export interface ChatSessionDto {
  id: string;
  sessionType: SessionType;
  messageCount: number;
  reportGenerated: boolean;
  detectedSeverity?: SeverityLevel | null;
  isActive: boolean;
  createdAt: string;
  messages: ChatMessageDto[];
  messagesUsedToday: number;
  messagesDailyLimit: number;
  reportsUsedToday: number;
  reportsDailyLimit: number;
}

export interface MentalHealthReportDto {
  id: string;
  userId: string;
  sessionId?: string;
  phaseNumber: number;
  title: string;
  mentalStateLevel: SeverityLevel;
  wellnessScore: number;
  conditionPoints: string[];
  recommendedExercises: string[];
  recommendedMeditations: string[];
  conclusion: string;
  counsellorReferralSuggested: boolean;
  createdAt: string;
  userName?: string;
  userEmail?: string;
}

export interface RateLimitInfo {
  messagesUsedToday: number;
  messagesDailyLimit: number;
  reportsUsedToday: number;
  reportsDailyLimit: number;
  canChat: boolean;
  canReport: boolean;
}

export const chatService = {
  startSession(type: SessionType = 'CASUAL'): Promise<ChatSessionDto> {
    return apiClient.post<ChatSessionDto>(`/chat/sessions?type=${type}`, {});
  },

  endActiveSession(): Promise<void> {
    return apiClient.post<void>('/chat/sessions/active/end', {});
  },

  getActiveSession(): Promise<ChatSessionDto | null> {
    return apiClient.get<ChatSessionDto | null>('/chat/sessions/active')
      .then(res => res || null)
      .catch(() => null);
  },

  sendMessage(sessionId: string, content: string, uploadedReportText?: string): Promise<ChatMessageDto> {
    return apiClient.post<ChatMessageDto>(`/chat/sessions/${sessionId}/messages`, {
      content,
      uploadedReportText: uploadedReportText ?? null,
    });
  },

  getMessages(sessionId: string): Promise<ChatMessageDto[]> {
    return apiClient.get<ChatMessageDto[]>(`/chat/sessions/${sessionId}/messages`);
  },

  generateReport(sessionId: string): Promise<MentalHealthReportDto> {
    return apiClient.post<MentalHealthReportDto>(`/chat/sessions/${sessionId}/report`, {});
  },

  getReports(): Promise<MentalHealthReportDto[]> {
    return apiClient.get<MentalHealthReportDto[]>('/chat/reports');
  },

  getReport(reportId: string): Promise<MentalHealthReportDto> {
    return apiClient.get<MentalHealthReportDto>(`/chat/reports/${reportId}`);
  },

  getRateLimitInfo(): Promise<RateLimitInfo> {
    return apiClient.get<RateLimitInfo>('/chat/rate-limit');
  },
};
