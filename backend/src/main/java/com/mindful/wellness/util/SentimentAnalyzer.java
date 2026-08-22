package com.mindful.wellness.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for rule-based sentiment analysis.
 * Provides keyword-based sentiment scoring and intensity calculation.
 */
public class SentimentAnalyzer {
    
    // Positive words (contributes to positive sentiment)
    private static final Set<String> POSITIVE_WORDS = Set.of(
        "happy", "joy", "joyful", "excited", "great", "wonderful", "amazing",
        "excellent", "fantastic", "love", "loved", "loving", "good", "better",
        "best", "improved", "improving", "hopeful", "hope", "optimistic",
        "peaceful", "calm", "relaxed", "comfortable", "grateful", "thankful",
        "blessed", "fortunate", "lucky", "content", "satisfied", "pleased",
        "proud", "accomplished", "successful", "motivated", "inspired"
    );
    
    // Negative words (contributes to negative sentiment)
    private static final Set<String> NEGATIVE_WORDS = Set.of(
        "sad", "sadness", "depressed", "depression", "unhappy", "miserable",
        "terrible", "awful", "horrible", "bad", "worse", "worst", "hate",
        "hated", "angry", "mad", "furious", "upset", "annoyed", "irritated",
        "anxious", "anxiety", "worried", "worry", "nervous", "scared", "fear",
        "fearful", "panic", "panicking", "stressed", "stress", "overwhelmed",
        "helpless", "hopeless", "worthless", "useless", "failure", "failed",
        "lonely", "alone", "isolated", "abandoned", "rejected", "broken",
        "hurt", "pain", "painful", "suffering", "struggling", "difficult",
        "hard", "tough", "unbearable", "can't", "cannot", "impossible",
        "never", "nobody", "nothing", "no one"
    );
    
    // Strong positive words (double weight)
    private static final Set<String> STRONG_POSITIVE = Set.of(
        "amazing", "fantastic", "wonderful", "excellent", "perfect",
        "incredible", "outstanding", "brilliant", "thrilled", "ecstatic"
    );
    
    // Strong negative words (double weight)
    private static final Set<String> STRONG_NEGATIVE = Set.of(
        "suicidal", "suicide", "kill myself", "end my life", "want to die",
        "hopeless", "worthless", "unbearable", "can't go on", "give up",
        "no point", "no hope", "devastated", "destroyed", "shattered"
    );
    
    // Intensity modifiers
    private static final Set<String> INTENSIFIERS = Set.of(
        "very", "extremely", "really", "so", "too", "incredibly", "absolutely",
        "completely", "totally", "utterly", "entirely", "deeply", "highly"
    );
    
    // Negation words (reverse sentiment)
    private static final Set<String> NEGATIONS = Set.of(
        "not", "no", "never", "neither", "nor", "none", "nobody",
        "nothing", "nowhere", "hardly", "scarcely", "barely"
    );
    
    /**
     * Calculate sentiment score from text.
     * @param text Input text to analyze
     * @return Sentiment score between -1.0 and 1.0
     */
    public static BigDecimal calculateSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        String lower = text.toLowerCase();
        List<String> words = tokenize(lower);
        
        if (words.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        double score = 0.0;
        int scoredWords = 0;
        
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            
            // Check for negation in previous 3 words
            boolean negated = false;
            for (int j = Math.max(0, i - 3); j < i; j++) {
                if (NEGATIONS.contains(words.get(j))) {
                    negated = true;
                    break;
                }
            }
            
            // Check for intensifier in previous 2 words
            boolean intensified = false;
            for (int j = Math.max(0, i - 2); j < i; j++) {
                if (INTENSIFIERS.contains(words.get(j))) {
                    intensified = true;
                    break;
                }
            }
            
            double wordScore = 0.0;
            
            // Calculate word sentiment
            if (STRONG_POSITIVE.contains(word)) {
                wordScore = 2.0;
            } else if (POSITIVE_WORDS.contains(word)) {
                wordScore = 1.0;
            } else if (STRONG_NEGATIVE.contains(word)) {
                wordScore = -2.0;
            } else if (NEGATIVE_WORDS.contains(word)) {
                wordScore = -1.0;
            }
            
            if (wordScore != 0.0) {
                // Apply negation
                if (negated) {
                    wordScore = -wordScore * 0.5; // Partial reversal
                }
                
                // Apply intensification
                if (intensified) {
                    wordScore *= 1.5;
                }
                
                score += wordScore;
                scoredWords++;
            }
        }
        
