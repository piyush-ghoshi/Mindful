/**
 * User Types
 */
export const UserRole = {
  STUDENT: 'STUDENT',
  COUNSELLOR: 'COUNSELLOR',
  ADMIN: 'ADMIN',
} as const;

export type UserRole = (typeof UserRole)[keyof typeof UserRole];

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  profilePictureUrl?: string;
  isEmailVerified?: boolean;
  isActive?: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface StudentProfile extends User {
  studentId: string;
  institutionId: string;
  dateOfBirth?: string;
  gender?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  wellnessGoals?: string[];
}

export interface CounsellorProfile extends User {
  counsellorId: string;
  institutionId: string;
  licenseNumber: string;
  specializations: string[];
  yearsOfExperience: number;
  bio?: string;
  rating?: number;
  isAcceptingNewStudents: boolean;
}

/**
 * Authentication Types
 */
export interface LoginCredentials {
  email: string;
  password: string;
}

export interface RegisterCredentials {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role: UserRole;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  user: User;
}

export interface PasswordResetRequest {
  email: string;
}

export interface PasswordReset {
  resetToken: string;
  newPassword: string;
}

/**
 * Appointment Types
 */
export const AppointmentStatus = {
  SCHEDULED: 'SCHEDULED',
  CONFIRMED: 'CONFIRMED',
  IN_PROGRESS: 'IN_PROGRESS',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
  NO_SHOW: 'NO_SHOW',
  RESCHEDULED: 'RESCHEDULED',
} as const;

export type AppointmentStatus = (typeof AppointmentStatus)[keyof typeof AppointmentStatus];

export interface Appointment {
  id: string;
  studentId: string;
  counsellorId: string;
  scheduledStartTime: string;
  scheduledEndTime: string;
  status: AppointmentStatus;
  appointmentType: 'IN_PERSON' | 'VIDEO' | 'PHONE';
  reason?: string;
  studentNotes?: string;
  counsellorNotes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface TimeSlot {
  startTime: string;
  endTime: string;
}

export interface AvailabilitySchedule {
  monday: TimeSlot[];
  tuesday: TimeSlot[];
  wednesday: TimeSlot[];
  thursday: TimeSlot[];
  friday: TimeSlot[];
  saturday: TimeSlot[];
  sunday: TimeSlot[];
}

/**
 * Mood Tracking Types
 */
export interface MoodEntry {
  id: string;
  studentId: string;
  moodRating: number; // 1-5
  emotions: string[];
  energyLevel: number; // 1-5
  sleepQuality: number; // 1-5
  journalText?: string;
  sentimentScore: number; // -1.0 to 1.0
  triggers?: string[];
  activities?: string[];
  recordedAt: string;
  createdAt: string;
}

export interface MoodTrendAnalysis {
  studentId: string;
  period: string;
  averageMoodRating: number;
  moodVariability: number;
  mostCommonEmotions: string[];
  averageEnergyLevel: number;
  averageSleepQuality: number;
  trendDirection: 'IMPROVING' | 'DECLINING' | 'STABLE';
  concerningPatterns: string[];
  recommendations: string[];
}

/**
 * Messaging Types
 */
export interface Message {
  id: string;
  conversationId: string;
  senderId: string;
  receiverId: string;
  encryptedContent: string;
  messageType: 'TEXT' | 'IMAGE' | 'FILE' | 'VOICE';
  attachments?: Attachment[];
  sentAt: string;
  deliveredAt?: string;
  readAt?: string;
}

export interface Attachment {
  id: string;
  fileName: string;
  fileSize: number;
  fileType: string;
  storageUrl: string;
  uploadedAt: string;
}

export interface Conversation {
  id: string;
  participant1Id: string;
  participant2Id: string;
  lastMessageAt: string;
  lastMessagePreview: string;
  unreadCount: number;
  createdAt: string;
}

/**
 * Forum Types
 */
export interface ForumPost {
  id: string;
  authorId: string;
  authorName?: string;
  title: string;
  content: string;
  category: string;
  isAnonymous: boolean;
  upvotes: number;
  downvotes: number;
  commentCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface ForumComment {
  id: string;
  postId: string;
  authorId: string;
  authorName?: string;
  content: string;
  isAnonymous: boolean;
  upvotes: number;
  downvotes: number;
  createdAt: string;
  updatedAt: string;
}

/**
 * Resource Types
 */
export interface Resource {
  id: string;
  title: string;
  description: string;
  category: string;
  resourceType: 'ARTICLE' | 'VIDEO' | 'PDF' | 'LINK' | 'TOOL';
  contentUrl: string;
  imageUrl?: string;
  viewCount: number;
  rating?: number;
  isFeatured: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface OfflineResource {
  id: string;
  name: string;
  address: string;
  phoneNumber: string;
  servicesOffered: string[];
  hoursOfOperation: string;
  isVerified: boolean;
  isRecommended: boolean;
}

/**
 * Wellness Tracker Types
 */
export interface WellnessGoal {
  id: string;
  studentId: string;
  title: string;
  description: string;
  targetDate: string;
  progress: number; // 0-100
  status: 'ACTIVE' | 'COMPLETED' | 'ABANDONED';
  createdAt: string;
  updatedAt: string;
}

export interface Achievement {
  id: string;
  studentId: string;
  badgeId: string;
  badgeName: string;
  badgeDescription: string;
  badgeImageUrl: string;
  unlockedAt: string;
}

export interface WellnessScore {
  studentId: string;
  totalPoints: number;
  level: number;
  nextLevelPoints: number;
  achievements: Achievement[];
  recentActivities: string[];
}

/**
 * Notification Types
 */
export interface Notification {
  id: string;
  userId: string;
  title: string;
  message: string;
  type: 'APPOINTMENT' | 'MESSAGE' | 'ALERT' | 'REMINDER' | 'ACHIEVEMENT';
  isRead: boolean;
  createdAt: string;
  actionUrl?: string;
}

/**
 * API Response Types
 */
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

/**
 * Error Types
 */
export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, unknown>;
}
