package com.mindful.wellness.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a crisis hotline/helpline.
 */
@Entity
@Table(name = "crisis_hotlines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrisisHotline {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "country_code", length = 5)
    @Builder.Default
    private String countryCode = "IN";
    
    @Column(name = "hotline_name", nullable = false, length = 200)
    private String hotlineName;
    
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "is_24_7")
    @Builder.Default
    private Boolean is247 = true;
    
    @Column(name = "operating_hours", length = 200)
    private String operatingHours;
    
    @Column(name = "languages", length = 200)
    private String languages;
    
    @Column(name = "specialty", length = 100)
    private String specialty;
    
    @Column(name = "has_phone")
    @Builder.Default
    private Boolean hasPhone = true;
    
    @Column(name = "has_sms")
    @Builder.Default
    private Boolean hasSms = false;
    
    @Column(name = "has_chat")
    @Builder.Default
    private Boolean hasChat = false;
    
    @Column(name = "has_email")
    @Builder.Default
    private Boolean hasEmail = false;
    
    @Column(name = "website_url", length = 500)
    private String websiteUrl;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
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
