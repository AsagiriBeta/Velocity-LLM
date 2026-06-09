package com.velocityllm.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.velocityllm.config.PluginConfig;
import org.slf4j.Logger;

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

public final class EmbeddingService {

    private final Gson gson = new Gson();
    private final HttpClient httpClient;
    private final Logger logger;

    public EmbeddingService(Logger logger) {
        this.logger = logger;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public CompletableFuture<float[]> embed(PluginConfig config, String text) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> body = new HashMap<>();
                body.put("model", config.getEmbeddingModel());
                body.put("input", text);

                String json = gson.toJson(body);
                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(URI.create(OllamaUrls.embeddings(config.getBaseUrl())))
                        .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json));

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

                return parseEmbedding(response.body());
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<List<float[]>> embedBatch(PluginConfig config, List<String> texts) {
        if (texts.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> body = new HashMap<>();
                body.put("model", config.getEmbeddingModel());
                body.put("input", texts);

                String json = gson.toJson(body);
                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(URI.create(OllamaUrls.embeddings(config.getBaseUrl())))
                        .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json));

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

                return parseEmbeddings(response.body(), texts.size());
            } catch (Exception e) {
                logger.debug("批量 embedding 失败，改为逐条请求: {}", e.getMessage());
                List<float[]> vectors = new ArrayList<>(texts.size());
                for (String text : texts) {
                    vectors.add(embed(config, text).join());
                }
                return vectors;
            }
        });
    }

    private float[] parseEmbedding(String body) {
        JsonObject json = gson.fromJson(body, JsonObject.class);
        if (json.has("embeddings")) {
            JsonArray embeddings = json.getAsJsonArray("embeddings");
            if (!embeddings.isEmpty()) {
                return toFloatArray(embeddings.get(0).getAsJsonArray());
            }
        }
        if (json.has("embedding")) {
            return toFloatArray(json.getAsJsonArray("embedding"));
        }
        throw new IllegalStateException("无法解析 embedding 响应");
    }

    private List<float[]> parseEmbeddings(String body, int expectedSize) {
        JsonObject json = gson.fromJson(body, JsonObject.class);
        if (json.has("embeddings")) {
            JsonArray embeddings = json.getAsJsonArray("embeddings");
            List<float[]> result = new ArrayList<>(embeddings.size());
            for (JsonElement element : embeddings) {
                result.add(toFloatArray(element.getAsJsonArray()));
            }
            return result;
        }

        if (expectedSize == 1 && json.has("embedding")) {
            return List.of(toFloatArray(json.getAsJsonArray("embedding")));
        }

        throw new IllegalStateException("无法解析批量 embedding 响应");
    }

    private float[] toFloatArray(JsonArray array) {
        float[] values = new float[array.size()];
        for (int i = 0; i < array.size(); i++) {
            values[i] = array.get(i).getAsFloat();
        }
        return values;
    }
}
