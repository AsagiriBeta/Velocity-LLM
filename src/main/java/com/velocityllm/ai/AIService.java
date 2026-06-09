package com.velocityllm.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.velocityllm.config.PluginConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class AIService {

    private final Gson gson = new Gson();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public CompletableFuture<String> chat(PluginConfig config, List<ChatMessage> history, String userContent) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String requestJson = gson.toJson(buildRequestBody(config, history, userContent));
                String endpoint = resolveChatEndpoint(config);

                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestJson));

                if (config.getApiKey() != null && !config.getApiKey().isBlank()) {
                    builder.header("Authorization", "Bearer " + config.getApiKey());
                }

                HttpResponse<String> response = httpClient.send(
                        builder.build(),
                        HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() != 200) {
                    throw new IllegalStateException("HTTP " + response.statusCode() + ": " + response.body());
                }

                return parseResponse(response.body(), config.getApiStyle());
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    private String resolveChatEndpoint(PluginConfig config) {
        if (config.getApiStyle() == PluginConfig.ApiStyle.OPENAI) {
            return config.getBaseUrl();
        }
        return config.isUseChatApi()
                ? OllamaUrls.chat(config.getBaseUrl())
                : OllamaUrls.generate(config.getBaseUrl());
    }

    private Map<String, Object> buildRequestBody(PluginConfig config, List<ChatMessage> history, String userContent) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", config.getModel());
        body.put("stream", false);

        if (config.getTemperature() >= 0) {
            body.put("temperature", config.getTemperature());
        }

        if (config.getApiStyle() == PluginConfig.ApiStyle.OPENAI || config.isUseChatApi()) {
            List<Map<String, String>> messages = new ArrayList<>();
            if (config.getSystemPrompt() != null && !config.getSystemPrompt().isBlank()) {
                messages.add(Map.of("role", "system", "content", config.getSystemPrompt()));
            }
            for (ChatMessage message : history) {
                messages.add(Map.of("role", message.role(), "content", message.content()));
            }
            messages.add(Map.of("role", "user", "content", userContent));
            body.put("messages", messages);
            return body;
        }

        String fullPrompt = buildGeneratePrompt(config, history, userContent);
        body.put("prompt", fullPrompt);
        return body;
    }

    private String buildGeneratePrompt(PluginConfig config, List<ChatMessage> history, String userContent) {
        StringBuilder prompt = new StringBuilder();
        if (config.getSystemPrompt() != null && !config.getSystemPrompt().isBlank()) {
            prompt.append(config.getSystemPrompt()).append("\n\n");
        }
        for (ChatMessage message : history) {
            String label = "assistant".equals(message.role()) ? "AI" : "玩家";
            prompt.append(label).append(": ").append(message.content()).append('\n');
        }
        prompt.append("玩家: ").append(userContent).append("\nAI: ");
        return prompt.toString();
    }

    private String parseResponse(String body, PluginConfig.ApiStyle apiStyle) {
        JsonObject json = gson.fromJson(body, JsonObject.class);

        if (apiStyle == PluginConfig.ApiStyle.OPENAI) {
            JsonArray choices = json.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new IllegalStateException("AI 返回为空");
            }
            return choices.get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content")
                    .getAsString();
        }

        if (json.has("message")) {
            return json.getAsJsonObject("message").get("content").getAsString();
        }
        if (json.has("response")) {
            return json.get("response").getAsString();
        }
        throw new IllegalStateException("无法解析 Ollama 响应");
    }
}
