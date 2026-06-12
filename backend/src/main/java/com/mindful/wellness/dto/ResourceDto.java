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
public class ResourceDto {
    private UUID id;
    private String title;
    private String description;
    private String category;
    private String type;
    private String url;
    private boolean featured;
    private int viewCount;
    private LocalDateTime createdAt;
}
