package org.example.bankramenserver.global.ai

object GeminiPayload {
    data class GenerateContentRequest(val contents: List<Content>, val generationConfig: GenerationConfig) { fun contents() = contents; fun generationConfig() = generationConfig; companion object { @JvmStatic fun from(prompt: String) = GenerateContentRequest(listOf(Content(listOf(Part(prompt)))), GenerationConfig(0.0, 8)) } }
    data class GenerationConfig(val temperature: Double, val maxOutputTokens: Int) { fun temperature() = temperature; fun maxOutputTokens() = maxOutputTokens }
    data class GenerateContentResponse(val candidates: List<Candidate>?) { fun candidates() = candidates }
    data class Candidate(val content: Content?) { fun content() = content }
    data class Content(val parts: List<Part>?) { fun parts() = parts }
    data class Part(val text: String?) { fun text() = text }
}
