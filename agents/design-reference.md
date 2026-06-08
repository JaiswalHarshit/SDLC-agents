# Design Reference — Generic Software Product

Architecture lookup tables for a generic multi-module software product. This is the primary repository reference for a design agent.

> **Last verified:** [YYYY-MM-DD]. Approximate counts may drift — re-verify if precision matters.

> **Note:** Project-specific architecture documentation and coding rules define the full tech stack, module structure, request flow, data isolation model, caching architecture, security model, and coding conventions. This file contains additive design-specific reference data only. Read the main project guidance first.
>
> For sibling repositories, see the corresponding design-reference files for shared libraries, integration services, and provisioning/management tools.

---

## SOURCE REPOSITORIES

| Repo | Purpose | Local Path |
|---|---|---|
| Main Application | Primary app — UI, APIs, services, domain logic, authentication, jobs | `[../main-application]` |
| Integration Service | External integrations, adapters, message handlers, integration APIs | `[../integration-service]` |
| Shared Library | Shared utilities, base data-access abstractions, domain interfaces, common exceptions, feature/config helpers | `[../shared-library]` |
| Management / Provisioning | customer/account provisioning, copy-environment, tenant/customer migration tooling | `[../management-service]` |

### Repository selection by package or area

- Main application packages → Main Application repository
- Integration packages or adapters → Integration Service repository
- Shared/common packages → Shared Library repository
- Provisioning, migration, or environment-copy functionality → Management / Provisioning repository

---

## MODULE BLAST RADIUS

Use this classification for impact assessment:

- **Critical**: shared libraries, domain model, core utilities, platform-wide configuration
- **High**: service layer, persistence/migration modules, shared API contracts
- **Medium**: web/API layer, UI-facing controllers, specific integration modules
- **Low**: isolated features, generated schemas, optional adapters, narrow utility modules

---

## DOMAIN MODEL — CORE ENTITIES

### Entity Relationship Summary

```text
Customer / Account / Organization Scope
  ├── Organizational Unit
  │   └── Location / Operational Unit
  ├── User / Resource
  │   ├── Preferences / Skills / Qualifications
  │   └── Assignments / Rotations
  ├── Role / Position
  │   ├── Schedule / Shift
  │   ├── Job Type / Category
  │   └── Required Qualifications
  └── Assignment / Transaction Record
      ├── User / Resource
      ├── Role / Position
      ├── Time / Schedule
      └── Location / Operational Unit
```

### Entity Stats

- Entity classes: `[count or approximate count]`
- Enum classes: `[count or approximate count]`
- Cache entries: `[count or approximate count]`
- Cache eviction annotations/hooks: `[count or approximate count]`
- Junction/cross-reference tables use the project-standard suffix or naming convention
- Effective dating, soft-delete, or disableable behavior should use project-standard interfaces
- Enums persisted by ordinal must never be reordered; prefer stable string/code persistence for new enums where possible

### Business Subdomains

| Subdomain | Key Entities | Key Services |
|---|---|---|
| Organization Structure | organization, location, unit, group, category entities | organization/setup services |
| People / Workforce | person/user/resource, qualification, job-title entities | user/resource/people services |
| Positions / Jobs | position, shift, location, job role entities | position/shift services |
| Scheduling / Planning | schedule, calendar, template, override entities | scheduling services |
| Rules / Policies | rule, strategy, policy, penalty entities | rule/policy services |
| Notifications | notification, message, provider configuration entities | notification/contact services |
| Integrations | integration request, execution, token, mapping entities | integration services |
| Reporting | report definition, report execution entities | report services |
| Workflow | process, task, approval entities | workflow services |

---

## CROSS-CUTTING CONCERNS — AOP / MIDDLEWARE

Data isolation, caching, and security model details should be defined in the main project rules. The execution order below is design-critical when multiple concerns are applied to the same path.

