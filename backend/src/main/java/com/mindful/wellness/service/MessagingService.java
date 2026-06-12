package com.mindful.wellness.service;

import com.mindful.wellness.dto.ConversationDto;
import com.mindful.wellness.dto.MessageDto;
import com.mindful.wellness.entity.Conversation;
import com.mindful.wellness.entity.Message;
import com.mindful.wellness.entity.User;
import com.mindful.wellness.repository.ConversationRepository;
import com.mindful.wellness.repository.MessageRepository;
import com.mindful.wellness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for private messaging between students and counsellors.
 *
 * Handles conversation creation, message sending, and read-status tracking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MessagingService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /**
     * Send a message from one user to another.
     * Creates a conversation if one does not already exist.
     *
     * @param senderId   the sender's user ID
     * @param receiverId the receiver's user ID
     * @param content    the message text
     * @param type       message type (TEXT, IMAGE, FILE)
     * @return the saved MessageDto
     */
    public MessageDto sendMessage(UUID senderId, UUID receiverId, String content, String type) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot send a message to yourself");
        }

        // Ensure both users exist
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

        // Get or create conversation
        Conversation conversation = conversationRepository.findBetween(senderId, receiverId)
                .orElseGet(() -> conversationRepository.save(
                        Conversation.builder()
                                .participant1Id(senderId)
                                .participant2Id(receiverId)
                                .unreadCount1(0)
                                .unreadCount2(0)
                                .build()
                ));

        // Save message
        Message message = messageRepository.save(Message.builder()
                .conversationId(conversation.getId())
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .messageType(type != null ? type : "TEXT")
                .sentAt(LocalDateTime.now())
                .isDeleted(false)
                .build());

        // Update conversation metadata
        conversation.setLastMessageAt(message.getSentAt());
        conversation.setLastMessagePreview(content.length() > 100 ? content.substring(0, 100) + "…" : content);

        // Increment unread count for the receiver
        if (receiverId.equals(conversation.getParticipant1Id())) {
            conversation.setUnreadCount1(conversation.getUnreadCount1() + 1);
        } else {
            conversation.setUnreadCount2(conversation.getUnreadCount2() + 1);
        }
        conversationRepository.save(conversation);

        log.info("Message sent from {} to {} in conversation {}", senderId, receiverId, conversation.getId());
        return toMessageDto(message, sender.getFullName());
    }

    /**
     * Get all conversations for a user, ordered by most recent message.
     *
     * @param userId the user's ID
     * @return list of ConversationDto
     */
    @Transactional(readOnly = true)
    public List<ConversationDto> getConversations(UUID userId) {
        return conversationRepository.findByParticipant(userId).stream()
                .map(c -> toConversationDto(c, userId))
                .collect(Collectors.toList());
    }

    /**
     * Get paginated messages for a conversation.
     * Also marks all messages as read for the requesting user.
     *
     * @param conversationId the conversation ID
     * @param requestingUserId the user requesting the messages
     * @param pageable pagination parameters
     * @return page of MessageDto
     */
    public Page<MessageDto> getMessages(UUID conversationId, UUID requestingUserId, Pageable pageable) {
        // Verify the user is a participant
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (!conversation.getParticipant1Id().equals(requestingUserId)
                && !conversation.getParticipant2Id().equals(requestingUserId)) {
            throw new IllegalArgumentException("Access denied: not a participant in this conversation");
        }

        // Mark messages as read
        int marked = messageRepository.markAllAsRead(conversationId, requestingUserId);
        if (marked > 0) {
            // Reset unread count for this user
            if (requestingUserId.equals(conversation.getParticipant1Id())) {
                conversation.setUnreadCount1(0);
            } else {
                conversation.setUnreadCount2(0);
            }
            conversationRepository.save(conversation);
        }

        return messageRepository
                .findByConversationIdAndIsDeletedFalseOrderBySentAtDesc(conversationId, pageable)
                .map(m -> {
                    User sender = userRepository.findById(m.getSenderId()).orElse(null);
                    return toMessageDto(m, sender != null ? sender.getFullName() : "Unknown");
                });
    }

    /**
     * Soft-delete a message (only the sender can delete their own message).
     *
     * @param messageId the message ID
     * @param requestingUserId the user requesting deletion
     */
    public void deleteMessage(UUID messageId, UUID requestingUserId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!message.getSenderId().equals(requestingUserId)) {
            throw new IllegalArgumentException("You can only delete your own messages");
        }

        message.setIsDeleted(true);
        messageRepository.save(message);
        log.info("Message {} soft-deleted by user {}", messageId, requestingUserId);
    }

    // ── Converters ────────────────────────────────────────────────────────────

    private MessageDto toMessageDto(Message m, String senderName) {
        return MessageDto.builder()
                .id(m.getId())
                .conversationId(m.getConversationId())
                .senderId(m.getSenderId())
                .senderName(senderName)
                .receiverId(m.getReceiverId())
                .content(m.getContent())
                .messageType(m.getMessageType())
                .sentAt(m.getSentAt())
                .readAt(m.getReadAt())
                .isRead(m.getReadAt() != null)
                .build();
    }

    private ConversationDto toConversationDto(Conversation c, UUID currentUserId) {
        UUID otherUserId = c.getParticipant1Id().equals(currentUserId)
                ? c.getParticipant2Id() : c.getParticipant1Id();

        User other = userRepository.findById(otherUserId).orElse(null);
        int unread = c.getParticipant1Id().equals(currentUserId)
                ? c.getUnreadCount1() : c.getUnreadCount2();

        return ConversationDto.builder()
                .id(c.getId())
                .otherUserId(otherUserId)
                .otherUserName(other != null ? other.getFullName() : "Unknown")
                .otherUserRole(other != null ? other.getRole().name() : null)
                .lastMessagePreview(c.getLastMessagePreview())
                .lastMessageAt(c.getLastMessageAt())
                .unreadCount(unread)
                .build();
    }
}
