import { apiClient } from './api';

export interface ConversationDto {
  id: string;
  otherUserId: string;
  otherUserName: string;
  otherUserRole: string;
  lastMessagePreview: string | null;
  lastMessageAt: string | null;
  unreadCount: number;
}

export interface MessageDto {
  id: string;
  conversationId: string;
  senderId: string;
  senderName: string;
  receiverId: string;
  content: string;
  messageType: string;
  sentAt: string;
  readAt: string | null;
  isRead: boolean;
}

export interface MessagesPage {
  content: MessageDto[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
}

export const messagingService = {
  /** Get all conversations for the current user. */
  getConversations(): Promise<ConversationDto[]> {
    return apiClient.get<ConversationDto[]>('/messages/conversations');
  },

  /** Get paginated messages in a conversation (marks them as read). */
  getMessages(conversationId: string, page = 0, size = 20): Promise<MessagesPage> {
    return apiClient.get<MessagesPage>(
      `/messages/conversations/${conversationId}?page=${page}&size=${size}`
    );
  },

  /** Send a message to a user (creates conversation if needed). */
  sendMessage(receiverId: string, content: string, messageType = 'TEXT'): Promise<MessageDto> {
    return apiClient.post<MessageDto>('/messages', { receiverId, content, messageType });
  },

  /** Soft-delete a message. */
  deleteMessage(messageId: string): Promise<void> {
    return apiClient.delete<void>(`/messages/${messageId}`);
  },
};