| Order | Aspect / Middleware | Annotation / Trigger | Purpose |
|---|---|---|---|
| 0 | Distributed lock aspect | `@DistributedLock` / lock guard | Cluster-wide mutex for write contention |
| 1 | Deadlock/transient retry aspect | `@Retryable` / retry guard | Auto-retry on transient DB or upstream failures |
| varies | Cache eviction aspect | eviction annotation, event, or hook | Cache invalidation after mutations |
| varies | Access-control aspect | policy annotation / ACL hook | Scope-aware authorization validation |
| varies | Feature-flag aspect | feature/config guard | Gate new behavior by feature or configuration |
| varies | Synchronization barrier | sync-required annotation | Block dependent operations until sync completes |
| varies | Audit support aspect | audit-support annotation / listener | Entity state capture for audit diff |
| varies | ACL logging aspect | ACL method calls | Access-control decision logging |
| varies | Task interrupt aspect | task/job cancellation hook | Interrupt or cancel long-running background work |
| varies | Transaction logging aspect | transactional methods | Transaction lifecycle and slow-operation logging |
| varies | External connection validator | connection-validation guard | Upstream connectivity validation gate |

---

## EXCEPTION & ERROR PATTERNS

### Exception Hierarchy — Design-Critical

Document whether API exceptions share a common base class or are independent siblings. Do not catch a broad exception type expecting it to catch sibling exception classes unless the hierarchy confirms it.

| Exception | Extends | Carries | Default HTTP Status | Package |
|---|---|---|---|---|
| `ApiException` | `RuntimeException` | error code + HTTP status | 500, overridable | `[package]` |
| `BadRequestException` | `RuntimeException` or API base | validation errors | 400 | `[package]` |
| `ResourceNotFoundException` | `RuntimeException` or API base | validation/error payload | 404 or 410 | `[package]` |
| `ServiceUnavailableException` | API base | message/details | 503 | `[package]` |

### API Exception Handler Mappings

| Exception | HTTP Status |
|---|---|
| validation/binding/type mismatch/readability errors | 400 |
| bad request exception | 400 |
| access denied exception | 403 |
| route not found exception | 404 |
| not acceptable exception | 406 |
| resource not found exception | 404 or 410, depending on project convention |
| unsupported media type exception | 415 |
| API exception with status | dynamic |
| catch-all exception | 500 |

### Common API Response Codes

| Code | HTTP Status | Default Message |
|---|---|---|
| `SUCCESS` | 200 | Success |
| `BAD_REQUEST` | 400 | Invalid request |
| `UNAUTHORIZED` | 401 | Unauthorized access |
| `FORBIDDEN` | 403 | Access denied |
| `NOT_FOUND` | 404 | Resource not found |
| `VALIDATION_ERROR` | 400 | Validation failed |
| `SESSION_EXPIRED` | 401 | Session expired |
| `INTERNAL_SERVER_ERROR` | 500 | Internal server error |
| `SERVICE_UNAVAILABLE` | 503 | Service unavailable |
| `CONFLICT` | 409 | Conflict |

### API Response Static Factories

For projects using a response wrapper, prefer consistent factory methods:

```java
ApiResponse.ok(data);
ApiResponse.ok(data, message);
ApiResponse.ok(data, responseCode);
ApiResponse.error(ResponseCode.BAD_REQUEST);
ApiResponse.error(responseCode, message);
ApiResponse.error(responseCode, errorPayload);
```

Implement a project-standard response-code interface or enum for domain-specific error codes.

### REST Error Codes

Error codes should follow a stable `DOMAIN-CODE` pattern. Grep the project error-code enum/class for the exact meaning.

| Prefix | Domain | Examples |
|---|---|---|
| `ORG-` | Organization / setup | validation, conflict |
| `USR-` | User / person | duplicate identity field, validation |
| `SCH-` | Schedule | validation, not found |
| `REP-` | Reports | validation, permission, rendering |
| `IMP-` / `IMPORT-` | Import | records not found, file upload |
| `EXP-` / `EXPORT-` | Export | records not found, task failure |
| `INT-` | Integration | upstream failure, payload validation |
| `CACHE-` | Cache | validation, internal cache error |

