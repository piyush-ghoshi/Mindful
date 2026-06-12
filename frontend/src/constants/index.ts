/**
 * API Endpoints
 */
export const API_ENDPOINTS = {
  // Authentication
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh',
    ME: '/auth/me',
    PROFILE: '/auth/profile',
    PASSWORD_RESET_REQUEST: '/auth/password-reset/request',
    PASSWORD_RESET_CONFIRM: '/auth/password-reset/confirm',
  },

  // Users
  USERS: {
    GET_PROFILE: (userId: string) => `/users/${userId}`,
    UPDATE_PROFILE: (userId: string) => `/users/${userId}`,
    GET_COUNSELLORS: '/users/counsellors',
    GET_COUNSELLOR: (counsellorId: string) => `/users/counsellors/${counsellorId}`,
  },

  // Appointments
  APPOINTMENTS: {
    LIST: '/appointments',
    CREATE: '/appointments',
    GET: (appointmentId: string) => `/appointments/${appointmentId}`,
    UPDATE: (appointmentId: string) => `/appointments/${appointmentId}`,
    CANCEL: (appointmentId: string) => `/appointments/${appointmentId}/cancel`,
    RESCHEDULE: (appointmentId: string) => `/appointments/${appointmentId}/reschedule`,
    GET_AVAILABLE_SLOTS: (counsellorId: string) =>
      `/appointments/counsellors/${counsellorId}/available-slots`,
  },

  // Mood Tracking
  MOOD: {
    CREATE_ENTRY: '/mood/entries',
    GET_HISTORY: '/mood/history',
    GET_TRENDS: '/mood/trends',
    GET_INSIGHTS: '/mood/insights',
  },

  // Messaging
  MESSAGES: {
    SEND: '/messages',
    GET_CONVERSATIONS: '/messages/conversations',
    GET_CONVERSATION: (conversationId: string) => `/messages/conversations/${conversationId}`,
    MARK_AS_READ: (messageId: string) => `/messages/${messageId}/read`,
    UPLOAD_ATTACHMENT: '/messages/attachments/upload',
  },

  // Forum
  FORUM: {
    GET_POSTS: '/forum/posts',
    CREATE_POST: '/forum/posts',
    GET_POST: (postId: string) => `/forum/posts/${postId}`,
    UPDATE_POST: (postId: string) => `/forum/posts/${postId}`,
    DELETE_POST: (postId: string) => `/forum/posts/${postId}`,
    CREATE_COMMENT: (postId: string) => `/forum/posts/${postId}/comments`,
    GET_COMMENTS: (postId: string) => `/forum/posts/${postId}/comments`,
    VOTE_POST: (postId: string) => `/forum/posts/${postId}/vote`,
  },

  // Resources
  RESOURCES: {
    LIST: '/resources',
    GET: (resourceId: string) => `/resources/${resourceId}`,
    SEARCH: '/resources/search',
    BOOKMARK: (resourceId: string) => `/resources/${resourceId}/bookmark`,
    GET_BOOKMARKS: '/resources/bookmarks',
  },

  // Wellness Tracker
  WELLNESS: {
    GET_GOALS: '/wellness/goals',
    CREATE_GOAL: '/wellness/goals',
    UPDATE_GOAL: (goalId: string) => `/wellness/goals/${goalId}`,
    COMPLETE_GOAL: (goalId: string) => `/wellness/goals/${goalId}/complete`,
    GET_ACHIEVEMENTS: '/wellness/achievements',
    GET_SCORE: '/wellness/score',
    GET_LEADERBOARD: '/wellness/leaderboard',
  },

  // Crisis Support
  CRISIS: {
    GET_RESOURCES: '/crisis/resources',
    TRIGGER_PROTOCOL: '/crisis/trigger',
    GET_HOTLINES: '/crisis/hotlines',
  },

  // Notifications
  NOTIFICATIONS: {
    LIST: '/notifications',
    MARK_AS_READ: (notificationId: string) => `/notifications/${notificationId}/read`,
    UPDATE_PREFERENCES: '/notifications/preferences',
  },

  // Analytics (Admin)
  ANALYTICS: {
    DASHBOARD: '/analytics/dashboard',
    REPORTS: '/analytics/reports',
    EXPORT_REPORT: (reportId: string) => `/analytics/reports/${reportId}/export`,
  },
};

