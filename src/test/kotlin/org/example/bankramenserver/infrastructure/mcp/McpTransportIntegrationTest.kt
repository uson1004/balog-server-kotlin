package org.example.bankramenserver.infrastructure.mcp

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider
import org.apache.catalina.startup.Tomcat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.util.stream.StreamSupport

class McpTransportIntegrationTest {
    private val objectMapper = ObjectMapper()
    private val httpClient = HttpClient.newHttpClient()

    @Test
    fun streamableHttpEndpointDiscoversTheFourReadOnlyTools() {
        val configuration = McpServerConfiguration(mock(McpFinancialTools::class.java), objectMapper)
        val transport = configuration.mcpTransport()
        configuration.mcpServer(transport)
        val tomcat = Tomcat().apply {
            setBaseDir(Files.createTempDirectory("mcp-transport-test").toString())
            setPort(0)
        }
        val context = tomcat.addContext("", null)
        Tomcat.addServlet(context, "mcp", transport)
        context.addServletMappingDecoded("/mcp", "mcp")
        tomcat.start()
        try {
            val initializeResponse = call(
                tomcat.connector.localPort,
                """{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"test-client","version":"1.0"}}}""",
                null,
            )
            val sessionId = initializeResponse.headers().firstValue("Mcp-Session-Id").orElse(null)
            val toolsResponse = call(tomcat.connector.localPort, """{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}""", sessionId)
            val tools = responseBody(toolsResponse.body()).path("result").path("tools")

            assertThat(initializeResponse.statusCode()).describedAs(initializeResponse.body()).isEqualTo(200)
            assertThat(sessionId).isNotBlank()
            assertThat(toolsResponse.statusCode()).isEqualTo(200)
            assertThat(tools.map { it.path("name").asText() }).containsExactlyInAnyOrder(
                "get_recent_transactions", "get_monthly_expense_summary", "get_category_expenses", "get_monthly_income_expense_summary",
            )
            val monthlyExpenseSummary = StreamSupport.stream(tools.spliterator(), false)
                .filter { it.path("name").asText() == "get_monthly_expense_summary" }
                .findFirst()
                .orElseThrow()
            assertThat(monthlyExpenseSummary.path("inputSchema").path("properties").path("year").path("maximum").asInt()).isEqualTo(2100)
        } finally {
            tomcat.stop()
            tomcat.destroy()
        }
    }

    private fun call(port: Int, body: String, sessionId: String?): HttpResponse<String> {
        val request = HttpRequest.newBuilder(URI.create("http://localhost:$port/mcp"))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json, text/event-stream")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .apply { if (sessionId != null) header("Mcp-Session-Id", sessionId) }
            .build()
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private fun responseBody(body: String): JsonNode = objectMapper.readTree(
        body.lineSequence().firstOrNull { it.startsWith("data: ") }?.removePrefix("data: ") ?: body,
    )
}
