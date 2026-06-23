package com.papercrown.desktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.papercrown.shared.dto.*;
import com.papercrown.shared.enums.Move;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class BackendClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BackendClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public boolean isHealthy() {
        try {
            String json = get("/health");
            Map<String, String> result = objectMapper.readValue(json,
                    new TypeReference<Map<String, String>>() {});
            return "UP".equals(result.get("status"));
        } catch (Exception e) {
            return false;
        }
    }

    public RunDTO startRun() {
        return post("/runs", null, RunDTO.class);
    }

    public RunDTO getUnfinishedRun() {
        try {
            return get("/runs/unfinished", RunDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void abandonUnfinishedRun() {
        post("/runs/abandon", null, Void.class);
    }

    public List<RunDTO> getAllRuns() {
        return getList("/runs", new TypeReference<List<RunDTO>>() {});
    }

    private <T> T getList(String path, TypeReference<T> typeRef) {
        String json = get(path);
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response: " + e.getMessage(), e);
        }
    }

    public RunDTO getRunById(Long id) {
        return get("/runs/" + id, RunDTO.class);
    }

    public MoveResponse submitMove(Long runId, Move move) {
        MoveRequest request = new MoveRequest(move);
        return post("/runs/" + runId + "/round", request, MoveResponse.class);
    }

    public MoveResponse selectBuff(Long runId, Long buffId) {
        return post("/runs/" + runId + "/buff?buffId=" + buffId, null, MoveResponse.class);
    }

    public void abandonRun(Long runId) {
        post("/runs/" + runId + "/abandon", null, Void.class);
    }

    public StatsDTO getStats() {
        return get("/stats", StatsDTO.class);
    }

    public List<AchievementDTO> getAchievements() {
        return getList("/achievements", new TypeReference<List<AchievementDTO>>() {});
    }

    public Map<String, String> getSettings() {
        return getList("/settings", new TypeReference<Map<String, String>>() {});
    }

    public void updateSettings(Map<String, String> settings) {
        put("/settings", settings);
    }

    private String get(String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .timeout(TIMEOUT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }
            throw new RuntimeException("GET " + path + " failed: " + response.statusCode());
        } catch (Exception e) {
            throw new RuntimeException("Backend request failed: " + e.getMessage(), e);
        }
    }

    private <T> T get(String path, Class<T> type) {
        String json = get(path);
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response: " + e.getMessage(), e);
        }
    }

    private <T> T post(String path, Object body, Class<T> type) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .timeout(TIMEOUT)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json");

            if (body != null) {
                String jsonBody = objectMapper.writeValueAsString(body);
                builder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
            } else {
                builder.POST(HttpRequest.BodyPublishers.noBody());
            }

            HttpResponse<String> response = httpClient.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                if (type != null && !response.body().isEmpty()) {
                    return objectMapper.readValue(response.body(), type);
                }
                return null;
            }
            throw new RuntimeException("POST " + path + " failed: " + response.statusCode());
        } catch (Exception e) {
            throw new RuntimeException("Backend request failed: " + e.getMessage(), e);
        }
    }

    private void put(String path, Object body) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("PUT " + path + " failed: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Backend request failed: " + e.getMessage(), e);
        }
    }
}
