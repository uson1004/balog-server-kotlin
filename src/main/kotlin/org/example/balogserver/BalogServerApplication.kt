package org.example.balogserver

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.example.balogserver.domain.user.config.SingleUserProperties

@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties(SingleUserProperties::class)
class BalogServerApplication

fun main(args: Array<String>) {
    SpringApplication.run(BalogServerApplication::class.java, *args)
}
