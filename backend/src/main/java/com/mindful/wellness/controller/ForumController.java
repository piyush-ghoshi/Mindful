package com.mindful.wellness.controller;

import com.mindful.wellness.dto.CreateForumPostRequest;
import com.mindful.wellness.dto.ForumCommentDto;
import com.mindful.wellness.dto.ForumPostDto;
import com.mindful.wellness.entity.UserRole;
import com.mindful.wellness.service.ForumService;
import com.mindful.wellness.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for the community forum.
 *
 * Endpoints:
 *   GET    /api/forum/posts              — list approved posts (public)
 *   POST   /api/forum/posts              — create a post (authenticated)
 *   GET    /api/forum/posts/{id}         — get post with comments (public)
 *   DELETE /api/forum/posts/{id}         — delete post (author or admin)
 *   POST   /api/forum/posts/{id}/comments — add a comment (authenticated)
 *   DELETE /api/forum/comments/{id}      — delete comment (author or admin)
 */
@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
@Slf4j
public class ForumController {

    private final ForumService forumService;
    private final AuthUtil authUtil;

    /** List approved posts, optionally filtered by category. */
    @GetMapping("/posts")
    public ResponseEntity<?> getPosts(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<ForumPostDto> posts = forumService.getPosts(category, pageable);
        return ResponseEntity.ok(Map.of(
                "content", posts.getContent(),
                "totalElements", posts.getTotalElements(),
                "totalPages", posts.getTotalPages(),
                "currentPage", posts.getNumber()
        ));
    }

    /** Get a single post with its comments. */
    @GetMapping("/posts/{id}")
    public ResponseEntity<?> getPost(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(forumService.getPost(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Create a new forum post. */
    @PostMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPost(@Valid @RequestBody CreateForumPostRequest request) {
        try {
            UUID authorId = authUtil.getCurrentUserId();
            ForumPostDto post = forumService.createPost(
                    authorId, request.getTitle(), request.getContent(),
                    request.getCategory(), request.isAnonymous());
            return ResponseEntity.status(HttpStatus.CREATED).body(post);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** Delete a post (author or admin). */
    @DeleteMapping("/posts/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deletePost(@PathVariable UUID id) {
        try {
            UUID userId = authUtil.getCurrentUserId();
            boolean isAdmin = authUtil.getCurrentUserRole() == UserRole.ADMIN;
            forumService.deletePost(id, userId, isAdmin);
            return ResponseEntity.ok(Map.of("message", "Post deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** Add a comment to a post. */
    @PostMapping("/posts/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addComment(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        try {
            String content = (String) body.get("content");
            boolean anonymous = Boolean.TRUE.equals(body.get("anonymous"));

            if (content == null || content.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Content is required"));
            }

            UUID authorId = authUtil.getCurrentUserId();
            ForumCommentDto comment = forumService.addComment(id, authorId, content, anonymous);
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** Delete a comment (author or admin). */
    @DeleteMapping("/comments/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteComment(@PathVariable UUID id) {
        try {
            UUID userId = authUtil.getCurrentUserId();
            boolean isAdmin = authUtil.getCurrentUserRole() == UserRole.ADMIN;
            forumService.deleteComment(id, userId, isAdmin);
            return ResponseEntity.ok(Map.of("message", "Comment deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
