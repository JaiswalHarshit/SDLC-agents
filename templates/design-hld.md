# HLD: [Title]

## 1. Overview
- Problem statement
- Solution summary (1 paragraph)
- Scope: in / out / deferred
- Repositories or services affected: [repo/service names]

## 2. Current State
- Current system behaviour in the affected area
- Current flow: entry point → data store or downstream system (high-level numbered steps)
- Key components involved: domain models, services, controllers/handlers, caches, jobs, integrations
- Known limitations or tech debt in the current implementation

## 3. Architecture
- Component diagram (text-based)
- Affected modules/components and their roles
- Cross-repository or cross-service impact, if any
- Proposed flow: entry point → data store or downstream system (numbered steps showing new behaviour)

## 4. Data Model
- New or modified entities/models: fields, types, relationships
- Database changes: migration plan
- Migration strategy, if phased: release N adds backward-compatible structure → release N+1 enforces stricter constraints
- Estimated table sizes and growth rate for new tables or data stores

## 5. API Design
- New or modified endpoints: method, path, request, response
- Response pattern: existing API response wrapper / new response wrapper / plain response body
- Error codes and error handling
- API versioning approach, if modifying existing contracts

## 6. Integration Points
- External systems affected
- Message flows, if queues, streams, events, or pub/sub are involved
- Message class design: new fields optional with safe defaults for rolling deploys
- SOAP/REST/GraphQL/gRPC client changes, if applicable
- Scheduled job changes: scheduler, frequency, thread pool, and runtime impact

## 7. Cross-Cutting Concerns

### 7.1 Feature Flag / Configuration Guard
- Flag or configuration key, default: off / disabled / existing behaviour
- Guard locations: services, methods, endpoints, jobs, or workflows
- Behaviour when disabled
- Rollout plan and cleanup ticket

### 7.2 Data Isolation / Multi-Scope Access
- Data scoping model: customer, tenant, organization, workspace, account, region, or other scope
- Query and access filtering strategy
- Cross-scope exemptions, if any, with justification

### 7.3 Caching
- New or modified cache entries
- Cache tier: in-memory / distributed / hybrid
- TTL/TTI values
- Eviction strategy: trigger methods, annotations, events, or explicit invalidation
- Serialization safety: avoid non-serializable objects, lazy-loaded proxies, or unstable object shapes in cached data

### 7.4 Audit
- Entities, actions, or workflows requiring audit trail
- Audit events to log

## 8. Security Design
- Authentication: affected auth flows or filter/middleware changes
- Authorization: permission checks, roles, policies, ACL services, or access-control rules
- Data exposure: DTO/response filtering, PII handling, sensitive field exclusion
- Input validation: standard validation annotations, custom validators, injection prevention
- Data isolation: scoping verified on all data paths

| # | Risk | Security Category | Severity | Mitigation |
|---|---|---|---|---|
| S-1 | Description | Category | Critical/High/Medium/Low | Mitigation |

## 9. Performance Design
- Query performance: indexed columns, estimated row counts, complex join justification
- N+1 risk assessment: collections fetched in loops and mitigation strategy
- Caching strategy: cache tier, TTL/TTI, eviction triggers
- Transaction scope: boundaries, read-only flags, lock contention risk
- Concurrency: distributed locks, retry policy, idempotency, thread pool impact
- Scalability: scaling characteristics and impact on large customers or high-volume environments

| # | Area | Current State | Proposed Impact | Risk Level | Recommendation |
|---|---|---|---|---|---|
| P-1 | Description | ... | ... | Critical/High/Medium/Low | ... |

## 10. Backward Compatibility
- Rolling restart safety: can old code and new code run simultaneously against the same database, cache, queues, or APIs?
- Database migrations: all DDL additive and backward-compatible where possible
- Cache serialization: new or removed fields on cached objects → deployment cache flush needed?
- Message serialization: new fields on queued/event objects → optional with safe defaults?
- API contract: response shape changes → versioned endpoint or additive-only?
- Phased migration plan, if changes require multiple releases

## 11. Observability
- Logging:
  - Key events and log levels: INFO for business events, WARN for recoverable errors, ERROR for failures
  - Context keys: account/customer/scope ID, request ID, correlation ID, transaction ID, user ID
  - Existing logging gaps in the affected area
- Metrics: response time, throughput, cache hit rates, queue depth, error rates
- Alerting: failure conditions, thresholds, escalation path
- Health checks: new health indicators for added dependencies

## 12. Deployment & Rollout
- Applications, services, workers, or deployable units affected
- Database migration: automatic at startup / manual / pipeline-driven
- Cache flush requirements at deployment
- Configuration changes: new beans, components, listeners, environment variables, or import statements
- Rollout sequence: guard disabled → deploy → enable in test environment → enable for limited users/customers → enable globally
- Rollback plan: disable guard → redeploy previous version → migration rollback or compensating script, if applicable

## 13. Risks & Mitigations

| # | Risk | Category | Severity | Mitigation |
|---|---|---|---|---|
| R-1 | Description | Security/Performance/Data/Ops | Critical/High/Medium/Low | Mitigation |

## 14. Tech Decisions

| # | Decision | Options Considered | Chosen | Rationale |
|---|---|---|---|---|
| D-1 | Description | Option A, Option B | Option A | Why |

## 15. Assumptions
- Assumptions made during design, each marked for verification before implementation
- Suggested default for each assumption

## 16. Open Questions
- Unresolved questions from requirement analysis
- For each: what is unclear, why it matters, suggested default

## 17. Work Breakdown (Stories)

| # | Story Title | Description | Module(s) / Component(s) | Depends On | SP | Est. Days | Acceptance Criteria |
|---|---|---|---|---|---|---|---|
| S-1 | As a [role], I can [action] so that [value] | What this delivers | `[module/component]` | — | 2 | 1 | - AC1<br>- AC2 |
| | | | | **Total** | **Σ** | **Σ** | |

### Dependency Graph
- Text-based dependency graph showing story execution order
- Recommended implementation sequence with rationale
