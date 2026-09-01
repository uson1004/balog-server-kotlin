package org.example.balogserver.infrastructure.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.example.balogserver.global.error.exception.ErrorCode
import org.example.balogserver.global.error.exception.GlobalException
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.util.StringUtils
import java.io.InputStream

@Configuration
@ConditionalOnProperty(prefix = "fcm", name = ["enabled"], havingValue = "true")
class FirebaseConfig(
    private val fcmProperties: FcmProperties,
    private val resourceLoader: ResourceLoader,
) {
    @Bean
    fun firebaseApp(): FirebaseApp {
        if (FirebaseApp.getApps().isNotEmpty()) return FirebaseApp.getInstance()
        return FirebaseApp.initializeApp(
            FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(openCredentialsStream())).build(),
        )
    }

    @Bean
    fun firebaseMessaging(firebaseApp: FirebaseApp): FirebaseMessaging = FirebaseMessaging.getInstance(firebaseApp)

    private fun openCredentialsStream(): InputStream {
        val credentialsPath = fcmProperties.credentialsPath
        if (!StringUtils.hasText(credentialsPath)) throw GlobalException(ErrorCode.FCM_CREDENTIALS_NOT_FOUND)
        val resource = resolveCredentialsResource(credentialsPath!!)
        if (!resource.exists()) throw GlobalException(ErrorCode.FCM_CREDENTIALS_NOT_FOUND)
        return resource.inputStream
    }

    private fun resolveCredentialsResource(credentialsPath: String): Resource = resourceLoader.getResource(
        if (credentialsPath.startsWith("classpath:") || credentialsPath.startsWith("file:")) credentialsPath else "file:$credentialsPath",
    )
}
