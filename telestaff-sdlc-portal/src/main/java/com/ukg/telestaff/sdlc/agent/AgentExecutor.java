package com.ukg.telestaff.sdlc.agent;

import com.ukg.telestaff.sdlc.config.DemoModeProperties;
import com.ukg.telestaff.sdlc.model.AgentDefinition;
import com.ukg.telestaff.sdlc.model.AgentExecution;
import com.ukg.telestaff.sdlc.model.ExecutionStatus;
import com.ukg.telestaff.sdlc.service.AnthropicService;
import com.ukg.telestaff.sdlc.service.ClaudeCliService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Dispatches agent executions to the appropriate back-end:
 *
 * <ol>
 *   <li><b>CLI_AGENT_MAP</b> — Claude Code CLI with {@code --agent <name>} for named sub-agents
 *       that have Jira / GitHub / Confluence MCP tool access
 *       (currently: {@code customer-issue-analyzer → ce-issue-analyser}).</li>
 *   <li><b>DIRECT_CLI_AGENTS</b> — Claude Code CLI without {@code --agent}, executing a full
 *       crafted prompt directly. Used for agents that benefit from Claude's extended context
 *       window, slash-command skills (e.g. {@code /ticket-quality}), and local file reads
 *       without needing a named sub-agent definition.</li>
 *   <li>All other agents → Anthropic REST API with a curated system prompt.</li>
 * </ol>
 */
@Component
public class AgentExecutor {

    private static final Logger log = LoggerFactory.getLogger(AgentExecutor.class);

    /**
     * Web-portal agentId → Claude Code CLI sub-agent name.
     * Each entry invokes {@code claude --agent <value>} with a tailored prompt.
     * The named agent reads its own .md definition and has access to MCP tools
     * (Jira, GitHub, Confluence, Seeker) configured in the workforce-telestaff repo.
     */
    private static final Map<String, String> CLI_AGENT_MAP = Map.ofEntries(
        // Issue investigation — ce-issue-analyser sub-agent
        Map.entry("customer-issue-analyzer", "ce-issue-analyser"),

        // Feature design / story work — feature-designer sub-agent
        Map.entry("feature-design-hld",  "feature-designer"),
        Map.entry("feature-design-lld",  "feature-designer"),
        Map.entry("design-review",       "feature-designer"),
        Map.entry("story-breakdown",     "feature-designer"),
        Map.entry("defect-fix-designer", "feature-designer"),

        // Test planning — test-plan-writer sub-agent
        Map.entry("test-plan-generator", "test-plan-writer")
    );

    /**
     * Agents that run via {@code claude --print <prompt>} directly (no {@code --agent} flag).
     * They use Claude's built-in reasoning + optional slash-command skills.
     */
    private static final Set<String> DIRECT_CLI_AGENTS = Set.of(
        "code-review",
        "release-notes",
        "sprint-readiness",
        "production-incident",
        "create-open-prs"
    );

    private final AnthropicService    anthropicService;
    private final ClaudeCliService    claudeCliService;
    private final AgentPromptBuilder  promptBuilder;
    private final DemoModeProperties  demoModeProperties;
    private final DemoStreamingService demoStreamingService;

    public AgentExecutor(AnthropicService anthropicService,
                         ClaudeCliService claudeCliService,
                         AgentPromptBuilder promptBuilder,
                         DemoModeProperties demoModeProperties,
                         DemoStreamingService demoStreamingService) {
        this.anthropicService     = anthropicService;
        this.claudeCliService     = claudeCliService;
        this.promptBuilder        = promptBuilder;
        this.demoModeProperties   = demoModeProperties;
        this.demoStreamingService = demoStreamingService;
    }

    @Async
    public void execute(AgentDefinition agent, AgentExecution execution,
                        Map<String, String> inputs) {

        execution.setStatus(ExecutionStatus.RUNNING);
        log.info("Executing agent={} executionId={}", agent.getId(), execution.getExecutionId());

        // ── Demo mode short-circuit ──────────────────────────────────────────
        if (demoModeProperties.isDemoMode()) {
            log.info("[DEMO] Routing agent={} to mock streaming (no CLI/API call)",
                    agent.getId());
            demoStreamingService.stream(
                agent.getId(),
                inputs,
                execution::appendChunk,
                () -> {
                    execution.complete();
                    log.info("[DEMO] Mock execution {} complete ({}s)",
                            execution.getExecutionId(), execution.getDurationSeconds());
                },
                error -> {
                    log.error("[DEMO] Mock execution {} failed", execution.getExecutionId(), error);
                    execution.fail(error.getMessage());
                }
            );
            return;
        }
        // ── End demo mode ────────────────────────────────────────────────────

        String cliAgentName = CLI_AGENT_MAP.get(agent.getId());

        if (cliAgentName != null) {
            // Named sub-agent path (e.g. ce-issue-analyser with MCP tools)
            runViaCli(agent, execution, inputs, cliAgentName);

        } else if (DIRECT_CLI_AGENTS.contains(agent.getId())) {
            // Direct Claude CLI path (rich prompt, optional skill invocation)
            runViaDirectCli(agent, execution, inputs);

        } else {
            // Anthropic REST API path for all other agents
            runViaApi(agent, execution, inputs);
        }
    }

    // ── Named CLI agent path ─────────────────────────────────────────────────

    private void runViaCli(AgentDefinition agent, AgentExecution execution,
                           Map<String, String> inputs, String cliAgentName) {

        String userPrompt = promptBuilder.buildCliPrompt(agent, inputs);

        claudeCliService.runAgent(
            cliAgentName,
            userPrompt,
            execution::appendChunk,
            () -> {
                execution.complete();
                log.info("CLI agent execution {} complete ({}s)",
                    execution.getExecutionId(), execution.getDurationSeconds());
            },
            error -> {
                log.error("CLI agent execution {} failed", execution.getExecutionId(), error);
                execution.fail(error.getMessage());
            }
        );
    }

    // ── Direct CLI prompt path ───────────────────────────────────────────────

    private void runViaDirectCli(AgentDefinition agent, AgentExecution execution,
                                 Map<String, String> inputs) {

        String fullPrompt = promptBuilder.buildDirectCliPrompt(agent, inputs);
        String label      = agent.getName();

        claudeCliService.runDirectPrompt(
            label,
            fullPrompt,
            execution::appendChunk,
            () -> {
                execution.complete();
                log.info("Direct CLI execution {} complete ({}s)",
                    execution.getExecutionId(), execution.getDurationSeconds());
            },
            error -> {
                log.error("Direct CLI execution {} failed", execution.getExecutionId(), error);
                execution.fail(error.getMessage());
            }
        );
    }

    // ── Anthropic REST API path ──────────────────────────────────────────────

    private void runViaApi(AgentDefinition agent, AgentExecution execution,
                           Map<String, String> inputs) {

        String systemPrompt = promptBuilder.buildSystemPrompt(agent);
        String userPrompt   = promptBuilder.buildUserPrompt(agent, inputs);

        anthropicService.streamBlocking(
            systemPrompt,
            userPrompt,
            execution::appendChunk,
            () -> {
                execution.complete();
                log.info("API execution {} complete ({}s)",
                    execution.getExecutionId(), execution.getDurationSeconds());
            },
            error -> {
                log.error("API execution {} failed", execution.getExecutionId(), error);
                execution.fail(error.getMessage());
            }
        );
    }
}
