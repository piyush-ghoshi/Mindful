package com.mindful.wellness.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.FirebaseDatabase;
import com.google.cloud.firestore.Firestore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * Firebase configuration for Spring Boot application.
 * Initializes Firebase Admin SDK with service account credentials.
 */
@Configuration
public class FirebaseConfig {

    /**
     * Initialize Firebase Admin SDK with service account credentials.
     * The firebase-key.json file should be placed in src/main/resources/
     *
     * @return FirebaseApp instance
     * @throws IOException if the service account key file cannot be read
     */
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // Check if Firebase is already initialized
        if (FirebaseApp.getApps().isEmpty()) {
            // Try the actual service account file first, then fall back to generic name
            org.springframework.core.io.Resource resource;
            String[] candidates = {
                "mindful-54fd2-firebase-adminsdk-fbsvc-2e613fd09a.json",
                "firebase-key.json"
            };
            resource = null;
            for (String name : candidates) {
                ClassPathResource candidate = new ClassPathResource(name);
                if (candidate.exists()) {
                    resource = candidate;
                    break;
                }
            }
            if (resource == null) {
                throw new IOException("Firebase service account key file not found in classpath. " +
                    "Expected one of: " + java.util.Arrays.toString(candidates));
            }

            GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream());

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setDatabaseUrl("https://mindful-54fd2.firebaseio.com")
                    .setProjectId("mindful-54fd2")
                    .build();

            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }

    /**
     * Provide FirebaseAuth bean for authentication operations.
     * Explicitly depends on firebaseApp to guarantee initialization order.
     */
    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }

    /**
     * Provide Firestore bean — lazy so it doesn't crash startup if
     * Application Default Credentials aren't configured locally.
     */
    @Bean
    @org.springframework.context.annotation.Lazy
    public Firestore firestore(FirebaseApp firebaseApp) {
        return com.google.firebase.cloud.FirestoreClient.getFirestore(firebaseApp);
    }

    /**
     * Provide FirebaseDatabase bean for Realtime Database operations.
     */
    @Bean
    @org.springframework.context.annotation.Lazy
    public FirebaseDatabase firebaseDatabase(FirebaseApp firebaseApp) {
        return FirebaseDatabase.getInstance(firebaseApp);
    }
}
