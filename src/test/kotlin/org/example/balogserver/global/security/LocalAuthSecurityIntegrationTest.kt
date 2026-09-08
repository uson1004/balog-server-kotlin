package org.example.balogserver.global.security

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.example.balogserver.domain.auth.service.JwtService
import org.example.balogserver.domain.user.config.SingleUserProperties
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@SpringBootTest(
    classes = [SecurityTestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.config.location=optional:classpath:/jwt-test-empty.properties"],
)
@ActiveProfiles("local")
class LocalAuthSecurityIntegrationTest {
    @LocalServerPort private var port: Int = 0
    @Autowired private lateinit var jwt: JwtService
    @Autowired private lateinit var singleUser: SingleUserProperties
    @Autowired private lateinit var mapper: ObjectMapper

    @Test
    fun localOnlyLoginStillIssuesDevelopmentTokens() {
        val request = HttpRequest.newBuilder(URI("http://127.0.0.1:$port/auth/local/login")).GET().build()
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        assertThat(response.statusCode()).isEqualTo(200)
        val access = mapper.readTree(response.body()).path("accessToken").asText()
        assertThat(jwt.validateAccessToken(access)).isEqualTo(singleUser.id)
    }
}
