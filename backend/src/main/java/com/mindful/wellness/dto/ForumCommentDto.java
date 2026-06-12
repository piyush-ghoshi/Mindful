package com.mindful.wellness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumCommentDto {
    private UUID id;
    private UUID postId;
    private UUID authorId;
    private String authorName; // null when isAnonymous = true
    private String content;
    private boolean isAnonymous;
    private LocalDateTime createdAt;
}
