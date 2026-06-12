package com.mindful.wellness.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration class for JPA and transaction management.
 * 
 * This configuration enables:
 * - JPA repository scanning for the com.mindful.wellness package
 * - Transaction management for database operations
 * - JPA auditing for automatic timestamp management (createdAt, updatedAt)
 * 
 * @author Mindful Wellness Team
 * @version 1.0.0
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.mindful.wellness.repository")
@EnableTransactionManagement
@EnableJpaAuditing
public class JpaConfig {

}
