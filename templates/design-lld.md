# LLD: [Title]

## 1. Overview
- What is changing and why
- Parent EPIC/Story reference
- Repositories or services affected: [repo/service names]

## 2. Current State
- Existing behaviour of the affected area (brief)
- Current flow: entry point → data store or downstream system (numbered steps)
- Key classes involved (fully qualified names)

## 3. Detailed Design

### 3.1 Entity / Domain Model Changes
- Class: `[com.example.domain.XxxEntity]`
- Status: new / modified
- Interfaces implemented: `Serializable`, domain-specific marker interfaces, or framework-required interfaces

| Field | Type | Column / Property | Constraints | New/Modified |
|---|---|---|---|---|
| fieldName | `Type` | `column_name` | nullable, FK, unique | new |

- Relationships:

| Type | Target | Cascade | Fetch / Loading Strategy | Join Column / Mapping Key | New/Modified |
|---|---|---|---|---|---|
| `@ManyToOne` / reference | `TargetEntity` | ... | LAZY / eager / explicit | `fk_column` | new |

**Reminder**: Follow the project-standard persistence annotation and mapping style.

### 3.2 Data Access Changes
- Interface: `[com.example.dao.XxxDao]`
- Impl: `[com.example.dao.impl.XxxDaoImpl]`

| Method | Signature | Query / Criteria / Repository Method | Return Type | New/Modified |
|---|---|---|---|---|
| findByX | `List<XxxEntity> findByX(Long xId)` | `SELECT ...` | `List<XxxEntity>` | new |

### 3.3 Service Changes
- Interface: `[com.example.service.XxxService]`
- Impl: `[com.example.service.impl.XxxServiceImpl]`

| Method | Signature | Authorization / Policy Check | Transaction Boundary | Business Logic Summary |
|---|---|---|---|---|
| doThing | `Result doThing(Long id)` | `permission or role check` | `transactional / read-only / none` | Description |

### 3.4 AOP & Cross-Cutting Annotations / Middleware

| Annotation / Middleware | Applied To | Purpose |
|---|---|---|
| `@Retryable` / retry policy | `XxxServiceImpl.doThing()` | Retry transient failures |
| `@DistributedLock` / lock policy | `XxxServiceImpl.doThing()` | Prevent concurrent conflicting updates |
| `@AccessValidated` / policy guard | `XxxServiceImpl.getList()` | Enforce access control |
| `@FeatureGate` / configuration guard | `XxxServiceImpl.doThing()` | Feature or configuration gating |

### 3.5 Controller / API Handler Changes
- Class: `[com.example.web.XxxController]`
- Response pattern: existing response wrapper / new response wrapper / plain response body

| Method | HTTP | Path | Request DTO | Response DTO | New/Modified |
|---|---|---|---|---|---|
| getX | GET | `/api/v2/xxx/{id}` | — | `ApiResponse<XxxDto>` | new |

### 3.6 Cache Changes
- Cache name: `[CACHE_NAME]`
- Scope-aware: yes / no
- Tier: in-memory / distributed / hybrid
- TTL / TTI: values and where configured
- Eviction strategy: annotation, event, explicit invalidation, or utility class
- Eviction trigger methods: list service methods or events that trigger eviction
- Serialization: cached type is safe for the configured cache technology

### 3.7 Database Migration Changeset
- Migration ID: `[ISSUE-KEY]-[sequence]`
- Author: `[name]`
- File: `[path/to/migration/file]`

```sql
-- DDL statements
```

- Backward-compatible: yes / no (if no, phased plan)
- Indexes: online/concurrent index creation for large tables, where supported
- Data migration: batched DML if needed

### 3.8 Feature Flag / Configuration Guard
- Flag or configuration key: `[KEY]`
- Default: off / disabled / existing behaviour
- Guard locations:

| Class | Method | Behaviour when OFF / Disabled |
|---|---|---|
| `XxxServiceImpl` | `doThing()` | Returns old behaviour / fallback response / unsupported operation |

