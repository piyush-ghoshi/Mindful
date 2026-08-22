package com.mindful.wellness.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a user's emergency contact.
 */
@Entity
@Table(name = "emergency_contacts", indexes = {
    @Index(name = "idx_emergency_user", columnList = "user_id"),
    @Index(name = "idx_emergency_priority", columnList = "user_id, priority_order"),
    @Index(name = "idx_emergency_crisis_flag", columnList = "user_id, can_contact_crisis")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyContact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "contact_name", nullable = false, length = 200)
    private String contactName;
    
    @Column(name = "relationship", length = 100)
    private String relationship;
    
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;
    
    @Column(name = "email", length = 255)
    private String email;
    
    @Column(name = "priority_order")
    @Builder.Default
    private Integer priorityOrder = 1;
    
    @Column(name = "can_contact_crisis")
    @Builder.Default
    private Boolean canContactCrisis = true;
    
    @Column(name = "preferred_method", length = 20)
    @Builder.Default
    private String preferredMethod = "PHONE";
    
    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
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
