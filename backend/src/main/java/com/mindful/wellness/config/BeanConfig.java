package com.mindful.wellness.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;

/**
 * Configuration for application beans.
 * 
 * Validates: Requirements 1, 2, 3
 */
@Configuration
public class BeanConfig {

    /**
     * Customise the auto-configured ObjectMapper used by Spring MVC's
     * message converters so that Java 8 date/time types (LocalDateTime, etc.)
     * are serialised as ISO-8601 strings instead of arrays/timestamps.
     *
     * NOTE: Do NOT create a standalone ObjectMapper @Bean — that shadows
     * auto-configuration but does NOT replace the one used by
     * MappingJackson2HttpMessageConverter, causing LocalDateTime
     * serialisation failures.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder
                .modules(new JavaTimeModule())
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
