package org.example.bankramenserver.infrastructure.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.servlet.Servlet;
import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.infrastructure.mcp.auth.McpInitialConnectionProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(McpInitialConnectionProperties.class)
public class McpServerConfiguration {

    private final McpFinancialTools financialTools;
    private final ObjectMapper objectMapper;

    @Bean
    public McpJsonMapper mcpJsonMapper() {
        return new JacksonMcpJsonMapper(objectMapper);
    }

    @Bean
    public HttpServletStreamableServerTransportProvider mcpTransport() {
        return HttpServletStreamableServerTransportProvider.builder()
                .jsonMapper(mcpJsonMapper())
                .mcpEndpoint("/mcp")
                .disallowDelete(true)
                .build();
    }

    @Bean
    public ServletRegistrationBean<Servlet> mcpServlet(HttpServletStreamableServerTransportProvider transport) {
        return new ServletRegistrationBean<>(transport, "/mcp");
    }

    @Bean
    public McpSyncServer mcpServer(HttpServletStreamableServerTransportProvider transport) {
        return McpServer.sync(transport)
                .serverInfo("bankramen", "1.0.0")
                .tools(List.of(
                        tool("get_recent_transactions", "Get the linked user's most recent transactions. Use this for a recent activity list.",
                                schema(Map.of("limit", integerProperty("Maximum transactions to return, from 1 to 50.")), List.of("limit")),
                                financialTools::getRecentTransactions),
                        tool("get_monthly_expense_summary", "Get the linked user's expense total for a month and its comparison with the previous month.",
                                yearMonthSchema(), financialTools::getMonthlyExpenseSummary),
                        tool("get_category_expenses", "Get the linked user's expense totals grouped by category for a month.",
                                yearMonthSchema(), financialTools::getCategoryExpenses),
                        tool("get_monthly_income_expense_summary", "Get the linked user's income and expense totals for a month and their comparisons with the previous month.",
                                yearMonthSchema(), financialTools::getMonthlyIncomeExpenseSummary)
                ))
                .build();
    }

    private McpServerFeatures.SyncToolSpecification tool(
            String name,
            String description,
            McpSchema.JsonSchema inputSchema,
            Function<Map<String, Object>, Map<String, Object>> handler
    ) {
        McpSchema.Tool tool = McpSchema.Tool.builder()
                .name(name)
                .description(description)
                .inputSchema(inputSchema)
                .build();
        return new McpServerFeatures.SyncToolSpecification(tool, (exchange, request) -> response(handler.apply(request.arguments())));
    }

    private McpSchema.JsonSchema yearMonthSchema() {
        return schema(Map.of(
                "year", yearProperty(),
                "month", Map.of("type", "integer", "minimum", 1, "maximum", 12, "description", "Calendar month from 1 to 12.")
        ), List.of("year", "month"));
    }

    private McpSchema.JsonSchema schema(Map<String, Object> properties, List<String> required) {
        return new McpSchema.JsonSchema("object", properties, required, false, null, null);
    }

    private Map<String, Object> integerProperty(String description) {
        return Map.of("type", "integer", "minimum", 1, "maximum", 50, "description", description);
    }

    private Map<String, Object> yearProperty() {
        return Map.of("type", "integer", "minimum", 2000, "maximum", 2100, "description", "Calendar year from 2000 to 2100.");
    }

    private McpSchema.CallToolResult response(Map<String, Object> result) {
        try {
            return McpSchema.CallToolResult.builder()
                    .addTextContent(objectMapper.writeValueAsString(result))
                    .structuredContent(result)
                    .isError(result.containsKey("error"))
                    .build();
        } catch (JsonProcessingException exception) {
            return McpSchema.CallToolResult.builder()
                    .addTextContent("Unable to serialize MCP tool result")
                    .isError(true)
                    .build();
        }
    }
}
