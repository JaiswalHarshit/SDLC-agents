package com.ukg.telestaff.sdlc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binding for the {@code app.*} configuration namespace.
 *
 * <p>Enable demo mode at startup without editing any file:
 * <pre>
 *   mvn spring-boot:run -Dspring-boot.run.arguments="--app.demo-mode=true"
 *   # or via env var:
 *   APP_DEMO_MODE=true mvn spring-boot:run
 * </pre>
 *
 * <h3>Demo streaming phases</h3>
 * <pre>
 *  [submit]──(initialDelayMs)──▶ first line streams
 *                                  ...30% of lines at streamingDelayMs each...
 *                               ──(pauseMidDelayMs)──▶ rest of lines stream
 *                                  ...remaining lines at streamingDelayMs each...
 *                               ──▶ COMPLETE
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "app")
public class DemoModeProperties {

    /**
     * When {@code true}, all agent executions are short-circuited to pre-canned
     * responses from {@link com.ukg.telestaff.sdlc.agent.DemoResponseRegistry}.
     * No Claude CLI or Anthropic API calls are made.
     */
    private boolean demoMode = false;

    /**
     * Milliseconds to sleep between each streamed line.
     * 55 ms × 60 lines ≈ 3.3 s of active streaming.
     */
    private int demoStreamingDelayMs = 55;

    /**
     * Milliseconds to pause <em>before</em> the first line is emitted.
     * Keeps the execution in RUNNING state with no output visible,
     * simulating the agent "planning" before it starts writing.
     */
    private int demoInitialDelayMs = 1800;

    /**
     * Milliseconds to pause mid-stream (after {@link #demoPauseMidAfterPercent}%
     * of lines), simulating the agent pausing to "think" before generating
     * the main body of the response.
     */
    private int demoPauseMidDelayMs = 1200;

    /**
     * Percentage of total lines after which the mid-stream pause is inserted.
     * Default 28 — pause fires roughly after the header / classification section,
     * before the detailed findings start appearing.
     */
    private int demoPauseMidAfterPercent = 28;

    // ── getters / setters ────────────────────────────────────────────────────

    public boolean isDemoMode() { return demoMode; }
    public void setDemoMode(boolean demoMode) { this.demoMode = demoMode; }

    public int getDemoStreamingDelayMs() { return demoStreamingDelayMs; }
    public void setDemoStreamingDelayMs(int v) { this.demoStreamingDelayMs = v; }

    public int getDemoInitialDelayMs() { return demoInitialDelayMs; }
    public void setDemoInitialDelayMs(int v) { this.demoInitialDelayMs = v; }

    public int getDemoPauseMidDelayMs() { return demoPauseMidDelayMs; }
    public void setDemoPauseMidDelayMs(int v) { this.demoPauseMidDelayMs = v; }

    public int getDemoPauseMidAfterPercent() { return demoPauseMidAfterPercent; }
    public void setDemoPauseMidAfterPercent(int v) { this.demoPauseMidAfterPercent = v; }
}
