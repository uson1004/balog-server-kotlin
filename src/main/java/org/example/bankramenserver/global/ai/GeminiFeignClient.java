package org.example.bankramenserver.global.ai;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "geminiClient", url = "${gemini.base-url}")
public interface GeminiFeignClient {

    @PostMapping("/models/{model}:generateContent")
    GeminiPayload.GenerateContentResponse generateContent(
            @RequestHeader("x-goog-api-key") String apiKey,
            @PathVariable("model") String model,
            @RequestBody GeminiPayload.GenerateContentRequest request
    );
}
