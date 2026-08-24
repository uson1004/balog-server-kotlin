package org.example.bankramenserver.infrastructure.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.global.error.exception.ErrorCode;
import org.example.bankramenserver.global.error.exception.GlobalException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "fcm", name = "enabled", havingValue = "true")
public class FirebaseConfig {

    private final FcmProperties fcmProperties;
    private final ResourceLoader resourceLoader;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(openCredentialsStream()))
                .build();

        return FirebaseApp.initializeApp(options);
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    private InputStream openCredentialsStream() throws IOException {
        if (!StringUtils.hasText(fcmProperties.getCredentialsPath())) {
            throw new GlobalException(ErrorCode.FCM_CREDENTIALS_NOT_FOUND);
        }

        Resource credentialsResource = resolveCredentialsResource(fcmProperties.getCredentialsPath());
        if (!credentialsResource.exists()) {
            throw new GlobalException(ErrorCode.FCM_CREDENTIALS_NOT_FOUND);
        }

        return credentialsResource.getInputStream();
    }

    private Resource resolveCredentialsResource(String credentialsPath) {
        if (credentialsPath.startsWith("classpath:") || credentialsPath.startsWith("file:")) {
            return resourceLoader.getResource(credentialsPath);
        }

        return resourceLoader.getResource("file:" + credentialsPath);
    }
}
