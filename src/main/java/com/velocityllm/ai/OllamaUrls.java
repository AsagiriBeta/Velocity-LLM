package com.velocityllm.ai;

public final class OllamaUrls {

    private OllamaUrls() {
    }

    public static String normalizeBaseUrl(String url) {
        if (url == null || url.isBlank()) {
            return "http://127.0.0.1:11434";
        }
        String trimmed = url.trim();
        if (trimmed.endsWith("/api/chat") || trimmed.endsWith("/api/generate") || trimmed.endsWith("/api/embeddings")) {
            int index = trimmed.lastIndexOf("/api/");
            return trimmed.substring(0, index);
        }
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    public static String chat(String baseUrl) {
        return normalizeBaseUrl(baseUrl) + "/api/chat";
    }

    public static String generate(String baseUrl) {
        return normalizeBaseUrl(baseUrl) + "/api/generate";
    }

    public static String embeddings(String baseUrl) {
        return normalizeBaseUrl(baseUrl) + "/api/embeddings";
    }
}
