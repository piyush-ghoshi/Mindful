package com.mindful.wellness.repository;

import com.mindful.wellness.entity.ForumPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, UUID> {

    Page<ForumPost> findByIsDeletedFalseAndModerationStatusOrderByCreatedAtDesc(String moderationStatus, Pageable pageable);

    Page<ForumPost> findByCategoryAndIsDeletedFalseAndModerationStatusOrderByCreatedAtDesc(String category, String moderationStatus, Pageable pageable);

    Page<ForumPost> findByAuthorIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID authorId, Pageable pageable);
}
