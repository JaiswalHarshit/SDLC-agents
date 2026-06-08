# Fix: [ISSUE-KEY] — [Summary]

## 1. Current Behaviour & Reproduction
- What the system currently does (observed behaviour)
- Steps to reproduce:
  1. Preconditions (environment, configuration, user role, data setup)
  2. Action sequence
  3. Observed result vs. expected result
- Frequency: always / intermittent / data-dependent
- Affected versions / environments

## 2. Root Cause
- What is broken and why
- Root cause chain: symptom → intermediate cause → root cause
- Code path: entry point → failure point, with file and line references where verified

## 3. Alternative Approaches Considered

| # | Approach | Pros | Cons | Rejected Because |
|---|---|---|---|---|
| A-1 | Description | ... | ... | ... |

## 4. Fix

### 4.1 Code Changes
For each file:
- File: `[module/path/ClassName.java]`
- Change type: modify / create / delete
- What changes and why

### 4.2 Feature Flag / Configuration Guard
- Required: yes / no
- Flag or configuration key: ...
- Default state: off / disabled / existing behaviour
- Guard location: method(s), endpoint(s), job(s), or workflow step(s) protected by the guard
- Behaviour when disabled: old path preserved / fallback behaviour / not applicable

### 4.3 Database Migration / Data Correction
- Required: yes / no
- If yes: migration ID, DDL/DML summary, backward-compatibility verification
- Data correction script: required / not required
- Data correction scope: single customer / single environment / all affected records / not applicable

### 4.4 Application Configuration
- Files changed: configuration files, dependency-injection files, deployment descriptors, or runtime settings
- Bean/component additions or modifications
- Runtime configuration changes required: yes / no

## 5. Affected Caches
- Caches touched by the changed code path
- Eviction changes needed: new eviction trigger / modified trigger / none
- Cache flush required at deployment: yes / no
- Serialization compatibility: does the fix change any cached object's shape?

## 6. Security & Performance Check

### Security
- Does the fix introduce or close a security gap?
- Authorization coverage on changed paths
- Input validation: new or changed inputs validated?
- Data isolation preserved across customers, users, roles, or scopes?
- Data exposure: any new fields in responses containing sensitive data?

### Performance
- Does the fix affect query performance or caching?
- Transaction scope changes?
- N+1 or loop query risk?
- Concurrency / locking impact?
- External service, queue, or batch processing impact?

## 7. Backward Compatibility
- Rolling restart safe: can old code and new code run simultaneously?
- Message serialization: do changed classes appear in queues, events, or async messages?
- Cache serialization: are cached object shapes changed?
- API contract: any response shape changes visible to consumers?
- Database compatibility: can the schema support old and new code during deployment?

## 8. Impact
- Blast radius: which modules, services, applications, packages, or repositories are affected
- Regression risk: what existing behaviour could break
- Downstream consumers: services, jobs, scheduled tasks, APIs, or integrations that call the changed code

## 9. Rollback Plan
- Feature/configuration rollback: toggle guard off → expected behaviour
- Code rollback: revert commit → any side effects, including migrations already applied
- Data rollback: if data was migrated or corrected, is reversal possible?
- Operational rollback notes: deployment, cache, queue, or service restart considerations

## 10. Test Plan

### Unit Tests
- Test class: `[FullyQualifiedName]`
- Base class / runner / framework: project-standard unit test setup
- Key scenarios: fix path, old path when guard is disabled, edge cases, failure handling

### Integration Tests
- Test class: `[FullyQualifiedName]`
- Base class / framework: project-standard integration test setup
- Module or package: same module / shared integration-test module / other
- Data setup requirements

### Manual Verification
- Steps to verify the fix in a running environment
- Regression checks for adjacent functionality
- Configuration or feature-flag verification, if applicable

## 11. Assumptions
- Assumptions made during analysis, marked for verification before implementation