/**
 * Mood Categories
 */
export const MOOD_CATEGORIES = ['happy', 'sad', 'anxious', 'stressed', 'calm', 'angry'];

/**
 * Energy Levels
 */
export const ENERGY_LEVELS = [
  { value: 1, label: 'Very Low' },
  { value: 2, label: 'Low' },
  { value: 3, label: 'Moderate' },
  { value: 4, label: 'High' },
  { value: 5, label: 'Very High' },
];

/**
 * Sleep Quality Levels
 */
export const SLEEP_QUALITY_LEVELS = [
  { value: 1, label: 'Very Poor' },
  { value: 2, label: 'Poor' },
  { value: 3, label: 'Fair' },
  { value: 4, label: 'Good' },
  { value: 5, label: 'Excellent' },
];

/**
 * Forum Categories
 */
export const FORUM_CATEGORIES = [
  { id: 'anxiety', label: 'Anxiety' },
  { id: 'depression', label: 'Depression' },
  { id: 'stress', label: 'Stress' },
  { id: 'relationships', label: 'Relationships' },
  { id: 'academic', label: 'Academic' },
  { id: 'general', label: 'General Wellness' },
];

/**
 * Resource Categories
 */
export const RESOURCE_CATEGORIES = [
  { id: 'stress-management', label: 'Stress Management' },
  { id: 'mindfulness', label: 'Mindfulness' },
  { id: 'sleep-hygiene', label: 'Sleep Hygiene' },
  { id: 'academic-wellness', label: 'Academic Wellness' },
  { id: 'relationships', label: 'Relationships' },
  { id: 'nutrition', label: 'Nutrition' },
  { id: 'exercise', label: 'Exercise' },
];

/**
 * Appointment Types
 */
export const APPOINTMENT_TYPES = [
  { id: 'IN_PERSON', label: 'In Person' },
  { id: 'VIDEO', label: 'Video Call' },
  { id: 'PHONE', label: 'Phone Call' },
];

/**
 * Session Configuration
 */
export const SESSION_CONFIG = {
  TIMEOUT_MINUTES: 30,
  WARNING_MINUTES: 5,
};

/**
 * Pagination
 */
export const PAGINATION = {
  DEFAULT_PAGE_SIZE: 10,
  MAX_PAGE_SIZE: 100,
};

/**
 * Error Messages
 */
export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Network error. Please check your connection.',
  UNAUTHORIZED: 'You are not authorized to perform this action.',
  FORBIDDEN: 'Access denied.',
  NOT_FOUND: 'Resource not found.',
  VALIDATION_ERROR: 'Please check your input and try again.',
  SERVER_ERROR: 'Server error. Please try again later.',
  TIMEOUT: 'Request timed out. Please try again.',
};

/**
 * Success Messages
 */
export const SUCCESS_MESSAGES = {
  LOGIN_SUCCESS: 'Logged in successfully.',
  LOGOUT_SUCCESS: 'Logged out successfully.',
  REGISTRATION_SUCCESS: 'Registration successful. Please log in.',
  PROFILE_UPDATED: 'Profile updated successfully.',
  APPOINTMENT_BOOKED: 'Appointment booked successfully.',
  APPOINTMENT_CANCELLED: 'Appointment cancelled successfully.',
  MOOD_ENTRY_SAVED: 'Mood entry saved successfully.',
  MESSAGE_SENT: 'Message sent successfully.',
  POST_CREATED: 'Post created successfully.',
};
