package org.example.balogserver.global.security

import io.jsonwebtoken.Jwts

import java.util.Base64

import org.example.balogserver.domain.auth.service.JwtService
import org.example.balogserver.domain.auth.service.LocalAuthService
import org.example.balogserver.domain.auth.service.RefreshTokenService
import org.example.balogserver.domain.auth.controller.LocalAuthController
import org.example.balogserver.domain.transaction.domain.repository.TransactionRepository
import org.example.balogserver.domain.transaction.presentation.TransactionController
import org.example.balogserver.domain.transaction.service.*
import org.example.balogserver.domain.user.config.SingleUserProperties
import org.example.balogserver.domain.user.domain.repository.UserRepository
import org.example.balogserver.domain.user.facade.UserFacade
import org.example.balogserver.global.config.SecurityConfig
import org.example.balogserver.global.error.GlobalExceptionFilter
import org.example.balogserver.global.error.GlobalExceptionHandler
import org.example.balogserver.global.jwt.JwtAuthenticationFilter
import org.example.balogserver.infrastructure.mcp.auth.*
import org.example.balogserver.infrastructure.mcp.McpServerConfiguration
import org.example.balogserver.infrastructure.mcp.McpFinancialTools
import org.example.balogserver.domain.report.service.GetMonthlyAmountSummaryService
import org.example.balogserver.domain.report.service.GetMonthlyCategoryExpenseListService
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.example.balogserver.domain.user.domain.User
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import java.util.Optional
import java.util.UUID
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Configuration(proxyBeanMethods = false)
@ImportAutoConfiguration(
    ServletWebServerFactoryAutoConfiguration::class, DispatcherServletAutoConfiguration::class,
    WebMvcAutoConfiguration::class, JacksonAutoConfiguration::class, HttpMessageConvertersAutoConfiguration::class,
    SecurityAutoConfiguration::class, SecurityFilterAutoConfiguration::class,
)
@Import(
    SecurityConfig::class, GlobalExceptionFilter::class, GlobalExceptionHandler::class,
    JwtAuthenticationFilter::class, McpBearerAuthenticationFilter::class,
    McpAgentConnectionAuthenticationService::class, McpAgentPrincipalResolver::class,
    UserFacade::class, TransactionController::class, GetRecentTransactionListService::class,
    DeleteTransactionService::class, SecurityProbeController::class,
    McpServerConfiguration::class, McpFinancialTools::class,
    LocalAuthService::class, LocalAuthController::class,
)
class SecurityTestApplication {
    @Bean fun jwtService() = JwtService(TEST_SECRET, 300, 600)
    @Bean fun refreshTokens(): RefreshTokenService = mock(RefreshTokenService::class.java)
    @Bean fun singleUserProperties() = SingleUserProperties()
    @Bean fun users(): UserRepository = mock(UserRepository::class.java)
    @Bean fun transactions(): TransactionRepository = mock(TransactionRepository::class.java)
    @Bean fun connections(): AgentConnectionRepository = mock(AgentConnectionRepository::class.java)
    @Bean fun incomes(): GetMonthlyIncomeTransactionListService = mock(GetMonthlyIncomeTransactionListService::class.java)
    @Bean fun expenses(): GetMonthlyExpenseTransactionListService = mock(GetMonthlyExpenseTransactionListService::class.java)
    @Bean fun create(): CreateTransactionService = mock(CreateTransactionService::class.java)
    @Bean fun notification(): CreatePaymentNotificationTransactionService = mock(CreatePaymentNotificationTransactionService::class.java)
    @Bean fun category(): UpdateTransactionCategoryService = mock(UpdateTransactionCategoryService::class.java)
    @Bean fun reportSummary(): GetMonthlyAmountSummaryService = mock(GetMonthlyAmountSummaryService::class.java)
    @Bean fun reportCategories(): GetMonthlyCategoryExpenseListService = mock(GetMonthlyCategoryExpenseListService::class.java)

    companion object {
        val TEST_SECRET = Base64.getEncoder().encodeToString(Jwts.SIG.HS256.key().build().encoded)

        @JvmStatic
        fun main(args: Array<String>) {
            val context = SpringApplicationBuilder(SecurityTestApplication::class.java)
                .properties("spring.config.location=optional:classpath:/jwt-test-empty.properties", "server.address=127.0.0.1", "server.port=0")
                .run(*args) as ServletWebServerApplicationContext
            val users = context.getBean(UserRepository::class.java)
            val userId = UUID.fromString("11111111-1111-4111-8111-111111111111")
            `when`(users.findById(userId)).thenReturn(Optional.of(User.singleUser(userId)))
            `when`(users.existsById(userId)).thenReturn(true)
            val hash = McpTokenHasher.hash("agent-test-token")
            `when`(context.getBean(AgentConnectionRepository::class.java).findByTokenHash(hash)).thenReturn(Optional.of(
                AgentConnection.of("test", "test-agent", userId, true, hash, setOf(AgentScope.TRANSACTIONS_READ)),
            ))
            println("SECURITY_FIXTURE_PORT=${context.webServer.port}")
        }
    }
}

@RestController
class SecurityProbeController {
    @GetMapping("/swagger-ui/probe") fun publicResource() = "public"
}
