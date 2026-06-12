package com.mindful.wellness.util;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

/**
 * Utility for sentiment analysis using Stanford CoreNLP.
 * 
 * Analyzes text sentiment and returns a score between -1.0 (very negative)
 * and 1.0 (very positive).
 * 
 * Validates: Requirement 4
 */
@Component
@Slf4j
public class SentimentAnalysisUtil {

    private final StanfordCoreNLP pipeline;

    public SentimentAnalysisUtil() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,parse,sentiment");
        props.setProperty("ssplit.isOneSentence", "true");
        props.setProperty("tokenize.language", "en");
        this.pipeline = new StanfordCoreNLP(props);
    }

    /**
     * Analyze sentiment of the given text.
     * 
     * @param text the text to analyze
     * @return sentiment score between -1.0 and 1.0
     */
    public Double analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }

        try {
            Annotation annotation = new Annotation(text);
            pipeline.annotate(annotation);

            List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
            if (sentences == null || sentences.isEmpty()) {
                return 0.0;
            }

            double totalSentiment = 0.0;
            int sentenceCount = 0;

            for (CoreMap sentence : sentences) {
                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                if (tree != null) {
                    int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                    // Convert sentiment (0-4) to score (-1.0 to 1.0)
                    // 0 = very negative (-1.0)
                    // 1 = negative (-0.5)
                    // 2 = neutral (0.0)
                    // 3 = positive (0.5)
                    // 4 = very positive (1.0)
                    double score = (sentiment - 2) / 2.0;
                    totalSentiment += score;
                    sentenceCount++;
                }
            }

            if (sentenceCount == 0) {
                return 0.0;
            }

            double averageSentiment = totalSentiment / sentenceCount;
            // Clamp to [-1.0, 1.0]
            return Math.max(-1.0, Math.min(1.0, averageSentiment));
        } catch (Exception e) {
            log.warn("Error analyzing sentiment for text: {}", text, e);
            return 0.0;
        }
    }

    /**
     * Get sentiment category based on score.
     * 
     * @param sentimentScore the sentiment score
     * @return sentiment category (VERY_NEGATIVE, NEGATIVE, NEUTRAL, POSITIVE, VERY_POSITIVE)
     */
    public String getSentimentCategory(Double sentimentScore) {
        if (sentimentScore == null) {
            return "NEUTRAL";
        }

        if (sentimentScore < -0.6) {
            return "VERY_NEGATIVE";
        } else if (sentimentScore < -0.2) {
            return "NEGATIVE";
        } else if (sentimentScore < 0.2) {
            return "NEUTRAL";
        } else if (sentimentScore < 0.6) {
            return "POSITIVE";
        } else {
            return "VERY_POSITIVE";
        }
    }
}
