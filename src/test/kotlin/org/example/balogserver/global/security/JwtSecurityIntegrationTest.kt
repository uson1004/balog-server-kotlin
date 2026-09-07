package org.example.balogserver.global.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.assertj.core.api.Assertions.assertThat
import org.example.balogserver.domain.auth.service.JwtService
import org.example.balogserver.domain.transaction.domain.Transaction
import org.example.balogserver.domain.transaction.domain.repository.TransactionRepository
import org.example.balogserver.domain.user.config.SingleUserProperties
import org.example.balogserver.domain.user.domain.User
import org.example.balogserver.domain.user.domain.repository.UserRepository
import org.example.balogserver.infrastructure.mcp.auth.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Date
import java.util.Optional
import java.util.UUID

@SpringBootTest(
    classes = [SecurityTestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.config.location=optional:classpath:/jwt-test-empty.properties"],
)
class JwtSecurityIntegrationTest {
    @LocalServerPort private var port: Int = 0
    @Autowired private lateinit var jwt: JwtService
    @Autowired private lateinit var users: UserRepository
    @Autowired private lateinit var transactions: TransactionRepository
    @Autowired private lateinit var connections: AgentConnectionRepository
    @Autowired private lateinit var singleUser: SingleUserProperties
    private val client = HttpClient.newHttpClient()
    private val alice = UUID.fromString("11111111-1111-4111-8111-111111111111")
    private val bob = UUID.fromString("22222222-2222-4222-8222-222222222222")

    @BeforeEach
    fun usersAndAgentConnection() {
        reset(users, transactions, connections)
        listOf(alice, bob, singleUser.id).forEach { id ->
            `when`(users.findById(id)).thenReturn(Optional.of(User.singleUser(id)))
            `when`(users.existsById(id)).thenReturn(true)
        }
        `when`(connections.findByTokenHash(McpTokenHasher.hash("agent-test-token"))).thenReturn(Optional.of(
            AgentConnection.of("test", "test-agent", alice, true, McpTokenHasher.hash("agent-test-token"), setOf(AgentScope.TRANSACTIONS_READ)),
        ))
    }

    @ParameterizedTest
    @ValueSource(strings = ["/transactions/recent", "/reports/monthly/summary", "/recurring-payments", "/device-tokens", "/auth/local/login"])
    fun missingTokenIsUnauthorized(path: String) {
        val response = request(path)
        assertThat(response.statusCode()).isEqualTo(401)
        assertThat(response.body()).contains("MISSING_TOKEN")
        verifyNoInteractions(transactions)
    }

    @ParameterizedTest
    @ValueSource(strings = ["Bearer invalid", "Bearer ", "Basic invalid", "Bearer agent-test-token"])
    fun badCredentialsAreUnauthorized(authorization: String) {
        val response = request("/transactions/recent", authorization)
        assertThat(response.statusCode()).isEqualTo(401)
        verifyNoInteractions(transactions)
    }

    @Test
    fun validJwtSelectsEachSubjectInsteadOfConfiguredUser() {
        listOf(alice, bob).forEach { id ->
            val response = request("/transactions/recent", "Bearer ${jwt.generateAccessToken(id)}")
            assertThat(response.statusCode()).isEqualTo(200)
            assertThat(response.body()).contains("transactions")
            verify(transactions).findRecentTransactionHistories(id, 5)
        }
        verify(transactions, never()).findRecentTransactionHistories(singleUser.id, 5)
        assertThat(request("/transactions/recent").statusCode()).isEqualTo(401)
    }

    @Test
    fun foreignTransactionCannotBeDeleted() {
        val transactionId = UUID.randomUUID()
        val response = request("/transactions/$transactionId", "Bearer ${jwt.generateAccessToken(alice)}", "DELETE")
        assertThat(response.statusCode()).isEqualTo(404)
        verify(transactions).findByIdAndUser_Id(transactionId, alice)
        verifyNoMoreInteractions(transactions)
    }

    @Test
    fun ownTransactionCanBeDeleted() {
        val transactionId = UUID.randomUUID()
        val transaction = mock(Transaction::class.java)
        `when`(transactions.findByIdAndUser_Id(transactionId, alice)).thenReturn(Optional.of(transaction))
        val response = request("/transactions/$transactionId", "Bearer ${jwt.generateAccessToken(alice)}", "DELETE")
        assertThat(response.statusCode()).isEqualTo(204)
        verify(transactions).delete(transaction)
    }

    @Test
    fun refreshTokenIsNotAnAccessToken() {
        val response = request("/transactions/recent", "Bearer ${jwt.generateRefreshToken(alice)}")
        assertThat(response.statusCode()).isEqualTo(401)
        assertThat(response.body()).contains("INVALID_TOKEN")
    }

    @Test
    fun expiredTokenIsUnauthorized() {
        val response = request("/transactions/recent", "Bearer ${signedToken(alice.toString(), -60)}")
        assertThat(response.statusCode()).isEqualTo(401)
        assertThat(response.body()).contains("EXPIRED_TOKEN")
    }

    @Test
    fun malformedSubjectIsUnauthorizedRatherThanServerError() {
        val response = request("/transactions/recent", "Bearer ${signedToken("not-a-uuid", 300)}")
        assertThat(response.statusCode()).isEqualTo(401)
        assertThat(response.body()).contains("INVALID_TOKEN")
    }

    @Test
    fun deletedUserIsUnauthorized() {
        val response = request("/transactions/recent", "Bearer ${jwt.generateAccessToken(UUID.randomUUID())}")
        assertThat(response.statusCode()).isEqualTo(401)
    }

    @Test
    fun anotherSigningKeyIsRejected() {
        val other = JwtService("different-test-only-signing-key-0123456789abcdef0123456789abcdef", 300, 600)
        assertThat(request("/transactions/recent", "Bearer ${other.generateAccessToken(alice)}").statusCode()).isEqualTo(401)
    }

    @Test
    fun mcpAcceptsOnlyItsOwnTokenAndPreservesLinkedUser() {
        val initialize = mcpCall("""{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"security-test","version":"1"}}}""")
        assertThat(initialize.statusCode()).isEqualTo(200)
        val session = initialize.headers().firstValue("Mcp-Session-Id").orElseThrow()
        val result = mcpCall("""{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"get_recent_transactions","arguments":{"limit":5}}}""", session)
        assertThat(result.statusCode()).isEqualTo(200)
        assertThat(result.body()).contains("transactions").doesNotContain("\"isError\":true")
        verify(transactions).findRecentTransactionHistories(alice, 5)
        val bobHash = McpTokenHasher.hash("bob-agent-test-token")
        `when`(connections.findByTokenHash(bobHash)).thenReturn(Optional.of(
            AgentConnection.of("bob-test", "bob-agent", bob, true, bobHash, setOf(AgentScope.TRANSACTIONS_READ)),
        ))
        val bobResult = mcpCall("""{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"get_recent_transactions","arguments":{"limit":5}}}""", session, "bob-agent-test-token")
        assertThat(bobResult.body()).contains("transactions").doesNotContain("\"isError\":true")
        verify(transactions).findRecentTransactionHistories(bob, 5)
        val denied = mcpCall("""{"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"get_monthly_expense_summary","arguments":{"year":2026,"month":9}}}""", session)
        assertThat(denied.body()).contains("missing required MCP scope").contains("\"isError\":true")
        assertThat(request("/mcp", "Bearer ${jwt.generateAccessToken(alice)}").statusCode()).isEqualTo(401)
        assertThat(request("/mcp").statusCode()).isEqualTo(401)
    }

    private fun mcpCall(body: String, session: String? = null, token: String = "agent-test-token"): HttpResponse<String> {
        val request = HttpRequest.newBuilder(URI("http://127.0.0.1:$port/mcp"))
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json, text/event-stream")
            .POST(HttpRequest.BodyPublishers.ofString(body))
        session?.let { request.header("Mcp-Session-Id", it) }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString())
    }

    @Test
    fun documentationRemainsPublic() {
        assertThat(request("/swagger-ui/probe").statusCode()).isEqualTo(200)
    }

    private fun signedToken(subject: String, seconds: Long): String = Jwts.builder().subject(subject)
        .claim("type", "access").expiration(Date(System.currentTimeMillis() + seconds * 1000))
        .signWith(Keys.hmacShaKeyFor(SecurityTestApplication.TEST_SECRET.toByteArray())).compact()

    private fun request(path: String, authorization: String? = null, method: String = "GET"): HttpResponse<String> {
        val request = HttpRequest.newBuilder(URI("http://127.0.0.1:$port$path"))
            .method(method, HttpRequest.BodyPublishers.noBody())
        authorization?.let { request.header("Authorization", it) }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString())
    }
}
