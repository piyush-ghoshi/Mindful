package com.mindful.wellness.repository;

import com.mindful.wellness.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> {

    List<Resource> findByCategoryIgnoreCaseOrderByIsFeaturedDescCreatedAtDesc(String category);

    List<Resource> findByIsFeaturedTrueOrderByCreatedAtDesc();

    List<Resource> findAllByOrderByIsFeaturedDescCreatedAtDesc();
}
