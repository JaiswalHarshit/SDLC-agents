package com.ukg.telestaff.sdlc.agent;

import com.ukg.telestaff.sdlc.model.AgentDefinition;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AgentPromptBuilder {

    private static final String SYSTEM_CTX =
        "You are an expert AI agent embedded in the Telestaff SDLC Portal, an enterprise AI platform " +
        "for UKG Telestaff engineering teams. Telestaff is an enterprise workforce management system " +
        "(scheduling, rostering, staffing) built with Java 11, Spring 5.3, Hibernate 5.4, PostgreSQL 17, " +
        "and deployed on GCP (GKE + Cloud SQL).\n\n" +
        "Tech stack:\n" +
        "- Backend: Java 11, Spring Framework 5.3 (XML config, NOT Spring Boot), Spring Security 5.8\n" +
        "- ORM: Hibernate 5.4, JPA, multi-tenant (EDAP tenant + institution scope)\n" +
        "- DB: PostgreSQL 17 via Cloud SQL, Liquibase migrations, no MS SQL\n" +
        "- Caching: Two-tier EHCache + Redis (TenantAwareCompositeCacheManager)\n" +
        "- Messaging: ActiveMQ JMS, GCP Pub/Sub, Spring Integration\n" +
        "- Feature flags: LaunchDarkly (every code change MUST be wrapped in a feature flag)\n" +
        "- Frontend: JSP + Apache Tiles (primary), Angular 17 (partial)\n" +
        "- Testing: JUnit 4/5 + Mockito, BaseServiceIT for integration tests\n" +
        "- Modules: telestaff-web -> telestaff-services -> telestaff-domain -> telestaff-core -> telestaff-commons\n\n" +
        "Coding conventions:\n" +
        "- Entity classes: *Tbl suffix (e.g., LocationTbl), JPA annotations on getters NOT fields\n" +
        "- Services: interface (*Service) + impl (*ServiceImpl), @Transactional only on impl mutations\n" +
        "- @PreAuthorize on service interfaces only\n" +
        "- Cache eviction via @XxxEvict annotations, never direct cache manager calls\n" +
        "- All new features MUST check LaunchDarkly feature flag\n\n" +
        "Produce structured, professional output in Markdown format. Be specific and actionable, " +
        "referencing actual Telestaff class names, patterns, and conventions where relevant.";

    // =========================================================================
    // CLI-backed named agent prompts  (--agent <name>)
    // =========================================================================

    /**
     * Dispatches to a per-agent prompt builder for agents routed via {@code claude --agent}.
     * Each named sub-agent (ce-issue-analyser, feature-designer, test-plan-writer) already
     * carries its own comprehensive system prompt from its {@code .md} definition file, so
     * we only send a focused task brief with the relevant inputs — no system context needed.
     */
    public String buildCliPrompt(AgentDefinition agent, Map<String, String> inputs) {
        switch (agent.getId()) {
            case "customer-issue-analyzer": return buildIssueAnalyzerPrompt(inputs);
            case "feature-design-hld":      return buildHldPrompt(inputs);
            case "feature-design-lld":      return buildLldPrompt(inputs);
            case "design-review":           return buildDesignReviewPrompt(inputs);
            case "story-breakdown":         return buildStoryBreakdownPrompt(inputs);
            case "defect-fix-designer":     return buildDefectFixPrompt(inputs);
            case "test-plan-generator":     return buildTestPlanPrompt(inputs);
            default:
                // Generic fallback: Jira ID + all inputs
                return buildGenericCliPrompt(agent, inputs);
        }
    }

    // ── ce-issue-analyser ────────────────────────────────────────────────────

    private String buildIssueAnalyzerPrompt(Map<String, String> inputs) {
        String jiraId = inputs.getOrDefault("jiraId", "").trim();
        if (jiraId.isEmpty()) {
            throw new IllegalArgumentException("Jira Issue Key is required for the Customer Issue Analyzer");
        }

        StringBuilder sb = new StringBuilder(jiraId);

        String description = inputs.getOrDefault("description", "").trim();
        String steps       = inputs.getOrDefault("steps",       "").trim();
        String severity    = inputs.getOrDefault("severity",    "").trim();
        String environment = inputs.getOrDefault("environment", "").trim();

        if (!description.isEmpty() || !steps.isEmpty() || !severity.isEmpty() || !environment.isEmpty()) {
            sb.append("\n\nAdditional context provided by the engineer:");
            if (!severity.isEmpty())    sb.append("\n- Severity: ").append(severity);
            if (!description.isEmpty()) sb.append("\n- Issue description: ").append(description);
            if (!steps.isEmpty())       sb.append("\n- Steps to reproduce: ").append(steps);
            if (!environment.isEmpty()) sb.append("\n- Environment/version: ").append(environment);
        }

        return sb.toString();
    }

    // ── feature-designer: HLD ────────────────────────────────────────────────

    private String buildHldPrompt(Map<String, String> inputs) {
        String jiraId       = inputs.getOrDefault("jiraId",       "").trim();
        String requirements = inputs.getOrDefault("requirements", "").trim();
        String complexity   = inputs.getOrDefault("complexity",   "").trim();
        String constraints  = inputs.getOrDefault("constraints",  "").trim();

        StringBuilder sb = new StringBuilder();
        sb.append("Generate a **High Level Design (HLD)** document for the following Telestaff feature.\n\n");

        if (!jiraId.isEmpty())       sb.append("**Jira Epic / Story**: ").append(jiraId).append("\n");
        if (!complexity.isEmpty())   sb.append("**Complexity**: ").append(complexity).append("\n");
        sb.append("\n");

        if (!requirements.isEmpty()) {
            sb.append("## Feature Requirements\n\n").append(requirements).append("\n\n");
        }
        if (!constraints.isEmpty()) {
            sb.append("## Technical Constraints\n\n").append(constraints).append("\n\n");
        }

        sb.append("## Required HLD Sections\n\n")
          .append("1. Executive Summary\n")
          .append("2. Architecture Overview (with component diagram in ASCII or Mermaid)\n")
          .append("3. Component Breakdown (modules, responsibilities, package names)\n")
          .append("4. Data Flow\n")
          .append("5. Integration Points (APIs, JMS, Pub/Sub, SOAP, cache)\n")
          .append("6. Technology Choices with justification\n")
          .append("7. Non-Functional Requirements (performance, scalability, security)\n")
          .append("8. LaunchDarkly flag name and rollout strategy\n")
          .append("9. Open Questions / Risks\n\n")
          .append("Use Telestaff conventions: *Tbl entities, *Service/*ServiceImpl split, ")
          .append("@PreAuthorize on interfaces, @Transactional on impl mutations, ")
          .append("@XxxEvict for cache eviction, multi-tenant institution filtering.");

        return sb.toString();
    }

    // ── feature-designer: LLD ────────────────────────────────────────────────

    private String buildLldPrompt(Map<String, String> inputs) {
        String jiraId       = inputs.getOrDefault("jiraId",       "").trim();
        String hld          = inputs.getOrDefault("hld",          "").trim();
        String requirements = inputs.getOrDefault("requirements", "").trim();
        String module       = inputs.getOrDefault("module",       "").trim();

        StringBuilder sb = new StringBuilder();
        sb.append("Generate a **Low Level Design (LLD)** document for the following Telestaff feature.\n\n");

        if (!jiraId.isEmpty())  sb.append("**Jira Story**: ").append(jiraId).append("\n");
        if (!module.isEmpty())  sb.append("**Target Module**: ").append(module).append("\n");
        sb.append("\n");

        if (!hld.isEmpty()) {
            sb.append("## High Level Design Reference\n\n").append(hld).append("\n\n");
        }
        if (!requirements.isEmpty()) {
            sb.append("## Detailed Requirements\n\n").append(requirements).append("\n\n");
        }

        sb.append("## Required LLD Sections\n\n")
          .append("1. Class Design — fully qualified names, responsibilities, fields, methods\n")
          .append("2. Sequence Diagrams in Mermaid format\n")
          .append("3. API Contract — HTTP method, path, request/response DTOs with field types\n")
          .append("4. Database Schema — Liquibase changeSet XML for new tables/columns/indexes\n")
          .append("5. Service Layer — method signatures, @PreAuthorize expressions, @Transactional boundaries\n")
          .append("6. Cache Strategy — what to cache, CacheManagerCacheEnum entry, eviction triggers, TTL\n")
          .append("7. Error Handling — exception classes, HTTP status codes, error messages\n")
          .append("8. LaunchDarkly Integration — exact code location where featureFlagService.isFeatureEnabled() is called\n\n")
          .append("Follow Telestaff conventions strictly. JPA annotations on getters. ")
          .append("Entity suffix *Tbl. No @SpringBootTest. Checkstyle compliant (4-space indent, no star imports).");

        return sb.toString();
    }

    // ── feature-designer: Design Review ─────────────────────────────────────

    private String buildDesignReviewPrompt(Map<String, String> inputs) {
        String jiraId     = inputs.getOrDefault("jiraId",      "").trim();
        String design     = inputs.getOrDefault("design",      "").trim();
        String reviewType = inputs.getOrDefault("reviewType",  "Full Design Review").trim();
        String context    = inputs.getOrDefault("context",     "").trim();

        StringBuilder sb = new StringBuilder();
        sb.append("Perform a **").append(reviewType).append("** on the following Telestaff design document.\n\n");

        if (!jiraId.isEmpty())  sb.append("**Jira Reference**: ").append(jiraId).append("\n\n");
        if (!context.isEmpty()) sb.append("## Context\n\n").append(context).append("\n\n");

        if (!design.isEmpty()) {
            sb.append("## Design Document\n\n").append(design).append("\n\n");
        } else if (!jiraId.isEmpty()) {
            sb.append("## Design Source\n\n")
              .append("Fetch the design from Jira ticket **").append(jiraId)
              .append("** and its linked Confluence pages. Review the attached design document.\n\n");
        }

        sb.append("## Review Checklist\n\n")
          .append("Evaluate against each of these areas and give a verdict per section:\n\n")
          .append("1. **Overall Assessment**: PASS / NEEDS REVISION / FAIL\n")
          .append("2. **Critical Issues** — must fix before implementation\n")
          .append("3. **Major Issues** — should fix\n")
          .append("4. **Minor Issues / Suggestions**\n")
          .append("5. **Security** — multi-tenancy leaks, @PreAuthorize coverage, sensitive data exposure\n")
          .append("6. **Performance** — N+1 queries, missing indexes, unbounded cache, heavy joins\n")
          .append("7. **Multi-tenancy** — institution filter on every query, TenantContext on child threads\n")
          .append("8. **Caching** — correct eviction strategy, no Hibernate proxies in cache, TTL defined\n")
          .append("9. **Conventions** — *Tbl suffix, annotations on getters, service interface split, @Transactional placement\n")
          .append("10. **LaunchDarkly** — flag present, both states tested, removal plan noted\n")
          .append("11. **Liquibase** — backward-compatible DDL, new columns nullable, no blocking index creation\n")
          .append("12. **Actionable Recommendations** — specific class names and code changes required\n");

        return sb.toString();
    }

    // ── feature-designer: Story Breakdown ────────────────────────────────────

    private String buildStoryBreakdownPrompt(Map<String, String> inputs) {
        String jiraId          = inputs.getOrDefault("jiraId",          "").trim();
        String epicDescription = inputs.getOrDefault("epicDescription", "").trim();
        String teamSize        = inputs.getOrDefault("teamSize",        "").trim();
        String constraints     = inputs.getOrDefault("constraints",     "").trim();

        if (jiraId.isEmpty() && epicDescription.isEmpty()) {
            throw new IllegalArgumentException("Jira Epic ID or description is required for Story Breakdown");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Break down the following Telestaff epic or large story into well-defined, ")
          .append("independently deliverable sub-stories ready for sprint planning.\n\n");

        if (!jiraId.isEmpty())    sb.append("**Jira Epic / Story**: ").append(jiraId).append("\n");
        if (!teamSize.isEmpty())  sb.append("**Team Size**: ").append(teamSize).append(" engineers\n");
        sb.append("\n");

        if (!epicDescription.isEmpty()) {
            sb.append("## Epic / Story Description\n\n").append(epicDescription).append("\n\n");
        }
        if (!constraints.isEmpty()) {
            sb.append("## Technical Constraints / Dependencies\n\n").append(constraints).append("\n\n");
        }

        sb.append("## Required Output\n\n")
          .append("1. **Sub-stories list** — each with:\n")
          .append("   - Story title (Jira-ready format)\n")
          .append("   - Acceptance criteria (BDD Given/When/Then)\n")
          .append("   - Story point estimate and confidence\n")
          .append("   - Affected modules (telestaff-web, telestaff-services, etc.)\n")
          .append("   - LaunchDarkly flag needed? (Yes/No)\n")
          .append("   - DB migration needed? (Yes/No)\n")
          .append("2. **Dependency graph** — which stories must complete before others\n")
          .append("3. **Suggested sprint allocation** — based on the team size provided\n")
          .append("4. **Risk flags** — stories with unclear scope or high technical complexity\n")
          .append("5. **Definition of Done** checklist for the epic\n\n")
          .append("Target 3-8 story points per sub-story. Flag anything larger as needing further breakdown.");

        return sb.toString();
    }

    // ── feature-designer: Defect Fix Designer ────────────────────────────────

    private String buildDefectFixPrompt(Map<String, String> inputs) {
        String jiraId          = inputs.getOrDefault("jiraId",          "").trim();
        String defectDesc      = inputs.getOrDefault("defectDescription","").trim();
        String rootCause       = inputs.getOrDefault("rootCause",       "").trim();
        String affectedClasses = inputs.getOrDefault("affectedClasses", "").trim();
        String fixType         = inputs.getOrDefault("fixType",         "Code Fix").trim();

        if (jiraId.isEmpty() && defectDesc.isEmpty()) {
            throw new IllegalArgumentException("Jira Defect ID or description is required for Defect Fix Designer");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Design a targeted fix for the following Telestaff defect.\n\n");

        if (!jiraId.isEmpty())   sb.append("**Jira Defect**: ").append(jiraId).append("\n");
        if (!fixType.isEmpty())  sb.append("**Fix Type**: ").append(fixType).append("\n");
        sb.append("\n");

        if (!defectDesc.isEmpty()) {
            sb.append("## Defect Description\n\n").append(defectDesc).append("\n\n");
        }
        if (!rootCause.isEmpty()) {
            sb.append("## Known Root Cause\n\n").append(rootCause).append("\n\n");
        }
        if (!affectedClasses.isEmpty()) {
            sb.append("## Affected Classes / Areas\n\n").append(affectedClasses).append("\n\n");
        }

        sb.append("## Required Fix Design\n\n")
          .append("1. **Root Cause Confirmation** — verify or refine the root cause with code evidence\n")
          .append("2. **Fix Design** — specific code changes needed:\n")
          .append("   - Exact class(es) and method(s) to modify\n")
          .append("   - Before/after pseudocode or diff\n")
          .append("   - Any new classes, methods, or DB changes required\n")
          .append("3. **Impact Analysis** — what else could this fix affect? Regression risk areas?\n")
          .append("4. **Multi-tenancy Check** — does the fix correctly handle all tenants/institutions?\n")
          .append("5. **LaunchDarkly** — should the fix be gated behind a flag? Recommended flag key.\n")
          .append("6. **Validation Approach** — how to verify the fix works without breaking anything:\n")
          .append("   - Unit test cases to add\n")
          .append("   - Integration test scenarios\n")
          .append("   - Manual test steps\n")
          .append("7. **Rollback Plan** — if the fix causes issues in production, how to revert safely\n")
          .append("8. **DB Migration** (if applicable) — Liquibase changeSet, backward-compatible");

        return sb.toString();
    }

    // ── test-plan-writer ─────────────────────────────────────────────────────

    private String buildTestPlanPrompt(Map<String, String> inputs) {
        String jiraId             = inputs.getOrDefault("jiraId",             "").trim();
        String featureDescription = inputs.getOrDefault("featureDescription", "").trim();
        String acceptanceCriteria = inputs.getOrDefault("acceptanceCriteria", "").trim();
        String testingScope       = inputs.getOrDefault("testingScope",       "Full Coverage").trim();

        StringBuilder sb = new StringBuilder();
        sb.append("Generate a **detailed manual test plan** for the following Telestaff feature or story.\n\n");

        if (!jiraId.isEmpty())  sb.append("**Jira ID**: ").append(jiraId).append("\n");
        sb.append("**Testing Scope**: ").append(testingScope).append("\n\n");

        if (!featureDescription.isEmpty()) {
            sb.append("## Feature / Story Description\n\n").append(featureDescription).append("\n\n");
        }
        if (!acceptanceCriteria.isEmpty()) {
            sb.append("## Acceptance Criteria\n\n").append(acceptanceCriteria).append("\n\n");
        }

        sb.append("## Required Test Plan Sections\n\n")
          .append("1. **Test Plan Summary** — scope, objectives, entry/exit criteria\n")
          .append("2. **Test Environment** — required Telestaff modules, tenant setup, feature flags to enable\n")
          .append("3. **Test Cases** — for each test case provide:\n")
          .append("   - Test Case ID\n")
          .append("   - Test Scenario (what is being validated)\n")
          .append("   - Pre-conditions\n")
          .append("   - Step-by-step test steps\n")
          .append("   - Expected result\n")
          .append("   - Priority (P0/P1/P2)\n")
          .append("4. **Happy Path Scenarios** — core user flows end-to-end\n")
          .append("5. **Negative / Edge Case Scenarios** — invalid input, boundary values, empty states\n")
          .append("6. **Multi-tenancy Scenarios** — verify data isolation between institutions\n")
          .append("7. **LaunchDarkly Toggle Tests** — test with flag ON and flag OFF\n")
          .append("8. **Regression Risk Areas** — related features that could be impacted\n")
          .append("9. **Traceability Matrix** — test case ID ↔ acceptance criterion\n")
          .append("10. **Out of Scope** — what is explicitly not tested in this plan\n\n")
          .append("Format test cases as a Markdown table where possible. ")
          .append("Reference Telestaff-specific setup steps (tenant config, scheduling module, roles).");

        return sb.toString();
    }

    // ── Generic fallback ──────────────────────────────────────────────────────

    private String buildGenericCliPrompt(AgentDefinition agent, Map<String, String> inputs) {
        StringBuilder sb = new StringBuilder();
        sb.append(agent.getName()).append(" request for Telestaff.\n\n");
        inputs.forEach((k, v) -> {
            if (v != null && !v.isBlank()) {
                sb.append("**").append(k).append("**: ").append(v.trim()).append("\n\n");
            }
        });
        return sb.toString();
    }

    // =========================================================================
    // Direct CLI prompt dispatch  (no --agent flag)
    // =========================================================================

    /**
     * Injected into every direct-CLI prompt so Claude knows to fall back to Seeker
     * when Jira / GitHub / Confluence MCP tools are unavailable or return errors.
     */
    private static final String TOOL_USAGE_INSTRUCTIONS =
        "\n\n---\n\n" +
        "## Tool Usage Instructions\n\n" +
        "You have access to MCP tools. Use them proactively to enrich your analysis " +
        "rather than working from the provided text alone.\n\n" +
        "| Tool | When to use |\n" +
        "|---|---|\n" +
        "| `mcp__jira__jira_get_issue` | Fetch full ticket details when a Jira ID is supplied |\n" +
        "| `mcp__jira__jira_search` | Find related tickets, duplicates, or linked issues |\n" +
        "| `mcp__github__search_pull_requests` | Find PRs touching the same component or fixing similar bugs |\n" +
        "| `mcp__github__get_file_contents` | Read the current source file for the class under review |\n" +
        "| `mcp__github__search_code` | Locate usages of a class, method, or pattern across the repo |\n" +
        "| `mcp__confluence__confluence_search` | Find runbooks, design docs, ADRs, or post-mortems |\n" +
        "| `mcp__seeker__ask_seeker` | Search the Telestaff codebase for classes, methods, schema, config |\n\n" +
        "### Fallback Rule — Seeker First\n\n" +
        "If **any** MCP tool is unavailable, returns an error, requires credentials that are not " +
        "configured, or times out — **immediately fall back to `mcp__seeker__ask_seeker`**. " +
        "Seeker has a full index of the Telestaff codebase (all four repos: workforce-telestaff, " +
        "kronos-common, wfts-bidding, tsc) and can surface class implementations, method call graphs, " +
        "DB schema mappings, Liquibase changesets, and configuration.\n\n" +
        "Example Seeker queries that are especially useful:\n" +
        "- `\"LocationService implementation and DAO methods\"`\n" +
        "- `\"How is TenantContext propagated in JMS listeners?\"`\n" +
        "- `\"Caches evicted when a shift record is updated\"`\n" +
        "- `\"Liquibase changesets for the scheduling module\"`\n" +
        "- `\"@PreAuthorize expressions used on roster endpoints\"`\n\n" +
        "Do not skip tool calls just because the user did not paste source code — " +
        "fetch it yourself using the tools above.\n\n" +
        "---\n\n";

    /**
     * Builds the full prompt for agents that run via {@code claude --print <prompt>} directly.
     * Each agent gets a rich, Telestaff-specific prompt tailored to its purpose.
     * Sprint Readiness starts with {@code /ticket-quality} to invoke the skill.
     */
    public String buildDirectCliPrompt(AgentDefinition agent, Map<String, String> inputs) {
        switch (agent.getId()) {
            case "code-review":
                return buildCodeReviewPrompt(inputs);
            case "release-notes":
                return buildReleaseNotesPrompt(inputs);
            case "sprint-readiness":
                return buildSprintReadinessPrompt(inputs);
            case "production-incident":
                return buildProductionIncidentPrompt(inputs);
            default:
                // Fallback: use the standard API prompt as a direct CLI prompt
                return buildUserPrompt(agent, inputs);
        }
    }

    // ── Code Review Agent ─────────────────────────────────────────────────────

    private String buildCodeReviewPrompt(Map<String, String> inputs) {
        String prId       = inputs.getOrDefault("prId",       "").trim();
        String code       = inputs.getOrDefault("code",       "").trim();
        String reviewType = inputs.getOrDefault("reviewType", "Full Code Review").trim();
        String context    = inputs.getOrDefault("context",    "").trim();

        StringBuilder sb = new StringBuilder();
        sb.append("You are a senior Java engineer and code reviewer for UKG Telestaff — an enterprise ")
          .append("workforce management system built with Java 11, Spring 5.3 (XML config, NOT Spring Boot), ")
          .append("Hibernate 5.4, PostgreSQL 17, deployed on GCP.\n\n")
          .append("## Telestaff Coding Conventions (enforce strictly)\n\n")
          .append("- **Entities**: `*Tbl` suffix required; JPA/Hibernate annotations on getters, NEVER on fields\n")
          .append("- **Services**: interface (`*Service`) + impl (`*ServiceImpl`); `@Transactional` on impl ")
          .append("mutations only; `@Transactional(readOnly=true)` on reads\n")
          .append("- **Security**: `@PreAuthorize` on service interface methods only (not impls, not controllers)\n")
          .append("- **Caching**: use `@XxxEvict` annotations only; never call `cacheManager.getCache().evict()` directly\n")
          .append("- **LaunchDarkly**: every new feature/code path MUST be gated behind a `featureFlagService.isFeatureEnabled()` check\n")
          .append("- **Multi-tenancy**: every DAO query must filter by institution/tenant; never leak cross-tenant data; ")
          .append("`TenantContext` must be set on child threads\n")
          .append("- **Checkstyle**: 4-space indent, no tabs, no star imports, braces required, left curly same line\n")
          .append("- **DAO**: only interfaces exposed to callers (extend `GenericDao<T,PK>`); impl extends `AbstractTeleStaffDaoJpa`\n")
          .append("- **JAXB schemas**: never edit classes in `telestaff-schemas` — auto-generated\n\n");

        if (!prId.isEmpty()) {
            sb.append("## PR / Branch\n").append(prId).append("\n\n");
        }
        sb.append("## Review Scope\n").append(reviewType).append("\n\n");
        if (!context.isEmpty()) {
            sb.append("## Context\n").append(context).append("\n\n");
        }

        if (!code.isEmpty()) {
            sb.append("## Code / Diff to Review\n\n```\n").append(code).append("\n```\n\n");
        } else if (!prId.isEmpty()) {
            sb.append("## Code to Review\n\nReview the code changes in PR/branch: **").append(prId)
              .append("**. Focus on the changes introduced; call out anything that violates the conventions above.\n\n");
        } else {
            sb.append("## Note\nNo code diff was provided. Provide a general code review checklist and ")
              .append("the top Telestaff-specific pitfalls to watch for in this type of change.\n\n");
        }

        sb.append("## Required Output Format\n\n")
          .append("Produce a structured code review report with these sections:\n\n")
          .append("### Code Review Report\n\n")
          .append("#### Summary\n")
          .append("2-3 sentence overview of the change and overall quality.\n\n")
          .append("#### Critical Issues ❌\n")
          .append("Must fix before merge. Format each as:\n")
          .append("- **[CRITICAL]** `ClassName.method()` — Description. **Fix**: specific action.\n\n")
          .append("#### Major Issues ⚠️\n")
          .append("Should fix. Same format.\n\n")
          .append("#### Minor Issues / Suggestions 💡\n")
          .append("Nice to have. Same format.\n\n")
          .append("#### Security Analysis\n")
          .append("Multi-tenancy data leaks, `@PreAuthorize` gaps, injection risks, sensitive data exposure.\n\n")
          .append("#### Multi-tenancy Compliance\n")
          .append("Institution filter present? TenantContext propagated on new threads? Cross-tenant leak risk?\n\n")
          .append("#### LaunchDarkly Compliance\n")
          .append("Is the new code path gated? Flag key named correctly? Both flag states tested?\n\n")
          .append("#### Caching Review\n")
          .append("Correct eviction annotations? No lazy proxies in cached objects? TTL defined?\n\n")
          .append("#### Convention Compliance\n")
          .append("Checkstyle, naming, annotation placement, layer violations.\n\n")
          .append("#### Overall Assessment\n")
          .append("**VERDICT**: `APPROVE` ✅ | `REQUEST CHANGES` ❌ | `APPROVE WITH SUGGESTIONS` ⚠️\n")
          .append("List required actions before this can be merged.");

        sb.append(TOOL_USAGE_INSTRUCTIONS);
        return sb.toString();
    }

    // ── Release Notes Generator ───────────────────────────────────────────────

    private String buildReleaseNotesPrompt(Map<String, String> inputs) {
        String version     = inputs.getOrDefault("releaseVersion", "").trim();
        String jiraTickets = inputs.getOrDefault("jiraTickets",    "").trim();
        String audience    = inputs.getOrDefault("audience",       "All Audiences").trim();
        String releaseDate = inputs.getOrDefault("releaseDate",    "").trim();

        if (version.isEmpty()) {
            throw new IllegalArgumentException("Release Version is required for Release Notes Generator");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("You are a technical writer and product manager for UKG Telestaff, an enterprise ")
          .append("workforce management platform used by public safety organizations (police, fire, EMS) ")
          .append("and healthcare institutions.\n\n")
          .append("Generate professional release notes for **Telestaff ").append(version).append("**");
        if (!releaseDate.isEmpty()) {
            sb.append(", scheduled for release on **").append(releaseDate).append("**");
        }
        sb.append(".\n\n");

        sb.append("**Primary Audience**: ").append(audience).append("\n\n");

        if (!jiraTickets.isEmpty()) {
            sb.append("## Changes Included in This Release\n\n").append(jiraTickets).append("\n\n");
        }

        sb.append("## Audience-Specific Instructions\n\n");

        if (audience.contains("Customer") || audience.contains("All")) {
            sb.append("**For Customers**: Focus on user-visible improvements and business value. ")
              .append("Avoid internal Jira IDs and technical class names. ")
              .append("Use plain language. Emphasize how each change benefits their daily workflow ")
              .append("(scheduling, rostering, shift management).\n\n");
        }
        if (audience.contains("Engineering") || audience.contains("All")) {
            sb.append("**For Engineering**: Include technical details — affected modules, ")
              .append("LaunchDarkly flags that need to be enabled post-deploy, Liquibase migration notes, ")
              .append("any system.properties changes, and service restart requirements.\n\n");
        }
        if (audience.contains("Executive") || audience.contains("All")) {
            sb.append("**For Executive / Leadership**: Emphasize strategic value, compliance improvements, ")
              .append("performance gains, and risk reduction. Keep to 1-2 paragraphs. Avoid technical details.\n\n");
        }

        sb.append("## Required Output Format\n\n")
          .append("---\n\n")
          .append("# Release Notes: Telestaff ").append(version).append("\n\n");
        if (!releaseDate.isEmpty()) {
            sb.append("**Release Date**: ").append(releaseDate).append("  \n");
        }
        sb.append("**Version**: ").append(version).append("  \n\n")
          .append("---\n\n")
          .append("## Executive Summary\n")
          .append("[2-3 sentence strategic overview for leadership]\n\n")
          .append("## What's New\n")
          .append("[New features and capabilities — group logically, not by Jira ID]\n\n")
          .append("## Bug Fixes\n")
          .append("[Resolved issues — describe symptom and fix in user-friendly language]\n\n")
          .append("## Performance & Reliability Improvements\n")
          .append("[Any improvements to response times, stability, or uptime]\n\n")
          .append("## Security Updates\n")
          .append("[Security hardening, access control improvements — do not disclose vulnerability details]\n\n")
          .append("## Upgrade Notes\n")
          .append("[Required post-deployment steps: DB migrations, config changes, feature flag enablement, ")
          .append("service restart order]\n\n")
          .append("## API / Integration Changes\n")
          .append("[Breaking or additive changes to REST/SOAP APIs — include endpoint paths and change type]\n\n")
          .append("## Known Limitations / Issues\n")
          .append("[Any known issues shipping with this release and workarounds]\n\n")
          .append("---\n")
          .append("*Telestaff ").append(version)
          .append(" | UKG Workforce Management | Confidential*");

        sb.append(TOOL_USAGE_INSTRUCTIONS);
        return sb.toString();
    }

    // ── Sprint Readiness Agent (invokes /ticket-quality skill) ────────────────

    private String buildSprintReadinessPrompt(Map<String, String> inputs) {
        String sprintName    = inputs.getOrDefault("sprintName",    "").trim();
        String stories       = inputs.getOrDefault("stories",       "").trim();
        String teamCapacity  = inputs.getOrDefault("teamCapacity",  "").trim();
        String blockers      = inputs.getOrDefault("blockers",      "").trim();

        if (sprintName.isEmpty() && stories.isEmpty()) {
            throw new IllegalArgumentException("Sprint name or stories are required for Sprint Readiness");
        }

        // Start with the /ticket-quality skill invocation.
        // Claude CLI will resolve this slash command from .claude/commands/ticket-quality.md
        // in the working directory (workforce-telestaff repo).
        StringBuilder sb = new StringBuilder();
        sb.append("/ticket-quality\n\n");

        sb.append("Evaluate the ticket quality and sprint readiness for the following sprint:\n\n");

        if (!sprintName.isEmpty()) {
            sb.append("**Sprint**: ").append(sprintName).append("\n");
        }
        if (!teamCapacity.isEmpty()) {
            sb.append("**Team Capacity**: ").append(teamCapacity).append(" story points\n");
        }
        sb.append("\n");

        if (!stories.isEmpty()) {
            sb.append("## Stories / Tickets in Sprint\n\n").append(stories).append("\n\n");
        }

        if (!blockers.isEmpty()) {
            sb.append("## Known Blockers / Risks\n\n").append(blockers).append("\n\n");
        }

        sb.append("---\n\n")
          .append("After completing the ticket quality analysis, also produce a **Sprint Readiness Dashboard**:\n\n")
          .append("## Sprint Readiness Dashboard\n\n")
          .append("### Overall Readiness Score\n")
          .append("Score: X / 100\n\n")
          .append("| Category | Score | Assessment |\n")
          .append("|---|---|---|\n")
          .append("| Ticket Quality | | |\n")
          .append("| Acceptance Criteria Clarity | | |\n")
          .append("| Capacity Alignment | | |\n")
          .append("| Dependency Risk | | |\n")
          .append("| Technical Debt Risk | | |\n\n")
          .append("### Story-by-Story Analysis\n")
          .append("For each story: completeness rating, estimate confidence, LaunchDarkly flag needed?, ")
          .append("Telestaff-specific risks (multi-tenancy, caching, DB migration), recommendation.\n\n")
          .append("### Risk Flags\n")
          .append("Critical blockers, unclear requirements, missing acceptance criteria, ")
          .append("stories without estimates.\n\n")
          .append("### Capacity Assessment\n")
          .append("Estimated total points vs capacity. Buffer recommendation.\n\n")
          .append("### Recommendations\n")
          .append("Prioritized action items the team should complete before sprint start.\n\n")
          .append("### Go / No-Go Recommendation\n")
          .append("Clear **GO** or **NO-GO** with reasoning and conditions.");

        sb.append(TOOL_USAGE_INSTRUCTIONS);
        return sb.toString();
    }

    // ── Production Incident Summarizer ────────────────────────────────────────

    private String buildProductionIncidentPrompt(Map<String, String> inputs) {
        String incidentId   = inputs.getOrDefault("incidentId",   "").trim();
        String description  = inputs.getOrDefault("description",  "").trim();
        String timeline     = inputs.getOrDefault("timeline",     "").trim();
        String resolution   = inputs.getOrDefault("resolution",   "").trim();
        String severity     = inputs.getOrDefault("severity",     "").trim();

        if (description.isEmpty()) {
            throw new IllegalArgumentException("Incident description is required");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("You are an experienced SRE and incident manager for UKG Telestaff, an enterprise ")
          .append("workforce management platform used by public safety organizations (police, fire, EMS) ")
          .append("and healthcare institutions. A system incident has occurred. ")
          .append("Produce a structured, professional post-mortem / incident report.\n\n");

        sb.append("## Incident Details\n\n");
        if (!incidentId.isEmpty()) {
            sb.append("**Incident ID**: ").append(incidentId).append("  \n");
        }
        if (!severity.isEmpty()) {
            sb.append("**Severity**: ").append(severity).append("  \n");
        }
        sb.append("\n");

        sb.append("## Incident Description\n\n").append(description).append("\n\n");

        if (!timeline.isEmpty()) {
            sb.append("## Timeline of Events\n\n").append(timeline).append("\n\n");
        }
        if (!resolution.isEmpty()) {
            sb.append("## Resolution / Workaround Applied\n\n").append(resolution).append("\n\n");
        }

        sb.append("---\n\n")
          .append("## Required Report Format\n\n")
          .append("Generate a complete incident report with ALL of the following sections:\n\n")

          .append("### Executive Summary\n")
          .append("3-4 sentence summary: what happened, business impact, how resolved, current status.\n\n")

          .append("### Impact Assessment\n")
          .append("| Dimension | Details |\n")
          .append("|---|---|\n")
          .append("| Severity | |\n")
          .append("| Affected Features / Services | |\n")
          .append("| Affected Tenants / Institutions | |\n")
          .append("| User Impact | |\n")
          .append("| Incident Duration | |\n")
          .append("| Data Loss | Yes / No |\n")
          .append("| SLA Breach | Yes / No |\n\n")

          .append("### Incident Timeline\n")
          .append("| Time (UTC) | Event | Actor |\n")
          .append("|---|---|---|\n")
          .append("[Reconstruct or expand the timeline with likely events based on the description]\n\n")

          .append("### Root Cause Analysis\n")
          .append("**Primary Cause**:  \n")
          .append("**Contributing Factors**:  \n\n")
          .append("**5-Why Analysis**:\n")
          .append("1. Why did the incident occur?\n")
          .append("2. Why did that happen?\n")
          .append("3. Why?\n")
          .append("4. Why?\n")
          .append("5. Root cause:\n\n")

          .append("### Resolution Steps\n")
          .append("Chronological steps taken to resolve the incident, with commands or config changes if relevant.\n\n")

          .append("### Action Items\n")
          .append("| Priority | Action Item | Suggested Owner | Target Date |\n")
          .append("|---|---|---|---|\n")
          .append("| P0 — Immediate | | Engineering | |\n")
          .append("| P1 — This Sprint | | | |\n")
          .append("| P2 — Next Sprint | | | |\n\n")

          .append("### Prevention Measures\n")
          .append("Technical improvements (monitoring, alerting, circuit breakers, feature flags), ")
          .append("process improvements (runbooks, on-call procedures), and architectural changes ")
          .append("that would prevent recurrence.\n\n")

          .append("### Monitoring Gaps Identified\n")
          .append("Missing alerts, dashboards, or health checks that would have detected this sooner.\n\n")

          .append("### Lessons Learned\n")
          .append("Key takeaways for the team — what went well (detection, response), what to improve.\n\n")

          .append("### Incident Metrics\n")
          .append("| Metric | Value |\n")
          .append("|---|---|\n")
          .append("| MTTD (Mean Time to Detect) | |\n")
          .append("| MTTA (Mean Time to Acknowledge) | |\n")
          .append("| MTTR (Mean Time to Resolve) | |\n")
          .append("| Total Incident Duration | |\n\n")

          .append("---\n")
          .append("*Report prepared by UKG Telestaff SDLC Agents Portal | Confidential*");

        sb.append(TOOL_USAGE_INSTRUCTIONS);
        return sb.toString();
    }

    // =========================================================================
    // Anthropic REST API prompts  (used by all other agents)
    // =========================================================================

    public String buildSystemPrompt(AgentDefinition agent) {
        return SYSTEM_CTX + "\n\nYou are acting as the **" + agent.getName() + "** agent. "
                + "Your role: " + agent.getDescription();
    }

    public String buildUserPrompt(AgentDefinition agent, Map<String, String> inputs) {
        StringBuilder sb = new StringBuilder();
        sb.append("## ").append(agent.getName()).append(" Request\n\n");

        String jiraId = inputs.get("jiraId");
        if (jiraId != null && !jiraId.isBlank()) {
            sb.append("**Jira Reference:** ").append(jiraId).append("\n\n");
        }

        agent.getFields().forEach(field -> {
            String value = inputs.get(field.getId());
            if (value != null && !value.isBlank()) {
                sb.append("### ").append(field.getLabel()).append("\n");
                sb.append(value).append("\n\n");
            }
        });

        sb.append("---\n\n## Required Outputs\n\n");
        agent.getOutputs().forEach(o -> sb.append("- ").append(o).append("\n"));
        sb.append("\n");

        appendInstructions(sb, agent.getId());
        return sb.toString();
    }

    private void appendInstructions(StringBuilder sb, String agentId) {
        switch (agentId) {
            case "feature-design-hld":
                sb.append("Generate a comprehensive HLD document including:\n")
                  .append("1. Executive Summary\n2. Architecture Overview\n3. Component Breakdown\n")
                  .append("4. Data Flow (text/ASCII)\n5. Integration Points\n6. Technology Choices with justification\n")
                  .append("7. Non-Functional Requirements\n8. Open Questions / Risks\n")
                  .append("9. LaunchDarkly Flag name and rollout strategy\n");
                break;
            case "feature-design-lld":
                sb.append("Generate a detailed LLD including:\n")
                  .append("1. Class Design (package names, responsibilities)\n")
                  .append("2. Sequence Diagrams (Mermaid format)\n")
                  .append("3. API Contract (method, path, request/response DTOs)\n")
                  .append("4. Database Schema Changes (Liquibase changeSet format)\n")
                  .append("5. Service Layer (method signatures, @PreAuthorize, @Transactional boundaries)\n")
                  .append("6. Cache Strategy (what to cache, eviction triggers, TTL)\n")
                  .append("7. Error Handling (exception types, HTTP status codes)\n")
                  .append("8. LaunchDarkly Integration point in the code\n");
                break;
            case "design-review":
                sb.append("Review this design and provide:\n")
                  .append("1. Overall Assessment: PASS / NEEDS REVISION / FAIL\n")
                  .append("2. Critical Issues (must fix)\n3. Major Issues (should fix)\n4. Minor Issues\n")
                  .append("5. Security Analysis (multi-tenancy, @PreAuthorize, data exposure)\n")
                  .append("6. Performance Analysis (N+1 queries, missing indexes, cache strategy)\n")
                  .append("7. Convention Violations (naming, annotations, layer violations)\n")
                  .append("8. LaunchDarkly Compliance\n9. Specific, actionable recommendations\n");
                break;
            case "customer-issue-analyzer":
                sb.append("Analyze this issue and provide:\n")
                  .append("1. Issue Classification: Config Bug / DB Script / Code Bug / Data Issue\n")
                  .append("2. Severity Assessment with business impact\n")
                  .append("3. Investigation Summary\n")
                  .append("4. Root Cause Hypothesis (ranked by likelihood)\n")
                  .append("5. Affected Components (modules, classes, DB tables)\n")
                  .append("6. Regression Analysis\n")
                  .append("7. Investigation Checklist for the developer\n")
                  .append("8. Recommended Fix Approach\n");
                break;
            case "junit-test-generator":
                sb.append("Generate complete JUnit test code:\n")
                  .append("1. Test Class (follow Telestaff conventions: *Test unit, *IT integration)\n")
                  .append("2. Mock Setup using Mockito @Mock, @InjectMocks, MockitoAnnotations.initMocks\n")
                  .append("3. Test Methods with given_when_then naming\n")
                  .append("4. Happy Path, Edge Case, Error Case, and Feature Flag tests\n")
                  .append("5. AssertJ assertions (assertThat)\n")
                  .append("Use @RunWith(SpringJUnit4ClassRunner.class) + BaseServiceIT for integration tests.\n");
                break;
            case "environment-provisioning":
                sb.append("Generate a complete environment provisioning runbook including:\n")
                  .append("1. Pre-Provisioning Checklist (GCP IAM roles, VPC, networking prereqs)\n")
                  .append("2. GCP Resource Creation Steps (GKE namespace, Cloud SQL instance, Memorystore Redis)\n")
                  .append("3. Helm Chart Configuration (values.yaml overrides for the target env, tsc-telestaff + tsc-workflow)\n")
                  .append("4. Secrets & Config Setup (GCP Secret Manager entries, system.properties, application.properties)\n")
                  .append("5. Liquibase Migration Steps (run order: telestaff-web then telestaff-activiti-rest)\n")
                  .append("6. Service Deployment Order and kubectl/Helm commands\n")
                  .append("7. Hosts File / DNS Setup (e.g. telestaff.tenant2 -> ingress IP)\n")
                  .append("8. LaunchDarkly Flag Configuration for the new environment\n")
                  .append("9. Smoke Test Checklist (login, tenant resolution, scheduling page, API health endpoint)\n")
                  .append("10. Rollback Steps if provisioning fails\n");
                break;
            case "release-deployment":
                sb.append("Generate a deployment guide including:\n")
                  .append("1. Pre-Deployment Checklist\n")
                  .append("2. Ordered Deployment Steps with commands\n")
                  .append("3. Liquibase Migration Plan\n")
                  .append("4. LaunchDarkly Flag State (enable/disable timing)\n")
                  .append("5. Health Verification checks\n")
                  .append("6. Rollback Procedure with decision criteria\n")
                  .append("7. Go/No-Go Criteria (metrics and thresholds)\n")
                  .append("8. Post-Deployment Monitoring (first 24 hours)\n");
                break;
            default:
                sb.append("Provide a comprehensive, well-structured Markdown response. ")
                  .append("Be specific and actionable, referencing Telestaff conventions. ")
                  .append("Use headers, tables, and code blocks.\n");
        }
    }
}
