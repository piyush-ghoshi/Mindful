package com.mindful.wellness.config;

import com.mindful.wellness.security.handler.JwtAccessDeniedHandler;
import com.mindful.wellness.security.handler.JwtAuthenticationEntryPoint;
import com.mindful.wellness.security.jwt.JwtAuthenticationFilter;
import com.mindful.wellness.security.jwt.FirebaseTokenFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Configuration for JWT-based authentication.
 * 
 * This configuration:
 * - Enables JWT-based stateless authentication
 * - Configures CORS and CSRF settings
 * - Sets up authorization rules for different endpoints
 * - Integrates JWT filter into the security chain
 * - Configures password encoding
 * - Sets up error handlers for authentication/authorization failures
 * 
 * Validates: Requirements 1, 2, 3, 25, 26
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private FirebaseTokenFilter firebaseTokenFilter;

    /**
     * Configure the security filter chain.
     * 
     * @param http the HttpSecurity object
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless API
                .csrf(csrf -> csrf.disable())
                
                // Configure CORS
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                    corsConfig.setAllowedOrigins(java.util.List.of(
        "http://localhost:3000",
        "http://localhost:5173",
        "http://localhost:5174",
        "https://mindful-umber.vercel.app"));
                    corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                    corsConfig.setAllowedHeaders(java.util.List.of("*"));
                    corsConfig.setAllowCredentials(true);
                    corsConfig.setMaxAge(3600L);
                    return corsConfig;
                }))
                
                // Set session management to stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Configure exception handling
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                
                // Configure authorization
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/firebase-auth/verify-token").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/health").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/crisis/**").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/resources").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/resources/").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/resources/**").permitAll()
                        // Forum read endpoints are public
                        .requestMatchers(HttpMethod.GET,  "/api/forum/posts").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/forum/posts/**").permitAll()
                        // Admin endpoints require ADMIN role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Chat endpoints — authenticated users only (handled by @PreAuthorize in controller)
                        .requestMatchers("/api/chat/**").authenticated()
                        
                        // All other requests require authentication
                        .anyRequest().authenticated())
                
                // Add JWT filter, then Firebase token filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(firebaseTokenFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configure the authentication manager.
     * 
     * @param config the AuthenticationConfiguration
     * @return the AuthenticationManager
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configure the password encoder.
     * 
     * Uses BCrypt with strength 12 for password hashing.
     * 
     * @return the PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
