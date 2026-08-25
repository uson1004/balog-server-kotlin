package org.example.bankramenserver.infrastructure.fcm

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
class FcmAsyncConfig {
    @Bean(name = [FCM_PUSH_TASK_EXECUTOR])
    fun fcmPushTaskExecutor(): Executor = ThreadPoolTaskExecutor().apply {
        corePoolSize = 2
        maxPoolSize = 5
        setQueueCapacity(100)
        setThreadNamePrefix("fcm-push-")
        initialize()
    }

    companion object {
        const val FCM_PUSH_TASK_EXECUTOR = "fcmPushTaskExecutor"
    }
}
