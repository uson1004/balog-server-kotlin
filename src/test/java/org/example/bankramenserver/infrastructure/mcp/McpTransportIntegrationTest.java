package org.example.bankramenserver.infrastructure.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class McpTransportIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void streamableHttpEndpointDiscoversTheFourReadOnlyTools() throws Exception {
        McpServerConfiguration configuration = new McpServerConfiguration(mock(McpFinancialTools.class), objectMapper);
        HttpServletStreamableServerTransportProvider transport = configuration.mcpTransport();
        configuration.mcpServer(transport);
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(Files.createTempDirectory("mcp-transport-test").toString());
        tomcat.setPort(0);
        Context context = tomcat.addContext("", null);
        Tomcat.addServlet(context, "mcp", transport);
        context.addServletMappingDecoded("/mcp", "mcp");
        tomcat.start();
        try {
            HttpResponse<String> initializeResponse = call(tomcat.getConnector().getLocalPort(), """
                {"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"test-client","version":"1.0"}}}
                """, null);
            String sessionId = initializeResponse.headers().firstValue("Mcp-Session-Id").orElse(null);

            HttpResponse<String> toolsResponse = call(tomcat.getConnector().getLocalPort(), """
                {"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}
                """, sessionId);
            JsonNode tools = responseBody(toolsResponse.body()).path("result").path("tools");

            assertThat(initializeResponse.statusCode()).as(initializeResponse.body()).isEqualTo(200);
            assertThat(sessionId).isNotBlank();
            assertThat(toolsResponse.statusCode()).isEqualTo(200);
            assertThat(tools).extracting(node -> node.path("name").asText()).containsExactlyInAnyOrder(
                    "get_recent_transactions",
                    "get_monthly_expense_summary",
                    "get_category_expenses",
                    "get_monthly_income_expense_summary"
            );
            JsonNode monthlyExpenseSummary = StreamSupport.stream(tools.spliterator(), false)
                    .filter(tool -> "get_monthly_expense_summary".equals(tool.path("name").asText()))
                    .findFirst()
                    .orElseThrow();
            assertThat(monthlyExpenseSummary
                    .path("inputSchema").path("properties").path("year").path("maximum").asInt())
                    .isEqualTo(2100);
        } finally {
            tomcat.stop();
            tomcat.destroy();
        }
    }

    private HttpResponse<String> call(int port, String body, String sessionId) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/mcp"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (sessionId != null) {
            request.header("Mcp-Session-Id", sessionId);
        }
        return httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode responseBody(String body) throws Exception {
        String json = body.lines()
                .filter(line -> line.startsWith("data: "))
                .map(line -> line.substring(6))
                .findFirst()
                .orElse(body);
        return objectMapper.readTree(json);
    }

}
