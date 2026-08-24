package org.example.bankramenserver.global.ai;

import org.example.bankramenserver.domain.category.domain.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeminiCategoryRecommendationClientTest {

    @Mock
    private GeminiFeignClient geminiFeignClient;

    private GeminiProperties geminiProperties;
    private GeminiCategoryRecommendationClient client;

    @BeforeEach
    void setUp() {
        geminiProperties = new GeminiProperties();
        geminiProperties.setApiKey("test-api-key");
        geminiProperties.setModel("gemini-2.5-flash");
        geminiProperties.setBaseUrl("https://generativelanguage.googleapis.com/v1beta");
        client = new GeminiCategoryRecommendationClient(geminiFeignClient, geminiProperties);
    }

    @Test
    void recommendRequestsGeminiWithConfiguredModelAndParsesCategoryCode() {
        when(geminiFeignClient.generateContent(eq("test-api-key"), eq("gemini-2.5-flash"), any()))
                .thenReturn(new GeminiPayload.GenerateContentResponse(List.of(
                        new GeminiPayload.Candidate(new GeminiPayload.Content(List.of(
                                new GeminiPayload.Part("CAFE_SNACK")
                        )))
                )));

        Optional<Category> category = client.recommend("스타벅스 강남점");

        assertThat(category).contains(Category.CAFE_SNACK);
        ArgumentCaptor<GeminiPayload.GenerateContentRequest> requestCaptor = ArgumentCaptor.forClass(
                GeminiPayload.GenerateContentRequest.class
        );
        verify(geminiFeignClient).generateContent(eq("test-api-key"), eq("gemini-2.5-flash"), requestCaptor.capture());
        assertThat(requestCaptor.getValue().contents().get(0).parts().get(0).text())
                .contains("스타벅스 강남점")
                .contains("CAFE_SNACK")
                .contains("UNCATEGORIZED");
        assertThat(requestCaptor.getValue().generationConfig().temperature()).isEqualTo(0.0);
        assertThat(requestCaptor.getValue().generationConfig().maxOutputTokens()).isEqualTo(8);
    }

    @Test
    void recommendReturnsEmptyWhenGeminiFails() {
        when(geminiFeignClient.generateContent(eq("test-api-key"), eq("gemini-2.5-flash"), any()))
                .thenThrow(new RuntimeException("gemini error"));

        Optional<Category> category = client.recommend("스타벅스 강남점");

        assertThat(category).isEmpty();
    }

    @Test
    void recommendReturnsEmptyWhenApiKeyIsMissing() {
        geminiProperties.setApiKey("");

        Optional<Category> category = client.recommend("스타벅스 강남점");

        assertThat(category).isEmpty();
        verifyNoInteractions(geminiFeignClient);
    }
}
