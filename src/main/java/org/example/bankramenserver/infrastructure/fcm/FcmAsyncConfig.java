package org.example.bankramenserver.infrastructure.fcm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class FcmAsyncConfig {

    public static final String FCM_PUSH_TASK_EXECUTOR = "fcmPushTaskExecutor";

    @Bean(name = FCM_PUSH_TASK_EXECUTOR)
    public Executor fcmPushTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("fcm-push-");
        executor.initialize();
        return executor;
    }
}
