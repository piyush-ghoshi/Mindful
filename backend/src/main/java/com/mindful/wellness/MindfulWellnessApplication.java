package com.mindful.wellness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for the Mindful Wellness Platform.
 * 
 * This application provides a comprehensive student mental health and wellness
 * support system with features including:
 * - User authentication and authorization
 * - Appointment scheduling and management
 * - Mood tracking and journaling
 * - Secure messaging between students and counselors
 * - AI-powered chatbot support
 * - Crisis intervention resources
 * - Peer support forums
 * - Resource hub with wellness materials
 * - Gamified wellness tracking
 * - Analytics and reporting
 * 
 * @author Mindful Wellness Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class MindfulWellnessApplication {

    public static void main(String[] args) {
        SpringApplication.run(MindfulWellnessApplication.class, args);
    }

}
