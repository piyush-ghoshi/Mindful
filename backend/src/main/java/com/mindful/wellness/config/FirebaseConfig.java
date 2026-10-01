package com.mindful.wellness.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.FirebaseDatabase;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

/**
 * Firebase configuration for Spring Boot application.
 * Initializes Firebase Admin SDK with service account credentials.
 *
 * Credential loading order:
 *   1. FIREBASE_CREDENTIALS_JSON environment variable (raw JSON string)
 *   2. /etc/secrets/FIREBASE_CREDENTIALS_JSON  (Render secret file)
 *   3. /etc/secrets/firebase-key.json           (Render secret file)
 *   4. FIREBASE_CREDENTIALS_PATH property       (external file path)
 *
 * NOTE: Credentials are NEVER loaded from classpath. Service account JSON
 * files must not be committed to the repository.
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    /**
     * Initialize Firebase Admin SDK with service account credentials.
     *
     * @return FirebaseApp instance
     * @throws IOException if no valid credentials source is found
     */
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // Check if Firebase is already initialized
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        GoogleCredentials credentials = null;

        // 1. Try FIREBASE_CREDENTIALS_JSON env var (raw JSON string)
        String envJson = System.getenv("FIREBASE_CREDENTIALS_JSON");
        if (envJson != null && !envJson.trim().isEmpty()) {
            credentials = GoogleCredentials.fromStream(
                new java.io.ByteArrayInputStream(envJson.getBytes(java.nio.charset.StandardCharsets.UTF_8))
            );
        }

        // 2. Try Render secret file path /etc/secrets/FIREBASE_CREDENTIALS_JSON
        if (credentials == null) {
            java.io.File secretFile = new java.io.File("/etc/secrets/FIREBASE_CREDENTIALS_JSON");
            if (secretFile.exists() && secretFile.canRead()) {
                try (java.io.FileInputStream fis = new java.io.FileInputStream(secretFile)) {
                    credentials = GoogleCredentials.fromStream(fis);
                }
            }
        }

        // 3. Try alternative Render secret file path /etc/secrets/firebase-key.json
        if (credentials == null) {
            java.io.File secretFile = new java.io.File("/etc/secrets/firebase-key.json");
            if (secretFile.exists() && secretFile.canRead()) {
                try (java.io.FileInputStream fis = new java.io.FileInputStream(secretFile)) {
                    credentials = GoogleCredentials.fromStream(fis);
                }
            }
        }

        // 4. Try external file path from FIREBASE_CREDENTIALS_PATH property
        if (credentials == null && credentialsPath != null && !credentialsPath.trim().isEmpty()
                && !credentialsPath.startsWith("classpath:")) {
            java.io.File externalFile = new java.io.File(credentialsPath);
            if (externalFile.exists() && externalFile.canRead()) {
                try (java.io.FileInputStream fis = new java.io.FileInputStream(externalFile)) {
                    credentials = GoogleCredentials.fromStream(fis);
                }
            }
        }

        if (credentials == null) {
            log.warn("Firebase credentials not configured. Firebase authentication features will be disabled.");
            return null;
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        return FirebaseApp.initializeApp(options);
    }

    /**
     * Provide FirebaseAuth bean for authentication operations.
     * Returns null if FirebaseApp is not initialized (e.g. In tests or without credentials).
     */
    @Bean
    public FirebaseAuth firebaseAuth(org.springframework.beans.factory.ObjectProvider<FirebaseApp> firebaseAppProvider) {
        FirebaseApp app = firebaseAppProvider.getIfAvailable();
        if (app == null) {
            return null;
        }
        return FirebaseAuth.getInstance(app);
    }

    /**
     * Provide Firestore bean — lazy so it doesn't crash startup if
     * Application Default Credentials aren't configured locally.
     */
    @Bean
    @Lazy
    public Firestore firestore(org.springframework.beans.factory.ObjectProvider<FirebaseApp> firebaseAppProvider) {
        FirebaseApp app = firebaseAppProvider.getIfAvailable();
        if (app == null) {
            return null;
        }
        return FirestoreClient.getFirestore(app);
    }

    /**
     * Provide FirebaseDatabase bean for Realtime Database operations.
     */
    @Bean
    @Lazy
    public FirebaseDatabase firebaseDatabase(org.springframework.beans.factory.ObjectProvider<FirebaseApp> firebaseAppProvider) {
        FirebaseApp app = firebaseAppProvider.getIfAvailable();
        if (app == null) {
            return null;
        }
        return FirebaseDatabase.getInstance(app);
    }
}
