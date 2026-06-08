package com.ukg.telestaff.sdlc.controller;

import com.ukg.telestaff.sdlc.model.AgentExecution;
import com.ukg.telestaff.sdlc.model.ExecutionStatus;
import com.ukg.telestaff.sdlc.service.ExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/executions")
public class ExecutionController {

    private static final Logger log = LoggerFactory.getLogger(ExecutionController.class);

    private final ExecutionService executionService;
    private final ExecutorService sseThreadPool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "sse-worker");
        t.setDaemon(true);
        return t;
    });

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @GetMapping("/{executionId}")
    public String executionPage(@PathVariable String executionId, Model model) {
        AgentExecution execution = executionService.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execution not found: " + executionId));
        model.addAttribute("execution", execution);
        return "execution";
    }

    /**
     * Agents that run via the Claude CLI (named sub-agent or direct prompt) can take
     * significantly longer than the Anthropic REST API path — allow 10 minutes.
     */
    private static final Set<String> CLI_AGENT_IDS = Set.of(
        // ce-issue-analyser sub-agent
        "customer-issue-analyzer",
        // feature-designer sub-agent
        "feature-design-hld",
        "feature-design-lld",
        "design-review",
        "story-breakdown",
        "defect-fix-designer",
        // test-plan-writer sub-agent
        "test-plan-generator",
        // direct CLI agents (no --agent flag)
        "code-review",
        "release-notes",
        "sprint-readiness",
        "production-incident",
        "create-open-prs"
    );

    @GetMapping(value = "/{executionId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String executionId) {
        AgentExecution execution = executionService.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execution not found: " + executionId));

        long timeoutMs = CLI_AGENT_IDS.contains(execution.getAgentId())
            ? 600_000L   // 10 minutes for CLI-backed agents
            : 300_000L;  // 5 minutes for Anthropic REST API agents
        SseEmitter emitter = new SseEmitter(timeoutMs);

        sseThreadPool.submit(() -> {
            try {
                // First: replay any chunks already accumulated before this SSE connection opened
                int replayed = 0;
                for (String chunk : execution.getChunks()) {
                    sendChunk(emitter, chunk);
                    replayed++;
                }

                // If already done, send final event and return
                if (execution.getStatus() == ExecutionStatus.COMPLETED
                        || execution.getStatus() == ExecutionStatus.FAILED) {
                    sendDone(emitter, execution.getStatus());
                    emitter.complete();
                    return;
                }

                // Then: drain the live queue until DONE_SENTINEL arrives
                while (true) {
                    String chunk = execution.getChunkQueue().poll(30, TimeUnit.SECONDS);
                    if (chunk == null) {
                        // Timeout — check if execution is done
                        if (execution.getStatus() == ExecutionStatus.COMPLETED
                                || execution.getStatus() == ExecutionStatus.FAILED) {
                            sendDone(emitter, execution.getStatus());
                            emitter.complete();
                            break;
                        }
                        // Send keep-alive comment
                        emitter.send(SseEmitter.event().comment("keep-alive"));
                        continue;
                    }

                    if (AgentExecution.DONE_SENTINEL.equals(chunk)) {
                        sendDone(emitter, execution.getStatus());
                        emitter.complete();
                        break;
                    }

                    // Skip chunks already replayed
                    if (replayed > 0) {
                        replayed--;
                        continue;
                    }

                    sendChunk(emitter, chunk);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                emitter.completeWithError(e);
            } catch (IOException e) {
                log.debug("SSE client disconnected for execution {}", executionId);
                emitter.completeWithError(e);
            } catch (Exception e) {
                log.error("SSE error for execution {}", executionId, e);
                emitter.completeWithError(e);
            }
        });

        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> log.debug("SSE emitter error", e));
        return emitter;
    }

    private void sendChunk(SseEmitter emitter, String chunk) throws IOException {
        emitter.send(SseEmitter.event().name("chunk").data(chunk));
    }

    private void sendDone(SseEmitter emitter, ExecutionStatus status) throws IOException {
        String data = status == ExecutionStatus.FAILED ? "failed" : "done";
        emitter.send(SseEmitter.event().name("done").data(data));
    }
}