### Custom Domain Exceptions

| Exception | Module / Area | Meaning |
|---|---|---|
| `WriteLockTimeoutException` | core | DB write lock exceeded timeout |
| `PotentialDeadLockException` | core | DB deadlock detected |
| `DisableBeforeDeleteException` | core/domain | entity must be disabled before deletion |
| `MergeConflictException` | domain | merge or reconciliation conflict |
| `InvalidDateTimeException` | core | date/time parsing failed |
| `EncryptionException` | core/security | encryption or decryption failed |
| `WorkflowClaimedTaskException` | workflow | workflow task already claimed |
| `NotificationFailureException` | notifications | messaging subsystem failure |
| `WebServiceConfigurationException` | integration | SOAP or web-service endpoint config error |
| `WebServiceExecutionException` | integration | SOAP or web-service runtime failure |
| `ImportAllOrNoneException` | import | transactional import rolled back |
| `PasswordChangeException` | security | password policy violation |
| `InvalidOrganizationalEntityException` | domain | record references invalid organization entity |
| `ProviderConfigurationException` | integration/notification | external provider misconfiguration |
| `ProviderAuthenticationException` | integration/notification | provider credentials invalid or expired |

---

## AUDIT TRAIL DESIGN

### Annotations / Configuration

| Annotation / Config | Target | Key Attributes |
|---|---|---|
| `@Audit` / audit config | type/entity | target, operations, display/name field, parent ID field |
| `@AuditOnInsert` | type/entity | expression/template |
| `@AuditOnUpdate` | type/entity | expression/template |
| `@AuditOnDelete` | type/entity | expression/template |
| `@AuditOnRename` | type/entity | display/name field and expression |
| `@AuditIgnoreField` | field/property | excludes field from change tracking |

### Audit Target Enum / Registry

Each audit target should have a stable ID/code and optional parent target for child/cross-reference records.

| Pattern | Examples |
|---|---|
| Top-level targets | shift, position, person, strategy, organization |
| Cross-reference/child targets | position-shift link, strategy-rule link |
| Setup/preference targets | preference, smart template, configuration object |

### Audit Support

Audit support should capture current entity state before mutation, initialize required lazy fields or referenced data, and compare old/new states after mutation.

### Design Rules for Audit

1. Add audit metadata on the entity or register it in audit configuration
2. Add audit support on mutating service methods
3. Register the entity/service in the audit registry if required
4. Expressions/templates must reference valid i18n/message keys
5. Exclude non-auditable fields explicitly
6. For nested/child entities, map audit events back to the parent ID

---

## DTO CONVENTIONS

### Location & Naming

| Location | Package | Naming |
|---|---|---|
| Primary domain/API DTOs | `[domain.dto package]` | `*DTO` or project-standard suffix |
| Date/time DTOs | `[date package]` | project-standard suffix |
| Integration DTOs | `[integration DTO package]` | project-standard suffix |
| Web/controller models | `[web model package]` | project-standard suffix |
| Generated schemas | `[generated package]` | request/response naming from schema |

### Base Interface

Use the project-standard DTO marker interface or serialization interface where applicable.

### Rules for New DTOs

1. Place in the project-standard DTO package
2. Use project-standard DTO naming
3. Implement required marker/serialization interfaces
4. Never return persistence entities from REST/API endpoints; always map to DTOs
5. Exclude sensitive or PII fields from serialization where appropriate

---

## DEPLOYMENT TOPOLOGY

### Deployable Artifacts

| Artifact | Context / Endpoint | Source Module | Purpose |
|---|---|---|---|
| primary web/API app | `[context path]` | `[web/API module]` | controllers, views, security |
| workflow/process app | `[context path]` | `[workflow module]` | workflow/process REST endpoints |
| integration app | `[context path]` | `[integration module]` | integration endpoints and adapters |
| background worker | n/a | `[worker module]` | scheduled jobs and async processing |

