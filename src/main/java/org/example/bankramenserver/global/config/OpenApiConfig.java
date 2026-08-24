package org.example.bankramenserver.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Bankramen API",
                version = "v1",
                description = "Bankramen 서버 API 명세입니다."
        ),
        tags = {
                @Tag(name = "Category", description = "기본 제공 카테고리 조회 API"),
                @Tag(name = "Monthly Report", description = "월별 리포트 조회 API"),
                @Tag(name = "Transaction", description = "거래 내역 조회 및 관리 API"),
                @Tag(name = "Push Notification", description = "푸시 알림 조회 API")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