        // Normalize score
        if (scoredWords == 0) {
            return BigDecimal.ZERO;
        }
        
        double normalizedScore = score / Math.max(scoredWords, words.size() / 5.0);
        
        // Clamp to [-1.0, 1.0]
        normalizedScore = Math.max(-1.0, Math.min(1.0, normalizedScore));
        
        return BigDecimal.valueOf(normalizedScore).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate mood intensity from text.
     * @param text Input text
     * @param detectedMood The detected mood type
     * @return Intensity score between 0.0 and 1.0
     */
    public static BigDecimal calculateIntensity(String text, String detectedMood) {
        if (text == null || text.trim().isEmpty()) {
            return BigDecimal.valueOf(0.5);
        }
        
        String lower = text.toLowerCase();
        double intensity = 0.5; // baseline
        
        // Factor 1: Intensity modifiers
        long intensifierCount = INTENSIFIERS.stream()
            .filter(lower::contains)
            .count();
        intensity += intensifierCount * 0.1;
        
        // Factor 2: Multiple exclamation marks
        if (text.matches(".*[!]{2,}.*")) {
            intensity += 0.15;
        }
        
        // Factor 3: ALL CAPS words
        long capsWords = Arrays.stream(text.split("\\s+"))
            .filter(word -> word.length() > 2 && word.equals(word.toUpperCase()))
            .count();
        intensity += capsWords * 0.08;
        
        // Factor 4: Short, choppy sentences (indicates emotional state)
        String[] sentences = text.split("[.!?]+");
        if (sentences.length > 2) {
            double avgLength = Arrays.stream(sentences)
                .mapToInt(String::length)
                .average()
                .orElse(50.0);
            
            if (avgLength < 20) {
                intensity += 0.1; // Short sentences = high emotion
            }
        }
        
        // Factor 5: Repetition of words
        List<String> words = tokenize(lower);
        Set<String> uniqueWords = new HashSet<>(words);
        if (words.size() > 10 && uniqueWords.size() < words.size() * 0.7) {
            intensity += 0.1; // Repetition indicates intensity
        }
        
        // Factor 6: Strong negative keywords
        if (STRONG_NEGATIVE.stream().anyMatch(lower::contains)) {
            intensity += 0.2;
        }
        
        // Factor 7: Message length (very short or very long can indicate intensity)
        int length = text.length();
        if (length < 20 || length > 300) {
            intensity += 0.05;
        }
        
        // Clamp to [0.0, 1.0]
        intensity = Math.max(0.0, Math.min(1.0, intensity));
        
        return BigDecimal.valueOf(intensity).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Count positive words in text.
     */
    public static int countPositiveWords(String text) {
        if (text == null) return 0;
        String lower = text.toLowerCase();
        return (int) POSITIVE_WORDS.stream()
            .filter(lower::contains)
            .count();
    }
    
    /**
     * Count negative words in text.
     */
    public static int countNegativeWords(String text) {
        if (text == null) return 0;
        String lower = text.toLowerCase();
        return (int) NEGATIVE_WORDS.stream()
            .filter(lower::contains)
            .count();
    }
    
    /**
     * Tokenize text into words.
     */
    private static List<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase()
            .replaceAll("[^a-z\\s']", " ")
            .split("\\s+"))
            .filter(s -> s.length() > 1)
            .collect(Collectors.toList());
    }
    
    /**
     * Check if text contains any of the given keywords.
     */
    public static boolean containsAny(String text, String... keywords) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        return Arrays.stream(keywords)
            .anyMatch(lower::contains);
    }
}
