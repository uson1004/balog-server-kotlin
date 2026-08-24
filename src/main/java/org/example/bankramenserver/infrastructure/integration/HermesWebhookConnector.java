package org.example.bankramenserver.infrastructure.integration;

import lombok.extern.slf4j.Slf4j;
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationConnectionProperties;
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationOutbox;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Slf4j
@Component
public class HermesWebhookConnector implements IntegrationConnector {

    private static final String CONNECTOR_TYPE = "HERMES_WEBHOOK";
    private final HttpClient httpClient;

    public HermesWebhookConnector() {
        this(HttpClient.newHttpClient());
    }

    HermesWebhookConnector(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String connectorType() {
        return CONNECTOR_TYPE;
    }

    @Override
    public void dispatch(IntegrationOutbox outbox, IntegrationConnectionProperties.Connection connection)
            throws IOException, InterruptedException {
        byte[] body = outbox.getPayload().getBytes(StandardCharsets.UTF_8);
        HttpResponse<Void> response = httpClient.send(HttpRequest.newBuilder(URI.create(connection.webhookUrl()))
                        .header("Content-Type", "application/json")
                        .header("X-Hub-Signature-256", signature(body, connection.secret()))
                        .header("X-Request-ID", outbox.getEventId())
                        .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                        .build(), HttpResponse.BodyHandlers.discarding());
        if (response.statusCode() >= 400) {
            throw new IOException("Webhook returned HTTP " + response.statusCode());
        }
    }

    static String signature(byte[] body, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return "sha256=" + HexFormat.of().formatHex(mac.doFinal(body));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign Hermes webhook payload", exception);
        }
    }
}
