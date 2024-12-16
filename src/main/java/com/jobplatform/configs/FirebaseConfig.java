package com.jobplatform.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // Load the service account key from src/main/resources
//        GoogleCredentials credentials = GoogleCredentials.fromStream(
//                new ClassPathResource("firebase-key.json").getInputStream()
//        );
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("etc/secrets/firebase-key.json"));

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        return FirebaseApp.initializeApp(options);
    }
}
