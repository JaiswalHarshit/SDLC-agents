package com.ukg.telestaff.sdlc.agent;

import com.ukg.telestaff.sdlc.config.DemoModeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Streams a pre-canned demo response through three distinct phases that mimic
 * the feel of a real agent execution:
 *
 * <ol>
 *   <li><b>Planning phase</b> — {@code demoInitialDelayMs} of silence before the
 *       first character appears, simulating the agent reading context and planning
 *       its response.</li>
 *   <li><b>Early output phase</b> — the first {@code demoPauseMidAfterPercent}% of
 *       lines stream at {@code demoStreamingDelayMs} per line (header, classification,
 *       summary sections).</li>
 *   <li><b>Mid-generation pause</b> — {@code demoPauseMidDelayMs} of silence,
 *       simulating the agent pausing to "think" before generating the detailed body.</li>
 *   <li><b>Main output phase</b> — remaining lines stream at the same per-line delay.</li>
 * </ol>
 *
 * <p>The SSE infrastructure in
 * {@link com.ukg.telestaff.sdlc.controller.ExecutionController} is completely
 * unaware of demo mode — it consumes chunks from the execution queue exactly as
 * it would for a real run.
 */
@Service
public class DemoStreamingService {

    private static final Logger log = LoggerFactory.getLogger(DemoStreamingService.class);

    private final DemoResponseRegistry demoResponseRegistry;
    private final DemoModeProperties   props;

    public DemoStreamingService(DemoResponseRegistry demoResponseRegistry,
                                DemoModeProperties props) {
        this.demoResponseRegistry = demoResponseRegistry;
        this.props                = props;
    }

    /**
     * Streams the canned response for {@code agentId} asynchronously with
     * phase-aware delays.
     *
     * @param agentId    agent being simulated
     * @param inputs     user form inputs (used to personalise response text)
     * @param onChunk    called once per line with the chunk text (including newline)
     * @param onComplete called when all lines have been emitted
     * @param onError    called if the thread is interrupted or an exception is thrown
     */
    @Async
    public void stream(String agentId,
                       Map<String, String> inputs,
                       Consumer<String> onChunk,
                       Runnable onComplete,
                       Consumer<Throwable> onError) {
        try {
            log.info("[DEMO] Starting mock stream — agentId={}", agentId);

            String   response       = demoResponseRegistry.getResponse(agentId, inputs);
            String[] lines          = response.split("\n", -1);
            int      totalLines     = lines.length;
            int      midPauseLine   = Math.max(1,
                                        (int) Math.ceil(totalLines * props.getDemoPauseMidAfterPercent() / 100.0));
            boolean  midPauseFired  = false;

            // ── Phase 1: planning silence ────────────────────────────────────
            log.debug("[DEMO] Planning phase — {}ms", props.getDemoInitialDelayMs());
            sleep(props.getDemoInitialDelayMs());

            // ── Phases 2 & 4: streaming with mid-pause ───────────────────────
            for (int i = 0; i < totalLines; i++) {

                // Insert mid-generation pause once, after N% of lines
                if (!midPauseFired && i >= midPauseLine) {
                    log.debug("[DEMO] Mid-generation pause — {}ms (after line {}/{})",
                            props.getDemoPauseMidDelayMs(), i, totalLines);
                    sleep(props.getDemoPauseMidDelayMs());
                    midPauseFired = true;
                }

                onChunk.accept(lines[i] + "\n");
                sleep(props.getDemoStreamingDelayMs());
            }

            onComplete.run();
            log.info("[DEMO] Mock stream complete — agentId={}", agentId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            onError.accept(e);
        } catch (Exception e) {
            log.error("[DEMO] Unexpected error in mock stream — agentId={}", agentId, e);
            onError.accept(e);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static void sleep(int ms) throws InterruptedException {
        if (ms > 0) {
            Thread.sleep(ms);
        }
    }
}