### 3.9 Application Configuration
- Files changed and what's added/modified:

| File | Change | Description |
|---|---|---|
| `application.yml` / equivalent | property | New runtime setting |
| `dependency-injection-config` | component | New bean/component definition |
| `scheduler-config` | job | New scheduled job definition |
| `messaging-config` | listener | New listener/subscriber configuration |
| `security-config` | access rule | New URL, route, or permission rule |

### 3.10 Message / Event Design
_(Omit if no messaging changes)_

- Message class: `[FullyQualifiedName]`
- Serialization: Java Serializable / JSON / Avro / Protobuf / other
- Transport: queue / stream / pub-sub / application event
- New fields: all optional with safe defaults for rolling deploy compatibility
- Listener/subscriber class: `[FullyQualifiedName]`
- Context propagation: how customer/account/scope/request context flows into the listener

## 4. Security Design
- Authorization expressions, policy checks, or permission rules on new/modified methods
- ACL or access-control service usage
- Input validation: standard annotations, schema validation, or custom validators
- Data exposure: fields excluded from responses, PII handling, sensitive field masking
- Data isolation: customer/account/workspace/scope filtering on new queries verified
- Audit: entities or actions requiring audit trail

## 5. Performance Design
- Query performance: indexes required, estimated cardinality, join complexity
- Caching: cache name, tier, TTL, eviction trigger methods
- Transaction scope: boundaries, read-only flags, explicit transaction manager if applicable
- Concurrency: lock policy, retry policy, idempotency, thread pool impact
- N+1 risk: any collections fetched in loops? mitigation via join fetch, batching, projection, or pagination

## 6. Backward Compatibility
- Rolling restart safe: old and new code run simultaneously against the same DB, cache, queues, and APIs
- Cache serialization: new/removed fields on cached objects → flush needed?
- Message serialization: new fields on queued/event objects → optional with defaults?
- API contract: response shape changes → versioned endpoint or additive-only?
- Database migration: DDL changes backward-compatible (see changeset in 3.7)

## 7. Flow

### Success Path
1. Step-by-step from entry point to response
2. Include class names and method names at each step

### Failure Path
- Exception types thrown at each step
- HTTP status codes or error codes returned
- Retry behaviour, if applicable
- Fallback behaviour: feature/configuration disabled, circuit breaker, default response, or degraded mode

## 8. Test Plan

### Unit Tests

| Test Class | Module / Package | Base Class / Runner / Framework | Key Scenarios |
|---|---|---|---|
| `XxxServiceTest` | `[module]` | project-standard unit test framework | happy path, guard off, edge case X |

- Mocking strategy: mocks for data access/external dependencies, injected class under test
- Feature/configuration mock: enabled and disabled paths

### Integration Tests

| Test Class | Module / Package | Base Class / Framework | Data Setup |
|---|---|---|---|
| `XxxServiceIT` | `[integration-test-module]` | project-standard integration test setup | migration test data + inline setup |
| `XxxDaoIT` | `[integration-test-module]` | project-standard persistence test setup | ... |

### Manual Verification
- Steps to verify in a running environment
- Regression checks for adjacent functionality

## 9. Stories & Estimates

| # | Story Title | Description | Module(s) / Component(s) | Depends On | SP | Est. Days | Acceptance Criteria |
|---|---|---|---|---|---|---|---|
| S-1 | As a [role], I can [action] so that [value] | What this delivers | `[module/component]` | — | 2 | 1 | - AC1<br>- AC2 |
| | | | | **Total** | **Σ** | **Σ** | |

## 10. Files to Create/Modify

| # | File Path | Module / Component | Change Type | Description |
|---|---|---|---|---|
| 1 | `[module]/src/.../XxxEntity.java` | `[module]` | create | New entity/domain model |
| 2 | `[module]/src/.../XxxDao.java` | `[module]` | modify | Add findByX method |

## 11. Assumptions
- Assumptions made during design, each marked for verification before implementation
