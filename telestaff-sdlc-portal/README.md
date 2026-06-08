# Telestaff SDLC Agents Portal

An AI-powered internal platform that puts specialized agents throughout every phase of the Telestaff software development lifecycle — from requirements to release.

Built for executive demos, innovation showcases, and proof-of-concept presentations.

---

## Quick Start

```bash
# 1. Clone / navigate to the project
cd telestaff-sdlc-portal

# 2. (Optional) Set your Anthropic API key for real AI responses
export ANTHROPIC_API_KEY=sk-ant-...

# 3. Start the app
mvn spring-boot:run -Pnexus

# 4. Open in browser
open http://localhost:8090
```

The app starts in **~6 seconds** and runs entirely in-memory — no database, no Redis, no external infrastructure required.

---

## Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `ANTHROPIC_API_KEY` | _(empty)_ | Anthropic API key. Without it the app runs in demo simulation mode. |
| `ANTHROPIC_MODEL` | `claude-opus-4-5` | Claude model to use |
| `ANTHROPIC_MAX_TOKENS` | `4096` | Max tokens per response |
| `SERVER_PORT` | `8090` | HTTP port (avoids conflict with Telestaff on 8080) |

All settings live in `src/main/resources/application.yml`.

---

## Demo Mode (no API key)

Without `ANTHROPIC_API_KEY`, every agent returns a formatted demo response that explains how to enable real AI. The full streaming UX still works — you see the text appear word-by-word.

---

## Architecture

```
telestaff-sdlc-portal/
├── src/main/java/com/ukg/telestaff/sdlc/
│   ├── TelestaffSdlcPortalApplication.java   # Spring Boot entry point
│   ├── config/
│   │   └── AgentRegistryConfig.java          # Central registry of all 20 agents
│   ├── model/
│   │   ├── AgentDefinition.java              # Immutable agent metadata
│   │   ├── InputField.java                   # Dynamic form field definition
│   │   ├── AgentExecution.java               # Per-run state (in-memory)
│   │   ├── ExecutionStatus.java              # PENDING / RUNNING / COMPLETED / FAILED
│   │   └── FieldType.java                    # TEXT / TEXTAREA / SELECT / MARKDOWN
│   ├── agent/
│   │   ├── AgentPromptBuilder.java           # Builds system + user prompts per agent
│   │   └── AgentExecutor.java                # @Async executor — calls Claude, streams back
│   ├── service/
│   │   ├── AgentRegistryService.java         # Lookup, search, filter agents
│   │   ├── ExecutionService.java             # ConcurrentHashMap<String, AgentExecution>
│   │   └── AnthropicService.java             # Java 11 HttpClient -> Anthropic streaming API
│   └── controller/
│       ├── DashboardController.java          # GET /
│       ├── AgentController.java              # GET /agents/{id}, POST /agents/{id}/execute
│       ├── ExecutionController.java          # GET /executions/{id}, SSE /executions/{id}/stream
│       └── HistoryController.java            # GET /history
├── src/main/resources/
│   ├── application.yml
│   ├── templates/
│   │   ├── dashboard.html                    # Agent card grid
│   │   ├── agent-form.html                   # Dynamic agent input form
│   │   ├── execution.html                    # Live streaming output page
│   │   └── history.html                      # Execution history
│   └── static/
│       ├── css/ukg-theme.css                 # UKG design system
│       └── js/app.js                         # Client-side helpers
└── local-settings.xml                        # Alt Maven settings (bypasses Nexus)
```

### Request Flow

```
POST /agents/{id}/execute
  → AgentController extracts inputs
  → ExecutionService.createExecution() → AgentExecution stored in ConcurrentHashMap
  → AgentExecutor.execute() [async thread]
      → AgentPromptBuilder builds system + user prompt
      → AnthropicService.streamBlocking() → Java HttpClient → Anthropic API
      → Each text chunk → execution.appendChunk() → LinkedBlockingQueue
  → Redirect to /executions/{executionId}

GET /executions/{id}/stream (SSE)
  → ExecutionController opens SseEmitter
  → Dedicated thread drains LinkedBlockingQueue
  → Each chunk sent as SSE "chunk" event
  → Client JS appends to rawMarkdown → marked.parse() → live render
  → DONE_SENTINEL → SSE "done" event → emitter.complete()
```

---

## The 20 Agents

### Requirements & Design
| Agent | Status | Generates |
|-------|--------|-----------|
| Feature Design - HLD | **Live** | HLD, Architecture Overview, Component Breakdown |
| Feature Design - LLD | **Live** | LLD, Sequence Flows, Class Diagrams, API Contracts |
| Design Review | **Live** | Review Findings, Risks, Recommendations |
| Requirements Clarification | **Live** | Clarifying Questions, Refined Requirements |
| Story Breakdown | **Live** | Sub-stories, Story Points, Sprint Plan |

### Development
| Agent | Status | Generates |
|-------|--------|-----------|
| Customer Issue Analyzer | **Live** | Investigation Summary, RCA, Developer Handoff |
| Defect Fix Designer | **Live** | Fix Design, Impact Analysis, Rollback Plan |
| Implement Fix | Placeholder | Implementation Plan, Code Changes |
| Implement Feature | Placeholder | Implementation Plan, Classes, APIs |
| Code Review Agent | **Live** | Review Findings, Security Issues, Violations |
| Root Cause Analysis | **Live** | 5-Why Analysis, Prevention Plan |

### Testing
| Agent | Status | Generates |
|-------|--------|-----------|
| JUnit & Integration Test Generator | **Live** | JUnit 4/5 Tests, Integration Tests |
| Test Plan & Manual Test Generator | **Live** | Test Plan, Test Cases, Coverage Matrix |
| Automated Test Generator | Placeholder | Automated Test Design |
| Risk Assessment Agent | **Live** | Risk Register, Mitigation Strategies |

### Environment & Release
| Agent | Status | Generates |
|-------|--------|-----------|
| Environment Provisioning | Placeholder | Environment Steps, Config Checklist |
| Release Deployment | **Live** | Deployment Plan, Release Checklist, Rollback |
| Release Notes Generator | **Live** | Customer Notes, Internal Notes, Exec Summary |
| Sprint Readiness | **Live** | Readiness Score, Risk Flags, Recommendations |
| Production Incident Summarizer | **Live** | Incident Report, Timeline, Action Items |

**15 Live · 5 Placeholder**

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 11 |
| Framework | Spring Boot 2.7.18 |
| Templates | Thymeleaf 3 |
| Interactivity | HTMX 1.9, Vanilla JS |
| Styling | Bootstrap 5.3, Custom UKG Design System |
| Streaming | Server-Sent Events (SseEmitter) |
| HTTP Client | Java 11 `java.net.http.HttpClient` |
| AI | Anthropic Claude via REST API |
| State | `ConcurrentHashMap<String, AgentExecution>` |
| Markdown | Marked.js (client-side) |
| Code Highlighting | Highlight.js |

**No database. No JMS. No Redis. No external dependencies.**

---

## Notes for Presenters

- The portal runs entirely offline except for the Anthropic API call
- Demo mode (no API key) still shows the full streaming animation with a formatted placeholder response
- Response history is stored in memory — restart clears it
- Port 8090 is intentional to avoid conflict with Telestaff on 8080
- The `local-settings.xml` can be used if Nexus is unavailable: `mvn spring-boot:run -s local-settings.xml`
