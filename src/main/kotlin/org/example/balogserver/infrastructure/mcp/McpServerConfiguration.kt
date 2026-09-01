package org.example.balogserver.infrastructure.mcp

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.modelcontextprotocol.json.McpJsonMapper
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper
import io.modelcontextprotocol.server.McpServer
import io.modelcontextprotocol.server.McpServerFeatures
import io.modelcontextprotocol.server.McpSyncServer
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider
import io.modelcontextprotocol.spec.McpSchema
import jakarta.servlet.Servlet
import org.example.balogserver.infrastructure.mcp.auth.McpInitialConnectionProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Function

@Configuration
@EnableConfigurationProperties(McpInitialConnectionProperties::class)
class McpServerConfiguration(
    private val financialTools: McpFinancialTools,
    private val objectMapper: ObjectMapper,
) {
    @Bean
    fun mcpJsonMapper(): McpJsonMapper = JacksonMcpJsonMapper(
        objectMapper.copy().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES),
    )

    @Bean
    fun mcpTransport(): HttpServletStreamableServerTransportProvider = HttpServletStreamableServerTransportProvider.builder()
        .jsonMapper(mcpJsonMapper())
        .mcpEndpoint("/mcp")
        .disallowDelete(true)
        .build()

    @Bean
    fun mcpServlet(transport: HttpServletStreamableServerTransportProvider): ServletRegistrationBean<Servlet> =
        ServletRegistrationBean(transport, "/mcp")

    @Bean
    fun mcpServer(transport: HttpServletStreamableServerTransportProvider): McpSyncServer = McpServer.sync(transport)
        .serverInfo("balog", "1.0.0")
        .tools(
            listOf(
                tool(
                    "get_recent_transactions",
                    "Get the linked user's most recent transactions. Use this for a recent activity list.",
                    schema(mapOf("limit" to integerProperty("Maximum transactions to return, from 1 to 50.")), listOf("limit")),
                    financialTools::getRecentTransactions,
                ),
                tool(
                    "get_monthly_expense_summary",
                    "Get the linked user's expense total for a month and its comparison with the previous month.",
                    yearMonthSchema(),
                    financialTools::getMonthlyExpenseSummary,
                ),
                tool(
                    "get_category_expenses",
                    "Get the linked user's expense totals grouped by category for a month.",
                    yearMonthSchema(),
                    financialTools::getCategoryExpenses,
                ),
                tool(
                    "get_monthly_income_expense_summary",
                    "Get the linked user's income and expense totals for a month and their comparisons with the previous month.",
                    yearMonthSchema(),
                    financialTools::getMonthlyIncomeExpenseSummary,
                ),
            ),
        )
        .build()

    private fun tool(
        name: String,
        description: String,
        inputSchema: McpSchema.JsonSchema,
        handler: (Map<String, Any>) -> Map<String, Any>,
    ): McpServerFeatures.SyncToolSpecification {
        val tool = McpSchema.Tool.builder().name(name).description(description).inputSchema(inputSchema).build()
        return McpServerFeatures.SyncToolSpecification(tool) { _, request -> response(handler(request.arguments())) }
    }

    private fun yearMonthSchema(): McpSchema.JsonSchema = schema(
        mapOf(
            "year" to yearProperty(),
            "month" to mapOf("type" to "integer", "minimum" to 1, "maximum" to 12, "description" to "Calendar month from 1 to 12."),
        ),
        listOf("year", "month"),
    )

    private fun schema(properties: Map<String, Any>, required: List<String>): McpSchema.JsonSchema =
        McpSchema.JsonSchema("object", properties, required, false, null, null)

    private fun integerProperty(description: String): Map<String, Any> =
        mapOf("type" to "integer", "minimum" to 1, "maximum" to 50, "description" to description)

    private fun yearProperty(): Map<String, Any> =
        mapOf("type" to "integer", "minimum" to 2000, "maximum" to 2100, "description" to "Calendar year from 2000 to 2100.")

    private fun response(result: Map<String, Any>): McpSchema.CallToolResult = try {
        McpSchema.CallToolResult.builder()
            .addTextContent(objectMapper.writeValueAsString(result))
            .structuredContent(result)
            .isError("error" in result)
            .build()
    } catch (_: JsonProcessingException) {
        McpSchema.CallToolResult.builder().addTextContent("Unable to serialize MCP tool result").isError(true).build()
    }
}
