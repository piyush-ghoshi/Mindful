package com.mindful.wellness.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resource entity representing a mental-health resource (article, guide, exercise, etc.).
 *
 * Resources are publicly accessible and can be filtered by category or featured flag.
 */
@Entity
@Table(name = "resources", indexes = {
        @Index(name = "idx_resources_category", columnList = "category"),
        @Index(name = "idx_resources_is_featured", columnList = "is_featured")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 100)
    private String category; // ANXIETY, MINDFULNESS, SLEEP, STRESS, RESILIENCE, DEPRESSION

    @Column(name = "resource_type", length = 50)
    private String resourceType; // ARTICLE, GUIDE, EXERCISE, VIDEO

    @Column(name = "content_url", length = 500)
    private String contentUrl;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
