package com.velocityllm.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

public final class EmbeddingService {

    private final Gson gson = new Gson();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public CompletableFuture<float[]> embed(PluginConfig config, String text) {
        return embedBatch(config, List.of(text)).thenApply(vectors -> vectors.getFirst());
    }

    public CompletableFuture<List<float[]>> embedBatch(PluginConfig config, List<String> texts) {
        if (texts.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> body = new HashMap<>();
                body.put("model", config.getEmbeddingModel());
                body.put("input", texts.size() == 1 ? texts.getFirst() : texts);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(OllamaUrls.embeddings(config.getBaseUrl())))
                        .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new IllegalStateException("HTTP " + response.statusCode() + ": " + response.body());
                }

                return parseEmbeddings(response.body(), texts.size());
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
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

        if (json.has("embedding")) {
            return List.of(toFloatArray(json.getAsJsonArray("embedding")));
        }

        throw new IllegalStateException("无法解析 embedding 响应");
    }

    private float[] toFloatArray(JsonArray array) {
        float[] values = new float[array.size()];
        for (int i = 0; i < array.size(); i++) {
            values[i] = array.get(i).getAsFloat();
        }
        return values;
    }
}
