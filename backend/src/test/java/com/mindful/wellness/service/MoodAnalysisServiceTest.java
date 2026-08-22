package com.mindful.wellness.service;

import com.mindful.wellness.dto.MoodAssessmentDto;
import com.mindful.wellness.entity.AnalysisMethod;
import com.mindful.wellness.entity.MoodType;
import com.mindful.wellness.repository.MoodAssessmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for MoodAnalysisService.
 * Tests both keyword-based and AI-based mood analysis.
 */
@SpringBootTest
@ActiveProfiles("test")
class MoodAnalysisServiceTest {
    
    @Autowired
    private MoodAnalysisService moodAnalysisService;
    
    @Autowired
    private MoodAssessmentRepository moodAssessmentRepository;
    
    @Test
    void testAnxiousMoodDetection() {
        // Arrange
        String message = "I'm feeling really anxious and overwhelmed today. Can't stop worrying.";
        UUID sessionId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        // Act
        MoodAssessmentDto result = moodAnalysisService.analyzeMood(message, sessionId, messageId, userId);
        
        // Assert
        assertNotNull(result);
        assertTrue(
            result.getDetectedMood() == MoodType.ANXIOUS || 
            result.getDetectedMood() == MoodType.OVERWHELMED
        );
        assertTrue(result.getSentimentScore().compareTo(BigDecimal.ZERO) < 0, "Sentiment should be negative");
        assertTrue(result.getMoodIntensity().compareTo(BigDecimal.valueOf(0.5)) > 0, "Intensity should be moderate-high");
        assertTrue(result.isConcerning(), "Should be flagged as concerning");
    }
    
    @Test
    void testHappyMoodDetection() {
        // Arrange
        String message = "I'm feeling great today! Things are going really well.";
        UUID sessionId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        // Act
        MoodAssessmentDto result = moodAnalysisService.analyzeMood(message, sessionId, messageId, userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(MoodType.HAPPY, result.getDetectedMood());
        assertTrue(result.getSentimentScore().compareTo(BigDecimal.ZERO) > 0, "Sentiment should be positive");
        assertFalse(result.isConcerning(), "Should not be concerning");
    }
    
    @Test
    void testSadMoodDetection() {
        // Arrange
        String message = "I've been feeling really sad and down lately. Nothing seems to help.";
        UUID sessionId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        // Act
        MoodAssessmentDto result = moodAnalysisService.analyzeMood(message, sessionId, messageId, userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(MoodType.SAD, result.getDetectedMood());
        assertTrue(result.getSentimentScore().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(result.isConcerning());
    }
    
    @Test
    void testHopelessMoodDetection() {
        // Arrange
        String message = "I feel hopeless. There's no point in trying anymore.";
        UUID sessionId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        // Act
        MoodAssessmentDto result = moodAnalysisService.analyzeMood(message, sessionId, messageId, userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(MoodType.HOPELESS, result.getDetectedMood());
        assertTrue(result.getSentimentScore().compareTo(BigDecimal.valueOf(-0.5)) < 0, "Very negative sentiment");
        assertTrue(result.isConcerning());
        assertTrue(result.isHighIntensity() || result.getMoodIntensity().compareTo(BigDecimal.valueOf(0.6)) > 0);
    }
    
    @Test
    void testNeutralMoodDetection() {
        // Arrange
        String message = "The weather is okay today. Just another day.";
        UUID sessionId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        // Act
        MoodAssessmentDto result = moodAnalysisService.analyzeMood(message, sessionId, messageId, userId);
        
        // Assert
        assertNotNull(result);
        // Should be neutral or calm
        assertTrue(
            result.getDetectedMood() == MoodType.NEUTRAL || 
            result.getDetectedMood() == MoodType.CALM
        );
        assertFalse(result.isConcerning());
    }
    
    @Test
    void testAIAnalysisMethodWhenAvailable() {
        // This test will use AI if Groq API key is configured
        // Otherwise will fallback to keyword-based
        String message = "I'm struggling with mixed emotions - happy about some things but worried about others.";
        UUID sessionId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        // Act
        MoodAssessmentDto result = moodAnalysisService.analyzeMood(message, sessionId, messageId, userId);
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getAnalysisMethod());
        assertTrue(
            result.getAnalysisMethod() == AnalysisMethod.AI || 
            result.getAnalysisMethod() == AnalysisMethod.KEYWORD
        );
        
        // AI should detect more nuanced mood
        if (result.getAnalysisMethod() == AnalysisMethod.AI) {
            System.out.println("✅ AI Analysis Used!");
            System.out.println("Detected Mood: " + result.getDetectedMood());
            System.out.println("Sentiment: " + result.getSentimentScore());
            System.out.println("Intensity: " + result.getMoodIntensity());
            System.out.println("Confidence: " + result.getConfidenceScore());
            System.out.println("Dominant Emotions: " + result.getDominantEmotions());
            assertTrue(result.getConfidenceScore().compareTo(BigDecimal.valueOf(0.6)) >= 0);
        } else {
            System.out.println("⚠️  AI unavailable, used keyword fallback");
        }
    }
    
    @Test
    void testMoodPersistence() {
        // Arrange
        String message = "I'm feeling anxious about my exams";
        UUID sessionId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        // Act
        MoodAssessmentDto result = moodAnalysisService.analyzeMood(message, sessionId, messageId, userId);
        
        // Assert - verify it was saved to database
        assertNotNull(result.getId());
        assertTrue(moodAssessmentRepository.existsById(result.getId()));
        
        // Verify we can retrieve it
        var retrieved = moodAssessmentRepository.findById(result.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(result.getDetectedMood(), retrieved.get().getDetectedMood());
    }
}
