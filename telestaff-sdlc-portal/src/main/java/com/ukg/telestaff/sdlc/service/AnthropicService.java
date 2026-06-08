package com.ukg.telestaff.sdlc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Consumer;

@Service
public class AnthropicService {

    private static final Logger log = LoggerFactory.getLogger(AnthropicService.class);
    private static final String API_URL = "https://api.anthropic.com/v1/messages";

    @Value("${anthropic.api-key:}")
    private String apiKey;

    @Value("${anthropic.model:claude-opus-4-5}")
    private String model;

    @Value("${anthropic.max-tokens:4096}")
    private int maxTokens;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AnthropicService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Streams the Claude response, calling onChunk for each text delta,
     * onComplete when done, onError on failure.
     * Blocking — intended to be called from an @Async thread.
     */
    public void streamBlocking(String systemPrompt, String userPrompt,
                               Consumer<String> onChunk,
                               Runnable onComplete,
                               Consumer<Throwable> onError) {
        if (apiKey == null || apiKey.isBlank()) {
            simulateStream(systemPrompt, userPrompt, onChunk, onComplete);
            return;
        }

        try {
            String requestBody = buildRequestBody(systemPrompt, userPrompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("Accept", "text/event-stream")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofMinutes(5))
                    .build();

            HttpResponse<java.io.InputStream> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                String body = new String(response.body().readAllBytes());
                throw new RuntimeException("Anthropic API error " + response.statusCode() + ": " + body);
            }

            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(response.body()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();
                        if (data.equals("[DONE]") || data.isEmpty()) continue;

                        String chunk = parseTextDelta(data);
                        if (chunk != null && !chunk.isEmpty()) {
                            onChunk.accept(chunk);
                        }

                        if (isMessageStop(data)) break;
                    }
                }
            }

            onComplete.run();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            onError.accept(e);
        } catch (Exception e) {
            log.error("Anthropic streaming error", e);
            onError.accept(e);
        }
    }

    private String buildRequestBody(String systemPrompt, String userPrompt) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("max_tokens", maxTokens);
        body.put("stream", true);

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            body.put("system", systemPrompt);
        }

        ArrayNode messages = body.putArray("messages");
        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);

        return objectMapper.writeValueAsString(body);
    }

    private String parseTextDelta(String jsonData) {
        try {
            JsonNode node = objectMapper.readTree(jsonData);
            if ("content_block_delta".equals(node.path("type").asText())) {
                JsonNode delta = node.path("delta");
                if ("text_delta".equals(delta.path("type").asText())) {
                    return delta.path("text").asText();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isMessageStop(String jsonData) {
        try {
            JsonNode node = objectMapper.readTree(jsonData);
            return "message_stop".equals(node.path("type").asText());
        } catch (Exception ignored) {
            return false;
        }
    }

    private void simulateStream(String systemPrompt, String userPrompt,
                                Consumer<String> onChunk, Runnable onComplete) {
        log.warn("ANTHROPIC_API_KEY not set — using demo simulation");
        String[] parts = buildDemoResponse().split("(?<=\\s)");
        for (String part : parts) {
            onChunk.accept(part);
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        onComplete.run();
    }

    private String buildDemoResponse() {
        return "# Demo Response — Telestaff SDLC Portal\n\n"
                + "> **Note:** Set `ANTHROPIC_API_KEY` to enable real Claude AI responses.\n\n"
                + "## How to Enable Real AI\n\n"
                + "```bash\n"
                + "export ANTHROPIC_API_KEY=sk-ant-...\n"
                + "mvn spring-boot:run -s local-settings.xml\n"
                + "```\n\n"
                + "## What This Agent Would Generate\n\n"
                + "With a real API key, this agent produces enterprise-quality, Telestaff-aware output including:\n\n"
                + "| Output Type | Description |\n"
                + "|-------------|-------------|\n"
                + "| Architecture Docs | HLD/LLD with class names, sequence flows, API contracts |\n"
                + "| Test Code | Runnable JUnit 4/5 tests following Telestaff conventions |\n"
                + "| Release Plans | Step-by-step deployment with rollback procedures |\n"
                + "| Issue Analysis | Root cause classification, investigation checklists |\n\n"
                + "## Portal Configuration\n\n"
                + "```yaml\n"
                + "anthropic:\n"
                + "  model: " + model + "\n"
                + "  max-tokens: " + maxTokens + "\n"
                + "  api-key: <NOT SET>\n"
                + "```\n\n"
                + "---\n*Telestaff SDLC Agents Portal — Powered by Anthropic Claude*";
    }
}
