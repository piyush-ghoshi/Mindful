package com.mindful.wellness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumPostDto {
    private UUID id;
    private UUID authorId;
    private String authorName; // null when isAnonymous = true
    private String title;
    private String content;
    private String category;
    private boolean isAnonymous;
    private String moderationStatus;
    private int likeCount;
    private int commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ForumCommentDto> comments;
}
