package com.mindful.wellness.repository;

import com.mindful.wellness.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    /** Paginated messages for a conversation, newest first. */
    Page<Message> findByConversationIdAndIsDeletedFalseOrderBySentAtDesc(UUID conversationId, Pageable pageable);

    /** Count unread messages in a conversation for a specific receiver. */
    long countByConversationIdAndReceiverIdAndReadAtIsNull(UUID conversationId, UUID receiverId);

    /** Mark all messages in a conversation as read for a receiver. */
    @Modifying
    @Query("UPDATE Message m SET m.readAt = CURRENT_TIMESTAMP WHERE m.conversationId = :convId AND m.receiverId = :userId AND m.readAt IS NULL")
    int markAllAsRead(@Param("convId") UUID conversationId, @Param("userId") UUID userId);
}
