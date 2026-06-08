package com.ukg.telestaff.sdlc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Runs Claude CLI commands and streams output back chunk-by-chunk via the SSE pipeline.
 *
 * <p>Two invocation modes:
 * <ul>
 *   <li>{@link #runAgent} — runs {@code claude --agent <agentId> ...} for named Claude Code
 *       sub-agents (e.g. {@code ce-issue-analyser}) that have MCP tool access.</li>
 *   <li>{@link #runDirectPrompt} — runs {@code claude --print ...} with a full prompt and
 *       no {@code --agent} flag, for agents that use Claude's native reasoning directly.</li>
 * </ul>
 *
 * <p>Event parsing strategy ({@code --output-format stream-json --verbose}):
 * <ul>
 *   <li>{@code stream_event / content_block_delta / text_delta} → real-time text chunk</li>
 *   <li>{@code stream_event / content_block_start / tool_use}   → tool-call status line</li>
 *   <li>{@code tool_use}  (top-level)                           → tool-call status line</li>
 *   <li>{@code system / init}                                   → startup header</li>
 *   <li>{@code result}                                          → stream complete</li>
 * </ul>
 */
@Service
public class ClaudeCliService {

    private static final Logger log = LoggerFactory.getLogger(ClaudeCliService.class);

    @Value("${claude.cli.path:/Users/harshit.jaiswal/.nvm/versions/node/v24.14.0/bin/claude}")
    private String claudeCliPath;

    @Value("${claude.cli.working-dir:/Users/harshit.jaiswal/Codebase/IJ26/workforce-telestaff}")
    private String workingDir;

    private final ObjectMapper objectMapper;

    /** Human-readable labels for MCP tools and built-in tools shown during streaming. */
    private static final Map<String, String> TOOL_LABELS = Map.ofEntries(
        Map.entry("mcp__jira__jira_get_issue",           "🎫 Fetching Jira ticket"),
        Map.entry("mcp__jira__jira_search",              "🔍 Searching Jira issues"),
        Map.entry("mcp__jira__jira_add_comment",         "💬 Posting comment to Jira"),
        Map.entry("mcp__github__search_pull_requests",   "🔀 Searching GitHub pull requests"),
        Map.entry("mcp__github__get_file_contents",      "📄 Reading source file"),
        Map.entry("mcp__github__search_code",            "🔎 Searching codebase"),
        Map.entry("mcp__github__pull_request_read",      "📋 Reading pull request"),
        Map.entry("mcp__github__list_commits",           "📜 Listing commits"),
        Map.entry("mcp__confluence__confluence_search",  "📚 Searching Confluence"),
        Map.entry("mcp__confluence__confluence_get_page","📖 Reading Confluence page"),
        Map.entry("mcp__seeker__ask_seeker",             "🧠 Querying Seeker (code intelligence)"),
        Map.entry("Bash",                                "💻 Running shell command"),
        Map.entry("Read",                                "📂 Reading file"),
        Map.entry("Grep",                                "🔍 Searching code"),
        Map.entry("Agent",                               "🤖 Spawning sub-agent"),
        Map.entry("WebSearch",                           "🌐 Searching the web"),
        Map.entry("WebFetch",                            "🌐 Fetching web page")
    );

    public ClaudeCliService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Runs a named Claude Code sub-agent via {@code --agent <agentId>}.
     * Use for agents defined in {@code .claude/agents/} that need MCP tool access.
     *
     * @param cliAgentId  agent name registered in Claude Code (e.g. "ce-issue-analyser")
     * @param userPrompt  the prompt / Jira ID + context to send
     * @param onChunk     called for each streamed text chunk
     * @param onComplete  called when the agent finishes successfully
     * @param onError     called on process or parse failure
     */
    public void runAgent(String cliAgentId,
                         String userPrompt,
                         Consumer<String> onChunk,
                         Runnable onComplete,
                         Consumer<Throwable> onError) {

        List<String> command = new ArrayList<>();
        command.add(claudeCliPath);
        command.add("--print");
        command.add("--output-format");  command.add("stream-json");
        command.add("--verbose");
        command.add("--dangerously-skip-permissions");
        command.add("--include-partial-messages");
        command.add("--agent");          command.add(cliAgentId);
        command.add(userPrompt);

        log.info("Launching Claude CLI (named agent): {} --agent {}", claudeCliPath, cliAgentId);
        runProcess(command, cliAgentId, onChunk, onComplete, onError);
    }

    /**
     * Runs Claude CLI with a full prompt but no {@code --agent} flag.
     * Use for agents that operate via direct Claude reasoning (no named sub-agent).
     * Supports slash-command skills (e.g. prompts starting with {@code /ticket-quality}).
     *
     * @param label       display label shown in the startup header (e.g. "Code Review Agent")
     * @param fullPrompt  the complete prompt (may start with a slash command like /ticket-quality)
     * @param onChunk     called for each streamed text chunk
     * @param onComplete  called when execution finishes successfully
     * @param onError     called on process or parse failure
     */
    public void runDirectPrompt(String label,
                                String fullPrompt,
                                Consumer<String> onChunk,
                                Runnable onComplete,
                                Consumer<Throwable> onError) {

        List<String> command = new ArrayList<>();
        command.add(claudeCliPath);
        command.add("--print");
        command.add("--output-format");  command.add("stream-json");
        command.add("--verbose");
        command.add("--dangerously-skip-permissions");
        command.add("--include-partial-messages");
        command.add(fullPrompt);

        log.info("Launching Claude CLI (direct prompt): {} label={}", claudeCliPath, label);
        runProcess(command, label, onChunk, onComplete, onError);
    }

    // ── Shared subprocess execution ───────────────────────────────────────────

    /**
     * Builds and runs the Claude CLI process, drains stderr, parses stdout stream-json,
     * and routes text chunks / tool-call lines to {@code onChunk}.
     * Blocks until the process exits; always call from an {@code @Async} thread.
     */
    private void runProcess(List<String> command,
                            String label,
                            Consumer<String> onChunk,
                            Runnable onComplete,
                            Consumer<Throwable> onError) {

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(workingDir));
        // Inherit the full shell environment so MCP servers, API keys, and nvm PATH
        // are all available inside the subprocess.
        pb.redirectInput(ProcessBuilder.Redirect.PIPE);
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        pb.redirectError(ProcessBuilder.Redirect.PIPE);

        try {
            Process process = pb.start();

            // Drain stderr on a daemon thread to prevent blocking
            Thread stderrDrain = new Thread(() -> {
                try (BufferedReader err = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = err.readLine()) != null) {
                        log.debug("[claude-cli stderr][{}] {}", label, line);
                    }
                } catch (IOException ignored) {}
            }, "claude-cli-stderr-" + label);
            stderrDrain.setDaemon(true);
            stderrDrain.start();

            // Parse stdout stream-json events line by line
            try (BufferedReader stdout = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = stdout.readLine()) != null) {
                    if (!line.isBlank()) {
                        parseAndForward(line, label, onChunk);
                    }
                }
            }

            int exitCode = process.waitFor();
            log.info("Claude CLI exited: label={} code={}", label, exitCode);

            if (exitCode == 0) {
                onComplete.run();
            } else {
                onError.accept(new RuntimeException(
                    "claude CLI exited with code " + exitCode + " for [" + label + "]. Check logs."));
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            onError.accept(e);
        } catch (Exception e) {
            log.error("Claude CLI execution error for [{}]", label, e);
            onError.accept(e);
        }
    }

    // ── Event parsing ────────────────────────────────────────────────────────

    private void parseAndForward(String jsonLine, String label, Consumer<String> onChunk) {
        try {
            JsonNode root      = objectMapper.readTree(jsonLine);
            String   type      = root.path("type").asText("");

            switch (type) {

                case "stream_event": {
                    JsonNode event     = root.path("event");
                    String   eventType = event.path("type").asText("");

                    if ("content_block_delta".equals(eventType)) {
                        // Real-time text streaming
                        JsonNode delta = event.path("delta");
                        if ("text_delta".equals(delta.path("type").asText())) {
                            String text = delta.path("text").asText();
                            if (!text.isEmpty()) onChunk.accept(text);
                        }

                    } else if ("content_block_start".equals(eventType)) {
                        // Tool call starting
                        JsonNode cb = event.path("content_block");
                        if ("tool_use".equals(cb.path("type").asText())) {
                            String toolName = cb.path("name").asText();
                            onChunk.accept(toolStatusLine(toolName));
                        }
                    }
                    break;
                }

                case "tool_use": {
                    // Top-level tool_use event (emitted by some Claude Code versions)
                    String toolName = root.path("name").asText();
                    onChunk.accept(toolStatusLine(toolName));
                    break;
                }

                case "system": {
                    // Emit a one-time header when Claude starts — use the agent label
                    if ("init".equals(root.path("subtype").asText())) {
                        String model = root.path("model").asText("claude");
                        onChunk.accept("> 🚀 **" + label + "** started · model: `" + model + "`\n>\n> ");
                    }
                    break;
                }

                case "result":
                case "rate_limit_event":
                case "assistant":  // complete assembled message — already streamed via stream_event
                default:
                    break;
            }

        } catch (Exception e) {
            // Silently skip unparseable lines (blank lines, debug output, etc.)
            log.trace("Skipping unparseable CLI line [{}]: {}",
                label, jsonLine.length() > 80 ? jsonLine.substring(0, 80) + "…" : jsonLine);
        }
    }

    private String toolStatusLine(String toolName) {
        String label = TOOL_LABELS.getOrDefault(toolName, "⚙️ " + toolName);
        return "\n\n`" + label + "`  \n";
    }
}
