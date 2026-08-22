package com.mindful.wellness.repository;

import com.mindful.wellness.entity.CrisisHotline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CrisisHotlineRepository extends JpaRepository<CrisisHotline, UUID> {
    
    List<CrisisHotline> findByCountryCodeAndIsActiveTrueOrderByDisplayOrderAsc(String countryCode);
    
    List<CrisisHotline> findByIsActiveTrueOrderByDisplayOrderAsc();
}
