package com.mindful.wellness.repository;

import com.mindful.wellness.entity.ForumComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ForumCommentRepository extends JpaRepository<ForumComment, UUID> {

    List<ForumComment> findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(UUID postId);

    long countByPostIdAndIsDeletedFalse(UUID postId);
}
