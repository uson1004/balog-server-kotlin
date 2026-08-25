package org.example.bankramenserver.infrastructure.integration

import org.example.bankramenserver.infrastructure.integration.domain.IntegrationConnectionProperties
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationOutbox
import org.springframework.stereotype.Component
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.HexFormat
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class HermesWebhookConnector(
    private val httpClient: HttpClient = HttpClient.newHttpClient(),
) : IntegrationConnector {
    override fun connectorType(): String = CONNECTOR_TYPE

    @Throws(IOException::class, InterruptedException::class)
    override fun dispatch(outbox: IntegrationOutbox, connection: IntegrationConnectionProperties.Connection) {
        val body = outbox.payload.toByteArray(StandardCharsets.UTF_8)
        val response = httpClient.send(
            HttpRequest.newBuilder(URI.create(connection.webhookUrl))
                .header("Content-Type", "application/json")
                .header("X-Hub-Signature-256", signature(body, connection.secret))
                .header("X-Request-ID", outbox.eventId)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build(),
            HttpResponse.BodyHandlers.discarding(),
        )
        if (response.statusCode() >= 400) throw IOException("Webhook returned HTTP ${response.statusCode()}")
    }

    companion object {
        private const val CONNECTOR_TYPE = "HERMES_WEBHOOK"

        @JvmStatic
        fun signature(body: ByteArray, secret: String): String = try {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
            "sha256=" + HexFormat.of().formatHex(mac.doFinal(body))
        } catch (exception: Exception) {
            throw IllegalStateException("Could not sign Hermes webhook payload", exception)
        }
    }
}
