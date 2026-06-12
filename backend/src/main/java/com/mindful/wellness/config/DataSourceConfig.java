package com.mindful.wellness.config;

import org.springframework.context.annotation.Configuration;

/**
 * DataSource configuration.
 *
 * Spring Boot auto-configures HikariCP from application.properties
 * (spring.datasource.url / username / password + hikari pool settings).
 * No manual bean definition is needed — having one here would override
 * auto-config and break the connection URL injection.
 */
@Configuration
public class DataSourceConfig {
    // Intentionally empty: let Spring Boot auto-configure the DataSource.
}
