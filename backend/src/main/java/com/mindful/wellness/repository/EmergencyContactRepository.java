package com.mindful.wellness.repository;

import com.mindful.wellness.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, UUID> {
    
    List<EmergencyContact> findByUserIdOrderByPriorityOrderAsc(UUID userId);
    
    List<EmergencyContact> findByUserIdAndCanContactCrisisTrueOrderByPriorityOrderAsc(UUID userId);
}
