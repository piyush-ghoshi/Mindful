package com.mindful.wellness.controller;

import com.mindful.wellness.dto.ResourceDto;
import com.mindful.wellness.service.ResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Public resource library endpoint.
 *
 * GET  /api/resources            — list resources (public, optional ?category=&featured=)
 * GET  /api/resources/{id}       — get single resource and increment view count (public)
 * POST /api/resources            — create resource (ADMIN only)
 * PUT  /api/resources/{id}       — update resource (ADMIN only)
 * DELETE /api/resources/{id}     — delete resource (ADMIN only)
 */
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@Slf4j
public class ResourceController {

    private final ResourceService resourceService;

    /** GET /api/resources — list all resources with optional filters. */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getResources(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean featured) {

        List<ResourceDto> resources = resourceService.getResources(category, featured);
        return ResponseEntity.ok(Map.of(
                "content", resources,
                "totalElements", resources.size(),
                "categories", List.of("ANXIETY", "MINDFULNESS", "SLEEP", "STRESS", "RESILIENCE", "DEPRESSION")
        ));
    }

    /** GET /api/resources/{id} — single resource (increments view count). */
    @GetMapping("/{id}")
    public ResponseEntity<?> getResource(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(resourceService.getResource(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** POST /api/resources — create a resource (ADMIN only). */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createResource(@RequestBody Map<String, Object> body) {
        try {
            ResourceDto created = resourceService.createResource(
                    (String) body.get("title"),
                    (String) body.get("description"),
                    (String) body.get("category"),
                    (String) body.get("type"),
                    (String) body.get("url"),
                    Boolean.TRUE.equals(body.get("featured")));
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** PUT /api/resources/{id} — update a resource (ADMIN only). */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateResource(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        try {
            Boolean featured = body.containsKey("featured") ? Boolean.valueOf(body.get("featured").toString()) : null;
            ResourceDto updated = resourceService.updateResource(
                    id,
                    (String) body.get("title"),
                    (String) body.get("description"),
                    (String) body.get("category"),
                    (String) body.get("type"),
                    (String) body.get("url"),
                    featured);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** DELETE /api/resources/{id} — delete a resource (ADMIN only). */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteResource(@PathVariable UUID id) {
        try {
            resourceService.deleteResource(id);
            return ResponseEntity.ok(Map.of("message", "Resource deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
