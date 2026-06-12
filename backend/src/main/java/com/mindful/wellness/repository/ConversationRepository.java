package com.mindful.wellness.repository;

import com.mindful.wellness.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    /** Find all conversations for a user (as either participant). */
    @Query("SELECT c FROM Conversation c WHERE c.participant1Id = :userId OR c.participant2Id = :userId ORDER BY c.lastMessageAt DESC NULLS LAST")
    List<Conversation> findByParticipant(@Param("userId") UUID userId);

    /** Find the conversation between two specific users (order-independent). */
    @Query("SELECT c FROM Conversation c WHERE (c.participant1Id = :a AND c.participant2Id = :b) OR (c.participant1Id = :b AND c.participant2Id = :a)")
    Optional<Conversation> findBetween(@Param("a") UUID a, @Param("b") UUID b);
}
