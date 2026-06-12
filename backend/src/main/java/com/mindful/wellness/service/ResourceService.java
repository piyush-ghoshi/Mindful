package com.mindful.wellness.service;

import com.mindful.wellness.dto.ResourceDto;
import com.mindful.wellness.entity.Resource;
import com.mindful.wellness.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing mental-health resources.
 *
 * Resources are publicly accessible. Admins can create/update/delete resources.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ResourceService {

    private final ResourceRepository resourceRepository;

    /**
     * Get all resources, optionally filtered by category and/or featured flag.
     *
     * @param category optional category filter
     * @param featured optional featured filter
     * @return list of ResourceDto
     */
    @Transactional(readOnly = true)
    public List<ResourceDto> getResources(String category, Boolean featured) {
        List<Resource> resources;

        if (category != null && !category.isBlank()) {
            resources = resourceRepository.findByCategoryIgnoreCaseOrderByIsFeaturedDescCreatedAtDesc(category);
        } else if (Boolean.TRUE.equals(featured)) {
            resources = resourceRepository.findByIsFeaturedTrueOrderByCreatedAtDesc();
        } else {
            resources = resourceRepository.findAllByOrderByIsFeaturedDescCreatedAtDesc();
        }

        // Apply featured filter on top of category filter if both provided
        if (featured != null && category != null && !category.isBlank()) {
            boolean featuredVal = featured;
            resources = resources.stream()
                    .filter(r -> r.getIsFeatured() == featuredVal)
                    .collect(Collectors.toList());
        }

        return resources.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Get a single resource by ID and increment its view count.
     *
     * @param id the resource ID
     * @return ResourceDto
     */
    public ResourceDto getResource(UUID id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        resource.setViewCount(resource.getViewCount() + 1);
        resourceRepository.save(resource);

        return toDto(resource);
    }

    /**
     * Create a new resource (admin only).
     *
     * @param title       resource title
     * @param description resource description
     * @param category    category
     * @param type        resource type (ARTICLE, GUIDE, EXERCISE, VIDEO)
     * @param url         content URL
     * @param featured    whether to feature this resource
     * @return the created ResourceDto
     */
    public ResourceDto createResource(String title, String description, String category,
                                      String type, String url, boolean featured) {
        Resource resource = resourceRepository.save(Resource.builder()
                .title(title)
                .description(description)
                .category(category)
                .resourceType(type)
                .contentUrl(url)
                .isFeatured(featured)
                .viewCount(0)
                .build());

        log.info("Resource created: {} ({})", resource.getTitle(), resource.getId());
        return toDto(resource);
    }

    /**
     * Update an existing resource (admin only).
     *
     * @param id          the resource ID
     * @param title       new title (null = no change)
     * @param description new description
     * @param category    new category
     * @param type        new type
     * @param url         new URL
     * @param featured    new featured flag
     * @return updated ResourceDto
     */
    public ResourceDto updateResource(UUID id, String title, String description, String category,
                                      String type, String url, Boolean featured) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        if (title != null) resource.setTitle(title);
        if (description != null) resource.setDescription(description);
        if (category != null) resource.setCategory(category);
        if (type != null) resource.setResourceType(type);
        if (url != null) resource.setContentUrl(url);
        if (featured != null) resource.setIsFeatured(featured);

        return toDto(resourceRepository.save(resource));
    }

    /**
     * Delete a resource (admin only).
     *
     * @param id the resource ID
     */
    public void deleteResource(UUID id) {
        if (!resourceRepository.existsById(id)) {
            throw new IllegalArgumentException("Resource not found");
        }
        resourceRepository.deleteById(id);
        log.info("Resource deleted: {}", id);
    }

    // ── Converter ─────────────────────────────────────────────────────────────

    private ResourceDto toDto(Resource r) {
        return ResourceDto.builder()
                .id(r.getId())
                .title(r.getTitle())
                .description(r.getDescription())
                .category(r.getCategory())
                .type(r.getResourceType())
                .url(r.getContentUrl())
                .featured(r.getIsFeatured())
                .viewCount(r.getViewCount())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
