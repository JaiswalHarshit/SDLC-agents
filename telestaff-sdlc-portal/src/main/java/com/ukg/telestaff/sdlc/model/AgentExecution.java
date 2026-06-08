package com.ukg.telestaff.sdlc.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

@Data
@Builder
public class AgentExecution {

    /** Sentinel placed on the queue when streaming is fully complete. */
    public static final String DONE_SENTINEL = "__STREAM_DONE__";

    private String executionId;
    private String agentId;
    private String agentName;
    private Map<String, String> inputs;

    @Builder.Default
    private volatile ExecutionStatus status = ExecutionStatus.PENDING;

    @Builder.Default
    private Instant startedAt = Instant.now();

    private volatile Instant completedAt;

    /** All chunks accumulated so far — for clients that connect after streaming starts. */
    @Builder.Default
    private final List<String> chunks = Collections.synchronizedList(new ArrayList<>());

    /**
     * Queue used to pass new chunks to the SSE subscriber.
     * Bounded to prevent unbounded memory growth if nobody consumes.
     */
    @Builder.Default
    private final LinkedBlockingQueue<String> chunkQueue = new LinkedBlockingQueue<>(8192);

    private volatile String errorMessage;

    public void appendChunk(String chunk) {
        chunks.add(chunk);
        chunkQueue.offer(chunk);
    }

    public void complete() {
        this.status = ExecutionStatus.COMPLETED;
        this.completedAt = Instant.now();
        chunkQueue.offer(DONE_SENTINEL);
    }

    public void fail(String errorMessage) {
        this.status = ExecutionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = Instant.now();
        chunkQueue.offer("\n\n**Error:** " + errorMessage);
        chunkQueue.offer(DONE_SENTINEL);
    }

    public String getFullResponse() {
        return String.join("", chunks);
    }

    public long getDurationSeconds() {
        if (completedAt == null) return 0;
        return completedAt.getEpochSecond() - startedAt.getEpochSecond();
    }
}
