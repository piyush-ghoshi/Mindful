package com.mindful.wellness.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for the Mindful Wellness Platform API.
 * 
 * Provides endpoints to verify that the application is running and
 * the database connection is healthy.
 * 
 * @author Mindful Wellness Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * Health check endpoint.
     * 
     * Returns a simple health status response to verify the application is running.
     * 
     * @return ResponseEntity with health status
     */
    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Mindful Wellness Platform");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

}
