package org.example.bankramenserver.global.ai

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(name = "geminiClient", url = "\${gemini.base-url}")
interface GeminiFeignClient { @PostMapping("/models/{model}:generateContent") fun generateContent(@RequestHeader("x-goog-api-key") apiKey: String, @PathVariable("model") model: String, @RequestBody request: GeminiPayload.GenerateContentRequest): GeminiPayload.GenerateContentResponse }
