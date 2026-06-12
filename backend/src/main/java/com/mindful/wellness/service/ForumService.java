package com.mindful.wellness.service;

import com.mindful.wellness.dto.ForumCommentDto;
import com.mindful.wellness.dto.ForumPostDto;
import com.mindful.wellness.entity.ForumComment;
import com.mindful.wellness.entity.ForumPost;
import com.mindful.wellness.entity.User;
import com.mindful.wellness.repository.ForumCommentRepository;
import com.mindful.wellness.repository.ForumPostRepository;
import com.mindful.wellness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for community forum operations.
 *
 * Handles post creation, listing, commenting, and moderation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ForumService {

    private final ForumPostRepository forumPostRepository;
    private final ForumCommentRepository forumCommentRepository;
    private final UserRepository userRepository;

    private static final String APPROVED = "APPROVED";

    /**
     * Create a new forum post.
     *
     * @param authorId  the author's user ID
     * @param title     post title
     * @param content   post body
     * @param category  optional category
     * @param anonymous whether to hide the author's identity
     * @return the created ForumPostDto
     */
    public ForumPostDto createPost(UUID authorId, String title, String content, String category, boolean anonymous) {
        userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ForumPost post = forumPostRepository.save(ForumPost.builder()
                .authorId(authorId)
                .title(title)
                .content(content)
                .category(category)
                .isAnonymous(anonymous)
                .moderationStatus(APPROVED) // auto-approve; add moderation queue if needed
                .likeCount(0)
                .commentCount(0)
                .isDeleted(false)
                .build());

        log.info("Forum post created by user {}: {}", authorId, post.getId());
        return toPostDto(post, false);
    }

    /**
     * Get paginated list of approved posts, optionally filtered by category.
     *
     * @param category optional category filter
     * @param pageable pagination
     * @return page of ForumPostDto (without comments for performance)
     */
    @Transactional(readOnly = true)
    public Page<ForumPostDto> getPosts(String category, Pageable pageable) {
        Page<ForumPost> page;
        if (category != null && !category.isBlank()) {
            page = forumPostRepository.findByCategoryAndIsDeletedFalseAndModerationStatusOrderByCreatedAtDesc(
                    category, APPROVED, pageable);
        } else {
            page = forumPostRepository.findByIsDeletedFalseAndModerationStatusOrderByCreatedAtDesc(APPROVED, pageable);
        }
        return page.map(p -> toPostDto(p, false));
    }

    /**
     * Get a single post with its comments.
     *
     * @param postId the post ID
     * @return ForumPostDto with comments
     */
    @Transactional(readOnly = true)
    public ForumPostDto getPost(UUID postId) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (post.getIsDeleted()) {
            throw new IllegalArgumentException("Post not found");
        }

        return toPostDto(post, true);
    }

    /**
     * Add a comment to a post.
     *
     * @param postId    the post ID
     * @param authorId  the commenter's user ID
     * @param content   comment text
     * @param anonymous whether to hide the commenter's identity
     * @return the created ForumCommentDto
     */
    public ForumCommentDto addComment(UUID postId, UUID authorId, String content, boolean anonymous) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (post.getIsDeleted()) {
            throw new IllegalArgumentException("Cannot comment on a deleted post");
        }

        ForumComment comment = forumCommentRepository.save(ForumComment.builder()
                .postId(postId)
                .authorId(authorId)
                .content(content)
                .isAnonymous(anonymous)
                .isDeleted(false)
                .build());

        // Update comment count
        post.setCommentCount(post.getCommentCount() + 1);
        forumPostRepository.save(post);

        log.info("Comment added to post {} by user {}", postId, authorId);
        return toCommentDto(comment);
    }

    /**
     * Soft-delete a post (only the author or an admin can delete).
     *
     * @param postId          the post ID
     * @param requestingUserId the user requesting deletion
     * @param isAdmin         whether the requesting user is an admin
     */
    public void deletePost(UUID postId, UUID requestingUserId, boolean isAdmin) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (!isAdmin && !post.getAuthorId().equals(requestingUserId)) {
            throw new IllegalArgumentException("You can only delete your own posts");
        }

        post.setIsDeleted(true);
        forumPostRepository.save(post);
        log.info("Post {} deleted by user {}", postId, requestingUserId);
    }

    /**
     * Soft-delete a comment (only the author or an admin can delete).
     *
     * @param commentId       the comment ID
     * @param requestingUserId the user requesting deletion
     * @param isAdmin         whether the requesting user is an admin
     */
    public void deleteComment(UUID commentId, UUID requestingUserId, boolean isAdmin) {
        ForumComment comment = forumCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (!isAdmin && !comment.getAuthorId().equals(requestingUserId)) {
            throw new IllegalArgumentException("You can only delete your own comments");
        }

        comment.setIsDeleted(true);
        forumCommentRepository.save(comment);

        // Decrement comment count on the post
        forumPostRepository.findById(comment.getPostId()).ifPresent(post -> {
            post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
            forumPostRepository.save(post);
        });

        log.info("Comment {} deleted by user {}", commentId, requestingUserId);
    }

    // ── Converters ────────────────────────────────────────────────────────────

    private ForumPostDto toPostDto(ForumPost post, boolean includeComments) {
        String authorName = null;
        if (!post.getIsAnonymous()) {
            User author = userRepository.findById(post.getAuthorId()).orElse(null);
            authorName = author != null ? author.getFullName() : null;
        }

        List<ForumCommentDto> comments = null;
        if (includeComments) {
            comments = forumCommentRepository
                    .findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(post.getId())
                    .stream()
                    .map(this::toCommentDto)
                    .collect(Collectors.toList());
        }

        return ForumPostDto.builder()
                .id(post.getId())
                .authorId(post.getIsAnonymous() ? null : post.getAuthorId())
                .authorName(authorName)
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .isAnonymous(post.getIsAnonymous())
                .moderationStatus(post.getModerationStatus())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .comments(comments)
                .build();
    }

    private ForumCommentDto toCommentDto(ForumComment comment) {
        String authorName = null;
        if (!comment.getIsAnonymous()) {
            User author = userRepository.findById(comment.getAuthorId()).orElse(null);
            authorName = author != null ? author.getFullName() : null;
        }

        return ForumCommentDto.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .authorId(comment.getIsAnonymous() ? null : comment.getAuthorId())
                .authorName(authorName)
                .content(comment.getContent())
                .isAnonymous(comment.getIsAnonymous())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
