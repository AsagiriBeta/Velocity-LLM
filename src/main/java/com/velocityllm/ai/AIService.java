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

    public CompletableFuture<String> chat(PluginConfig config, String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String requestJson = gson.toJson(buildRequestBody(config, prompt));
                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(URI.create(config.getApiUrl()))
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

                return parseResponse(response.body(), config.isMessagesFormat());
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    private Map<String, Object> buildRequestBody(PluginConfig config, String prompt) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", config.getModel());
        body.put("stream", false);

        if (config.isMessagesFormat()) {
            List<Map<String, String>> messages = new ArrayList<>();
            if (config.getSystemPrompt() != null && !config.getSystemPrompt().isBlank()) {
                messages.add(Map.of("role", "system", "content", config.getSystemPrompt()));
            }
            messages.add(Map.of("role", "user", "content", prompt));
            body.put("messages", messages);
        } else {
            String fullPrompt = config.getSystemPrompt() == null || config.getSystemPrompt().isBlank()
                    ? prompt
                    : config.getSystemPrompt() + "\n\n" + prompt;
            body.put("prompt", fullPrompt);
        }

        return body;
    }

    private String parseResponse(String body, boolean messagesFormat) {
        JsonObject json = gson.fromJson(body, JsonObject.class);
        if (messagesFormat) {
            JsonArray choices = json.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new IllegalStateException("AI 返回为空");
            }
            return choices.get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content")
                    .getAsString();
        }

        if (json.has("response")) {
            return json.get("response").getAsString();
        }
        throw new IllegalStateException("无法解析 AI 响应");
    }
}
