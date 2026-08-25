package org.example.bankramenserver.global.ai

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assumptions.assumeTrue

class GeminiCategoryRecommendationSmokeTest {
    @Test fun recommendCategoriesWithRealGeminiApi() {
        assumeTrue(System.getProperty("run.gemini.smoke") == "true" || System.getenv("RUN_GEMINI_SMOKE_TEST") == "true", "Set RUN_GEMINI_SMOKE_TEST=true or -Drun.gemini.smoke=true to run.")
        assumeTrue(!System.getProperty("gemini.api-key", System.getenv("GEMINI_API_KEY") ?: "").isBlank(), "Set GEMINI_API_KEY or -Dgemini.api-key to run.")
    }
}
