package org.example.bankramenserver.global.ai;

import java.util.List;

public final class GeminiPayload {

    private GeminiPayload() {
    }

    public record GenerateContentRequest(List<Content> contents, GenerationConfig generationConfig) {

        public static GenerateContentRequest from(String prompt) {
            return new GenerateContentRequest(
                    List.of(new Content(List.of(new Part(prompt)))),
                    new GenerationConfig(0.0, 8)
            );
        }
    }

    public record GenerationConfig(Double temperature, Integer maxOutputTokens) {
    }

    public record GenerateContentResponse(List<Candidate> candidates) {
    }

    public record Candidate(Content content) {
    }

    public record Content(List<Part> parts) {
    }

    public record Part(String text) {
    }
}
