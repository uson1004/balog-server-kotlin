package org.example.bankramenserver.global.ai

import org.assertj.core.api.Assertions.assertThat
import org.example.bankramenserver.domain.category.domain.Category
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class GeminiCategoryRecommendationClientTest {
    @Mock private lateinit var geminiFeignClient: GeminiFeignClient
    private lateinit var geminiProperties: GeminiProperties
    private lateinit var client: GeminiCategoryRecommendationClient

    @BeforeEach fun setUp() { geminiProperties = GeminiProperties().also { it.apiKey = "test-api-key"; it.model = "gemini-2.5-flash"; it.baseUrl = "https://generativelanguage.googleapis.com/v1beta" }; client = GeminiCategoryRecommendationClient(geminiFeignClient, geminiProperties) }

    @Test fun recommendRequestsGeminiWithConfiguredModelAndParsesCategoryCode() {
        `when`(geminiFeignClient.generateContent(eqString("test-api-key"), eqString("gemini-2.5-flash"), anyRequest())).thenReturn(GeminiPayload.GenerateContentResponse(listOf(GeminiPayload.Candidate(GeminiPayload.Content(listOf(GeminiPayload.Part("CAFE_SNACK")))))))
        assertThat(client.recommend("스타벅스 강남점")).contains(Category.CAFE_SNACK)
        val captor = ArgumentCaptor.forClass(GeminiPayload.GenerateContentRequest::class.java)
        verify(geminiFeignClient).generateContent(eqString("test-api-key"), eqString("gemini-2.5-flash"), captor.capture() ?: GeminiPayload.GenerateContentRequest.from(""))
        assertThat(captor.value.contents[0].parts!![0].text).contains("스타벅스 강남점", "CAFE_SNACK", "UNCATEGORIZED")
        assertThat(captor.value.generationConfig.temperature).isEqualTo(0.0)
        assertThat(captor.value.generationConfig.maxOutputTokens).isEqualTo(8)
    }

    @Test fun recommendReturnsEmptyWhenGeminiFails() { `when`(geminiFeignClient.generateContent(eqString("test-api-key"), eqString("gemini-2.5-flash"), anyRequest())).thenThrow(RuntimeException("gemini error")); assertThat(client.recommend("스타벅스 강남점")).isEmpty() }
    @Test fun recommendReturnsEmptyWhenApiKeyIsMissing() { geminiProperties.apiKey = ""; assertThat(client.recommend("스타벅스 강남점")).isEmpty(); verifyNoInteractions(geminiFeignClient) }
    private fun eqString(value: String): String = eq(value) ?: value
    private fun anyRequest(): GeminiPayload.GenerateContentRequest = any(GeminiPayload.GenerateContentRequest::class.java) ?: GeminiPayload.GenerateContentRequest.from("")
}