### Deployment Sequence

1. Build using the project-standard build command
2. Run mandatory migration or compatibility step, if applicable
3. Start required infrastructure dependencies, such as cache, message broker, or local services
4. Deploy artifacts to the target runtime
5. Verify application URLs and health checks

### Rolling Restart Requirements

Old and new code may run simultaneously during deployment. Every change must be safe for both versions:

- Database migrations: additive only where possible; new columns nullable or defaulted
- Serialization: new fields optional with safe defaults for cache/messages/events
- Feature/config guards: default-off or default-safe; both paths functional
- Cache: key format changes require coordinated cache flush or versioning
- APIs: additive-only response changes or versioned endpoints

---

## TRANSACTION & ENTITY MANAGER

| Component | Bean / Name | Notes |
|---|---|---|
| Transaction manager | `[transactionManager]` | specify explicitly if the project has multiple transaction managers |
| Entity manager / session | `[entityManager/session]` | understand flush behavior and transaction boundaries |
| Persistence unit | `[persistenceUnit]` | use the project-standard persistence unit name |

---

## ARCHITECTURAL PATTERNS — NEW FEATURE CHECKLIST

### New Entity

1. Create entity/model class in the domain module using project naming conventions
2. Map table/collection name to the actual database/store name
3. Implement required serialization or marker interfaces
4. Implement scope/ownership interface if customer/account scoped
5. Add database migration if persistence schema changes

### New Data Access Object / Repository

1. Interface extends the project-standard DAO/repository abstraction, if applicable
2. Implementation uses project-standard base class and repository annotation
3. Expose interface, not concrete implementation, outside the data layer
4. Use read-only transactions by default for query methods

### New Service

1. Interface in the service module with authorization policy where applicable
2. Implementation has service annotation/registration and transactions on mutations
3. Read methods use read-only transactions where supported
4. Add cache eviction if mutable/cached data changes
5. Add retry/lock policy if concurrent write contention is expected
6. Add access validation if returning scoped entities/data

### New REST/API Endpoint

1. Controller/handler in the project-standard web/API module
2. Use the project-standard URL and versioning pattern
3. Put authorization on the appropriate service/policy layer
4. Return DTOs, never persistence entities
5. Use the project-standard response wrapper or error model
6. Validate input using request DTO validation

### New Scheduled Job

1. Ensure non-concurrent execution if duplicate runs are unsafe
2. Use the correct job base class or framework hook
3. Add job type/registry value if required
4. Register with the scheduler or job manager
5. Assign to the correct scheduler/thread pool

### New Feature Flag / Configuration Guard

1. Add entry to the project-standard flag/config registry
2. Create the flag/config value in the management system
3. Wrap code behind flag/config service
4. Test enabled and disabled states
5. Create cleanup ticket if the flag is temporary

### New Cache Entry

1. Add cache entry with scope-awareness metadata
2. Configure TTL/TTI or expiration in all cache tiers
3. Create eviction hook/annotation/utility if needed
4. Add cache service or adapter where applicable
5. Verify all write paths trigger eviction
6. Cached type must be safe for the cache serialization mechanism

### New Audit Trail

1. Add audit metadata on the entity/action
2. Add audit support on mutating service methods
3. Register entity/action in audit registry if required
4. Exclude non-auditable fields
5. For nested entities, map to parent ID when appropriate

### Database Migration

1. Migration ID and author follow project conventions
2. New columns are nullable or have safe defaults
3. No single-step drops/renames of columns referenced by current code
4. DML must be idempotent and batched for large tables
5. Use online/concurrent index creation where supported
6. Backward-compatible for rolling restart

---

## API PATTERNS

### Response Wrappers

| Pattern | Classes | URL Paths | For New Code? |
|---|---|---|---|
| Legacy | legacy response wrapper and util | legacy or integration endpoints | no, unless modifying existing legacy controller |
| Current | current response wrapper and response-code enum | current or internal API paths | yes |

