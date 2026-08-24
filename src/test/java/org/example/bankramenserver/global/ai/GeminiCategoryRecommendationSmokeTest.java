package org.example.bankramenserver.global.ai;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bankramenserver.domain.category.domain.Category;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class GeminiCategoryRecommendationSmokeTest {

    private static final String DEFAULT_MODEL = "gemini-2.5-flash";
    private static final String DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    private static final Path REPORT_PATH = Path.of("build/reports/gemini-category-smoke-test.md");

    @Test
    void recommendCategoriesWithRealGeminiApi() throws IOException {
        assumeTrue(isSmokeTestEnabled(), "Set RUN_GEMINI_SMOKE_TEST=true or -Drun.gemini.smoke=true to run.");

        String apiKey = propertyOrEnv("gemini.api-key", "GEMINI_API_KEY", "");
        assumeTrue(hasText(apiKey), "Set GEMINI_API_KEY or -Dgemini.api-key to run.");

        String model = propertyOrEnv("gemini.model", "GEMINI_MODEL", DEFAULT_MODEL);
        String baseUrl = propertyOrEnv("gemini.base-url", "GEMINI_BASE_URL", DEFAULT_BASE_URL);

        GeminiProperties geminiProperties = new GeminiProperties();
        geminiProperties.setApiKey(apiKey);
        geminiProperties.setModel(model);
        geminiProperties.setBaseUrl(baseUrl);

        RecordingGeminiFeignClient geminiFeignClient = new RecordingGeminiFeignClient(baseUrl);
        GeminiCategoryRecommendationClient client = new GeminiCategoryRecommendationClient(
                geminiFeignClient,
                geminiProperties
        );

        List<String> titles = List.of(
                "스타벅스 강남점",
                "카카오뱅크 송금",
                "홍길동님이 50000원 송금",
                "홍길동님에게 송금",
                "입금",
                "계좌이체",
                "토스페이먼츠 결제",
                "CU 편의점"
        );

        StringBuilder report = new StringBuilder()
                .append("# Gemini category recommendation smoke test\n\n")
                .append("- generatedAt: ").append(LocalDateTime.now()).append('\n')
                .append("- model: `").append(model).append("`\n")
                .append("- baseUrl: `").append(baseUrl).append("`\n")
                .append("- note: API key is intentionally omitted.\n\n")
                .append("| title | parsed category | raw Gemini text | http status |\n")
                .append("| --- | --- | --- | --- |\n");

        boolean hasTransportFailure = false;
        for (String title : titles) {
            Optional<Category> category = client.recommend(title);

            if (geminiFeignClient.lastStatusCode < 200 || geminiFeignClient.lastStatusCode >= 300) {
                hasTransportFailure = true;
            }

            report.append("| ")
                    .append(escapeTable(title))
                    .append(" | ")
                    .append(category.map(Category::name).orElse("(empty -> service fallback would be UNCATEGORIZED)"))
                    .append(" | ")
                    .append(escapeTable(geminiFeignClient.lastModelText()))
                    .append(" | ")
                    .append(geminiFeignClient.lastStatusCode)
                    .append(" |\n");
        }

        Files.createDirectories(REPORT_PATH.getParent());
        Files.writeString(REPORT_PATH, report.toString(), StandardCharsets.UTF_8);

        assertThat(hasTransportFailure)
                .as("Gemini API transport failed. See %s", REPORT_PATH)
                .isFalse();
    }

    private static boolean isSmokeTestEnabled() {
        return Boolean.parseBoolean(propertyOrEnv(
                "run.gemini.smoke",
                "RUN_GEMINI_SMOKE_TEST",
                "false"
        ));
    }

    private static String propertyOrEnv(String propertyName, String envName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (hasText(propertyValue)) {
            return propertyValue;
        }

        String envValue = System.getenv(envName);
        if (hasText(envValue)) {
            return envValue;
        }

        return defaultValue;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String escapeTable(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\r", " ")
                .replace("\n", "<br>")
                .replace("|", "\\|");
    }

    private static final class RecordingGeminiFeignClient implements GeminiFeignClient {

        private final String baseUrl;
        private final HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        private final ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        private int lastStatusCode;
        private String lastResponseBody = "";

        private RecordingGeminiFeignClient(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        @Override
        public GeminiPayload.GenerateContentResponse generateContent(
                String apiKey,
                String model,
                GeminiPayload.GenerateContentRequest request
        ) {
            try {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/models/" + model + ":generateContent"))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .header("x-goog-api-key", apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                lastStatusCode = response.statusCode();
                lastResponseBody = response.body();

                if (lastStatusCode < 200 || lastStatusCode >= 300) {
                    throw new IllegalStateException("Gemini API returned HTTP " + lastStatusCode);
                }

                return objectMapper.readValue(lastResponseBody, GeminiPayload.GenerateContentResponse.class);
            } catch (IOException e) {
                throw new IllegalStateException("Gemini API request failed", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Gemini API request interrupted", e);
            }
        }

        private String lastModelText() {
            if (!hasText(lastResponseBody)) {
                return "";
            }

            try {
                GeminiPayload.GenerateContentResponse response = objectMapper.readValue(
                        lastResponseBody,
                        GeminiPayload.GenerateContentResponse.class
                );
                if (response.candidates() == null) {
                    return lastResponseBody;
                }

                return response.candidates().stream()
                        .filter(candidate -> candidate.content() != null)
                        .filter(candidate -> candidate.content().parts() != null)
                        .flatMap(candidate -> candidate.content().parts().stream())
                        .map(GeminiPayload.Part::text)
                        .filter(GeminiCategoryRecommendationSmokeTest::hasText)
                        .findFirst()
                        .orElse(lastResponseBody);
            } catch (IOException ignored) {
                return lastResponseBody;
            }
        }
    }
}
