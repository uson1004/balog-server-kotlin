package org.example.balogserver.domain.user.config

import org.example.balogserver.domain.user.domain.User
import org.example.balogserver.domain.user.domain.repository.UserRepository
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SingleUserInitializer {
    @Bean
    fun initializeSingleUser(properties: SingleUserProperties, userRepository: UserRepository) = ApplicationRunner {
        if (!userRepository.existsById(properties.id)) userRepository.save(User.singleUser(properties.id))
    }
}