### URL Patterns by Audience

| URL Pattern | Auth | Audience |
|---|---|---|
| browser UI paths | form login / SSO | browser UI |
| integration API paths | API header / service token | integrations |
| public/external API paths | API key, OAuth, or basic auth | external clients |
| internal API paths | service token / session / internal auth | internal UI or services |

### Validation by Pattern

| Legacy Pattern | Current Pattern |
|---|---|
| explicit validator class and aggregate error list | request DTO validation annotations |
| errors through legacy response util | standard validation exception handler |
| i18n message keys | response-code enum or domain-specific code enum |
| throw legacy bad request exception | return/throw current error response consistently |

---

## INTEGRATION POINTS

| System | Technology | Direction | Key Classes |
|---|---|---|---|
| Workforce or scheduling system | REST | bidirectional | integration data service, connection validator |
| Contact/communication provider | messaging + provider API | outbound | contact/notification service, message class |
| Email/SMS/push provider | provider SDK or REST | outbound | message gateway, notification service |
| SOAP clients | SOAP framework | outbound | generated client, service adapter |
| Import/export | batch + messaging | inbound/outbound | import service, export task service |
| Report engine | report framework | internal | report service, report processor |
| Workflow engine | BPM/workflow framework | internal | workflow service, process definitions |
| Data warehouse | REST/SQL/cloud SDK | outbound | datahub/analytics service |
| Pub/sub or event bus | messaging/cloud pub-sub | internal | cache/event consumer |

---

## MESSAGING / INTEGRATION CHANNEL MAPPING

All outbound messages should enter through a gateway/producer service.

| Gateway Method | Request Channel | Reply Channel | Use Case |
|---|---|---|---|
| `sendMessage(...)` | outbound message channel | optional reply channel | fire-and-forget outbound messages |
| `sendBlockingMessage(...)` | blocking request channel | blocking response channel | synchronous request/response flow |

### Outbound Message Routing

| Message Class | Outbound Channel | Queue / Topic | Destination |
|---|---|---|---|
| email message | communication outbound channel | email queue/topic | email provider |
| SMS message | communication outbound channel | sms queue/topic | SMS provider |
| push message | communication outbound channel | push queue/topic | push provider |
| integration message | integration outbound channel | integration queue/topic | external integration |
| background-task message | task outbound channel | task queue/topic | worker service |

Headers or metadata commonly added before send: authentication, customer/account/scope ID, transaction ID, correlation ID.

### Inbound Message Routing

| Inbound Channel | Message Class | Service Activator / Consumer | Method |
|---|---|---|---|
| email inbound | email request | email service | process email request |
| SMS inbound | SMS request | SMS service | process SMS request |
| integration inbound | integration event | integration service | process integration event |
| task inbound | background task | task service | process task |

---

## RUNTIME CONFIGURATION — DESIGN REFERENCE

Key properties that affect design decisions.

### Thread Pools

| Property Category | Notes |
|---|---|
| feature-specific async pools | ensure pool size, queue capacity, and rejection policy are appropriate |
| export/background task pools | undersized pools cause backlog and apparent hangs |
| job-allocation or compute pools | isolate heavy computation from request threads |

### Timeouts

| Property Category | Notes |
|---|---|
| transaction timeout | long operations can fail or hold locks too long |
| slow-operation logging threshold | ensure new long-running flows emit useful warnings |
| session idle timeout | too low can cause redirect loops or broken long-running flows |
| external I/O timeout | set for SFTP/cloud/file/export operations |

### Schedulers

| Property Category | Notes |
|---|---|
| monitoring interval | controls job-state visibility |
| recovery interval | controls failed job recovery timing |
| scheduler thread counts | assess saturation when adding jobs |

### Notifications & Providers

| Property Category | Notes |
|---|---|
| provider type and credentials | credential expiry and provider mismatch cause delivery failures |
| polling interval and timeout | too long makes providers appear stuck; too short may overload dependencies |

