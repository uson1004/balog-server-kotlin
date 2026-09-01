package org.example.balogserver.global.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean fun openAPI(): OpenAPI { val jwt = "JWT"; return OpenAPI().info(apiInfo()).addSecurityItem(SecurityRequirement().addList(jwt)).components(Components().addSecuritySchemes(jwt, SecurityScheme().name(jwt).type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))) }
    private fun apiInfo() = Info().title("Balog Server API").description("Balog 서비스의 API 명세서입니다.").version("1.0.0")
}
