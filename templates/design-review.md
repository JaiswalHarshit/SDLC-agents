# Design Review: [Title]

## 1. Summary
- Document reviewed: [Confluence page / local file / Jira description / design document]
- Review scope: [what was evaluated]
- Overall assessment: [Ready / Needs Revision / Major Rework Required]

## 2. Architecture Compliance
- [ ] Module/component placement follows the project dependency graph
- [ ] Pattern compliance: data access, service, API/controller, and integration patterns match adjacent code
- [ ] No unsupported framework patterns introduced
- [ ] Persistence mapping follows project-standard annotation and mapping style
- [ ] Domain model naming follows project conventions
- [ ] New API endpoints use the project-standard response pattern

## 3. Backward Compatibility (Rolling Restart)
- [ ] Database DDL is additive: new columns are nullable or have safe defaults
- [ ] No single-step column/table drops or renames that current code references
- [ ] Cache serialization: cached object shape changes are flagged for deployment cache flush
- [ ] Message serialization: new fields on queued/event/cache objects are optional with safe defaults
- [ ] API contract: no breaking response shape changes to existing endpoints

## 4. Cross-Cutting Concerns
- [ ] Feature flag or configuration guard is defined when required, default-off or default-safe, and both paths are tested
- [ ] Data isolation: customer/account/workspace/organization/scope filtering is applied where applicable
- [ ] Caching: cache entries are defined, TTL/TTI set, eviction occurs on all write paths, and cached objects are serialization-safe
- [ ] Security: authorization checks, ACL/policy validation, and input validation are present on changed paths
- [ ] Audit: entities, actions, or workflows requiring audit trail are identified
- [ ] Transaction boundaries: mutations and read paths use appropriate transaction settings

## 5. Observability
- [ ] Key business events logged at appropriate levels: INFO/WARN/ERROR
- [ ] Request context propagated: customer/account/scope ID, request ID, correlation ID, transaction ID, user ID
- [ ] Metrics defined for new endpoints or processing paths
- [ ] Alerting conditions identified for failure scenarios
- [ ] Health checks added for new external dependencies

## 6. Requirement Gaps
| # | Gap | Severity | Recommendation |
|---|---|---|---|
| G-1 | Description | Critical/High/Medium/Low | What to add or clarify |

## 7. Security Findings
| # | Finding | Security Category | Severity | Recommendation |
|---|---|---|---|---|
| S-1 | Description | Category | Critical/High/Medium/Low | Fix |

## 8. Performance Findings
| # | Finding | Area | Severity | Recommendation |
|---|---|---|---|---|
| P-1 | Description | Query/Cache/Transaction/Concurrency/N+1 | Critical/High/Medium/Low | Fix |

## 9. Positive Observations
- What the design does well
- Good decisions that should be preserved

## 10. Recommendations
- Prioritized list of changes needed before implementation
- **Blocking**: must fix before implementation begins
- **Non-blocking**: should fix but can proceed in parallel

## 11. Assumptions
- Assumptions found in the design — are they valid?
- Missing assumptions that should be called out

## 12. Open Questions
- Questions that need answers before the design is final
- For each question, include why it matters and who should answer it