### Integrations

| Property Category | Notes |
|---|---|
| processing batch size | too small causes backlog; too large can cause timeout |
| retry count and retryable error codes | distinguish permanent failures from transient failures |
| token refresh settings | incorrect values cause repeated auth failures |

### Reports

| Property Category | Notes |
|---|---|
| engine memory and buffers | too low causes large report failures |
| max rows and timeout | protects system from runaway report execution |

---

## APPLICATION CONFIG — KEY FILES

| File / Config | Location | Purpose | Blast Radius |
|---|---|---|---|
| root application config | service/resource config path | imports all core configuration | Critical |
| persistence config | service/resource config path | entity manager, connection pool, transaction manager | Critical |
| security config | web/API config path | auth filters, authorization rules | Critical |
| cache config | service/resource config path | cache managers and cache tiers | High |
| scheduler config | service/resource config path | scheduler factories and job registrations | High |
| messaging config | service/resource config path | broker, listeners, queues/topics | High |
| integration config | service/resource config path | gateway orchestration and adapters | High |
| batch config | service/resource config path | batch job and step config | Medium |
| web-service config | service/resource config path | SOAP or RPC endpoints | Medium |

---

## OBSERVABILITY — LOGGER PATTERNS

When designing observability for HLD/LLD sections, use existing logger infrastructure.

### Logger Categories

| Logger Category | When to Use |
|---|---|
| General troubleshooting | broad debugging |
| Authentication / SSO | login, token, session, and SSO |
| Observability / performance | task runtime, latency, throughput |
| Export / background task | export failures, discarded records, long-running tasks |
| Integration | external API, queue, webhook, sync failures |
| Cache | cache hit/miss/eviction tracing |
| Locking | distributed lock acquisition/release |
| Messaging | broker sends/receives, dead letters |
| Audit / access control | ACL and audit decision logging |

### Static / Named Loggers

| Accessor / Name | Purpose |
|---|---|
| cache logger | cache hit/miss/eviction tracing |
| lock logger | distributed lock acquisition/release |
| integration debug logger | integration or provider-specific debugging |
| temporary object/cache logger | temporary object or session cache operations |

### Transaction Tracing

Use the project-standard request ID, correlation ID, transaction ID, or trace ID to correlate all log entries for a single request.

---

## TEST BASE CLASSES

Document project-standard base classes and test framework choices here.

| Class | Package | Use Case | Key Setup |
|---|---|---|---|
| `BaseServiceIT` / equivalent | `[package]` | full application context + DB integration tests | transaction rollback, security/context setup |
| `BaseMultiScopeIT` / equivalent | `[package]` | data-isolation tests | scope context setup |
| `BaseDaoIT` / equivalent | `[package]` | DAO/repository integration tests | persistence context and DB setup |
| `AbstractDataDrivenIT` / equivalent | `[package]` | fixture-driven integration tests | fixture loading and setup helpers |

### Test Module Placement

| Test Type | Module | Base Class |
|---|---|---|
| Unit tests | same module as production class | Mockito or project-standard unit framework |
| DAO/repository integration tests | integration-test module or same module | base DAO integration test |
| Service integration tests | integration-test module or same module | base service integration test |
| Data-driven integration tests | integration-test module | data-driven base test |
| Web/API integration tests | web/API module or integration-test module | web/API integration base test |

### Testing Framework

Use the project-standard JUnit version and assertion library. Do not mix incompatible test lifecycle annotations in the same class.

---

## DESIGN DECISION MATRIX

| Decision Area | Preferred Approach | Anti-Pattern |
|---|---|---|
| Data access | project-standard DAO/repository abstraction | ad-hoc raw SQL where abstraction exists |
| Transactions | transactions on service layer, explicit manager if multiple exist | transactions on controllers or relying on wrong default manager |
| Caching | eviction hooks/annotations/events | manual one-off cache eviction scattered in code |
| Security | authorization on policy/service layer | security only in controller/UI |
| Config | project-standard runtime configuration | hard-coded toggles or unregistered env vars |
| Feature flags | flag registry + feature/config service | hard-coded toggles |
| Async | domain-specific thread pools | shared async pool without capacity planning |
| Messaging | gateway/producer + queue/topic mapping | direct broker calls scattered across services |
| Scheduled tasks | cluster-aware job framework | non-cluster-aware scheduler for distributed deployment |
| DB migrations | project-standard migration tool | manual DDL |
| REST API | current response wrapper/pattern for new endpoints | mixing legacy and current patterns in same controller |
| DTOs | separate DTO classes | returning persistence entities from APIs |
| Error handling | project-standard response codes and exception handlers | inconsistent ad-hoc error payloads |
| Logging | domain-specific or named loggers | `System.out.println` or unstructured logs |
| Audit | audit metadata + audit support | manual audit rows scattered in business logic |

---

## COMPLEXITY ESTIMATION GUIDE

| Complexity | Indicators | Typical Effort |
|---|---|---|
| **Trivial** | single file, config-only, existing pattern | < 1 day |
| **Low** | 2-3 files in one module, tests exist, single DAO/service/controller | 1-2 days |
| **Medium** | 3-5 files across 2 modules, new cache entry/eviction, new migration | 3-5 days |
| **High** | 5-10 files across 3+ modules, new integration point, new scheduled job, security changes | 1-2 weeks |
| **Very High** | shared module changes, new subsystem, multi-deployable impact, schema migration | 2-4 weeks |

### Complexity Multipliers

- Touches shared library → +1 level
- Touches security configuration → +1 level
- Adds new scheduled job → +1 level
- Cross-repository change → +1 level
- Requires phased migration → +1 level
- No existing test coverage → +0.5 level
- Adds new audit trail → +0.5 level
- Adds new messaging/integration flow → +1 level

---

## CONSTRAINT CHECKLIST FOR DESIGN REVIEW

Every design must address:

### Core Architecture
- [ ] Feature/config guard: code changes wrapped when required, default-off or default-safe
- [ ] Data isolation: customer/account/scope context propagated at all entry points
- [ ] Caching: cache entries identified, eviction paths defined, serialization safety checked
- [ ] Security: authorization and ACL/policy checks on changed paths
- [ ] Transactions: service-layer transaction boundaries; read-only for queries where applicable
- [ ] Entity mapping: project-standard naming, persistence mapping style, and serialization requirements
- [ ] Backward compatibility: rolling restart safe

### Data & Persistence
- [ ] Database migrations: additive changes, nullable columns or safe defaults, no single-step destructive changes
- [ ] Audit: audit metadata/support if entity changes require tracking
- [ ] DTOs: APIs return DTOs or response models, never persistence entities

### Input & Output
- [ ] Input validation: request DTO validation and safe parameterized queries
- [ ] Error handling: correct exception types and proper HTTP status codes
- [ ] API pattern: current API response pattern for new endpoints; legacy pattern only for existing legacy endpoints

### Performance
- [ ] Query efficiency: indexed columns, no N+1 patterns, projections where full entity loading is unnecessary
- [ ] Thread pool impact: schedulers, listeners, async pools assessed for saturation
- [ ] Concurrency: lock/retry policy where write contention is expected

### Observability
- [ ] Logging: key events logged via project-standard logger; appropriate levels
- [ ] Tracing: request/correlation/transaction ID included for request correlation

### Deployment
- [ ] Deployables affected: identified which apps/services/workers need redeployment
- [ ] Mandatory migration/compatibility step documented
- [ ] Cache flush: required if cached type serialization or key format changed
- [ ] Runtime config: new properties propagated to deployment/config repositories

### Testing
- [ ] Test coverage: unit and integration tests planned
- [ ] Test base class/framework: correct base selected
- [ ] Feature/config tests: enabled and disabled states tested where applicable
