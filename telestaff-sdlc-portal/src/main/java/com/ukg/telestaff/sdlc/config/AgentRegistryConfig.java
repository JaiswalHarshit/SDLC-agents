package com.ukg.telestaff.sdlc.config;

import com.ukg.telestaff.sdlc.model.AgentDefinition;
import com.ukg.telestaff.sdlc.model.InputField;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class AgentRegistryConfig {

    @Bean
    public List<AgentDefinition> agentDefinitions() {
        return Arrays.asList(

            // ===================== Requirements & Design =====================

            AgentDefinition.builder()
                .id("feature-design-hld")
                .name("Feature Design - HLD")
                .category("Requirements & Design")
                .description("Generate a High Level Design document with architecture overview, component breakdown, and integration strategy.")
                .icon("🏗️")
                .phase("Design")
                .placeholder(false)
                .outputs(Arrays.asList("High Level Design", "Architecture Overview", "Component Breakdown", "Integration Strategy"))
                .fields(Arrays.asList(
                    InputField.text("jiraId", "Jira Epic / Story ID", "e.g. PS-12345", true),
                    InputField.textarea("requirements", "Feature Requirements", "Describe the feature requirements, business rules, and acceptance criteria...", true),
                    InputField.select("complexity", "Complexity", Arrays.asList("Low", "Medium", "High", "Very High"), false),
                    InputField.textarea("constraints", "Technical Constraints", "Performance requirements, existing system constraints, third-party integrations...", false)
                ))
                .build(),

            AgentDefinition.builder()
                .id("feature-design-lld")
                .name("Feature Design - LLD")
                .category("Requirements & Design")
                .description("Generate Low Level Design with sequence flows, class diagrams, API contracts, and detailed technical design.")
                .icon("📐")
                .phase("Design")
                .placeholder(false)
                .outputs(Arrays.asList("Low Level Design", "Sequence Flows", "Class Diagrams", "API Contracts"))
                .fields(Arrays.asList(
                    InputField.text("jiraId", "Jira Story ID", "e.g. PS-12345", true),
                    InputField.textarea("hld", "High Level Design (HLD)", "Paste the HLD from the previous step or describe the high-level architecture...", false),
                    InputField.textarea("requirements", "Detailed Requirements", "User stories, acceptance criteria, edge cases...", true),
                    InputField.select("module", "Target Module", Arrays.asList(
                        "telestaff-web", "telestaff-services", "telestaff-domain",
                        "telestaff-commons", "telestaff-core", "wfts-bidding", "kronos-common"
                    ), false)
                ))
                .build(),

            AgentDefinition.builder()
                .id("design-review")
                .name("Design Review")
                .category("Requirements & Design")
                .description("Automated design review that identifies risks, violations, anti-patterns, and produces actionable recommendations.")
                .icon("🔍")
                .phase("Design")
                .placeholder(false)
                .outputs(Arrays.asList("Review Findings", "Risk Assessment", "Recommendations", "Compliance Check"))
                .fields(Arrays.asList(
                    InputField.text("jiraId", "Jira ID", "e.g. PS-12345", false),
                    InputField.textarea("design", "Design Document", "Paste the HLD/LLD design document to review...", true),
                    InputField.select("reviewType", "Review Focus", Arrays.asList(
                        "Full Design Review", "Security Review", "Performance Review",
                        "Multi-tenancy Review", "API Design Review", "Database Design Review"
                    ), false),
                    InputField.textarea("context", "Additional Context", "Architecture constraints, previous decisions, team concerns...", false)
                ))
                .build(),

            AgentDefinition.builder()
                .id("requirements-clarification")
                .name("Requirements Clarification")
                .category("Requirements & Design")
                .description("Analyze vague requirements and generate clarifying questions, assumptions, and a refined requirements document.")
                .icon("❓")
                .phase("Requirements")
                .placeholder(false)
                .outputs(Arrays.asList("Clarifying Questions", "Assumptions", "Refined Requirements", "Acceptance Criteria"))
                .fields(Arrays.asList(
                    InputField.text("jiraId", "Jira ID", "e.g. PS-12345", false),
                    InputField.textarea("requirements", "Raw Requirements", "Paste the raw or vague requirements from the Jira ticket or stakeholder...", true),
                    InputField.textarea("context", "Business Context", "What problem does this solve? Who are the users? Any known constraints?", false)
                ))
                .build(),

            AgentDefinition.builder()
                .id("story-breakdown")
                .name("Story Breakdown")
                .category("Requirements & Design")
                .description("Break down large epics or stories into well-defined, independently deliverable sub-stories with estimates.")
                .icon("📋")
                .phase("Planning")
                .placeholder(false)
                .outputs(Arrays.asList("Sub-stories", "Story Points", "Dependencies", "Sprint Plan"))
                .fields(Arrays.asList(
                    InputField.text("jiraId", "Epic / Story ID", "e.g. PS-12345", true),
                    InputField.textarea("epicDescription", "Epic / Story Description", "Describe the epic or large story to break down...", false),
                    InputField.select("teamSize", "Team Size", Arrays.asList("1-2", "3-5", "6-10"), false),
                    InputField.textarea("constraints", "Technical Constraints / Dependencies", "Known dependencies, blockers, or technical constraints...", false)
                ))
                .build(),

            // ===================== Development =====================

            AgentDefinition.builder()
                .id("customer-issue-analyzer")
                .name("Customer Issue Analyzer")
                .category("Development")
                .description("Deep investigation of customer-reported issues with root cause analysis, regression detection, and developer handoff report.")
                .icon("🔬")
                .phase("Development")
                .placeholder(false)
                .outputs(Arrays.asList("Investigation Summary", "Root Cause Analysis", "Regression Detection", "Developer Handoff"))
                .fields(Arrays.asList(
                    InputField.text("jiraId", "Jira Issue Key", "e.g. PS-821311", true),
                    InputField.textarea("description", "Issue Description", "Describe the customer-reported issue, symptoms, and impact...", false),
                    InputField.textarea("steps", "Steps to Reproduce", "1. Navigate to...\n2. Click...\n3. Observe...", false),
                    InputField.select("severity", "Severity", Arrays.asList("Critical", "High", "Medium", "Low"), false),
                    InputField.textarea("environment", "Environment Details", "Version, tenant, configuration, logs snippets...", false)
                ))
                .build(),

            AgentDefinition.builder()
                .id("defect-fix-designer")
                .name("Defect Fix Designer")
                .category("Development")
                .description("Design a targeted fix for a defect including impact analysis, validation approach, and rollback plan.")
                .icon("🛠️")
                .phase("Development")
                .placeholder(false)
                .outputs(Arrays.asList("Fix Design", "Impact Analysis", "Validation Approach", "Rollback Plan"))
                .fields(Arrays.asList(
                    InputField.text("jiraId", "Jira Defect ID", "e.g. PS-12345", true),
                    InputField.textarea("defectDescription", "Defect Description", "What is broken? What is the expected behavior?", false),
                    InputField.textarea("rootCause", "Root Cause (if known)", "Known root cause from investigation or logs...", false),
                    InputField.textarea("affectedClasses", "Affected Classes / Areas", "Known classes, modules, or DB tables involved...", false),
                    InputField.select("fixType", "Fix Type", Arrays.asList("Code Fix", "Configuration Fix", "DB Script", "Hotfix", "Rollback"), false)
                ))
                .build(),

            AgentDefinition.builder()
                .id("implement-fix")
                .name("Implement Fix")
                .category("Development")
                .description("Generates an implementation plan and suggested code changes based on the fix design.")
                .icon("💻")
                .phase("Development")
                .placeholder(true)
                .outputs(Arrays.asList("Implementation Plan", "Suggested Code Changes", "Test Strategy"))
                .fields(Arrays.asList(
                    InputField.text("jiraId", "Jira Defect ID", "e.g. PS-12345", true),
                    InputField.textarea("fixDesign", "Fix Design", "Paste the fix design from the Defect Fix Designer agent...", true)
                ))
                .build(),

            AgentDefinition.builder()
                .id("implement-feature")
                .name("Implement Feature")
                .category("Development")
                .description("Generates implementation plan, suggested classes, and API stubs from HLD/LLD design documents.")
                .icon("⚡")
                .phase("Development")
                .placeholder(true)
                .outputs(Arrays.asList("Implementation Plan", "Suggested Classes", "Suggested APIs", "DB Schema"))
                .fields(Arrays.asList(
                    InputField.text("jiraId", "Jira Story ID", "e.g. PS-12345", true),
                    InputField.textarea("hld", "High Level Design", "Paste the HLD document...", true),
                    InputField.textarea("lld", "Low Level Design", "Paste the LLD document...", false)
                ))
                .build(),

            AgentDefinition.builder()
                .id("code-review")
                .name("Code Review Agent")
                .category("Development")
                .description("Automated code review checking for bugs, security issues, performance problems, and Telestaff coding conventions.")
                .icon("👁️")
                .phase("Development")
                .placeholder(false)
                .outputs(Arrays.asList("Review Findings", "Security Issues", "Performance Issues", "Convention Violations"))
                .fields(Arrays.asList(
                    InputField.text("prId", "Pull Request / Branch", "PR number or branch name", false),
                    InputField.textarea("code", "Code Diff / Snippet", "Paste the code diff or snippet to review...", false),
                    InputField.select("reviewType", "Review Type", Arrays.asList(
                        "Full Code Review", "Security Focus", "Performance Focus",
                        "Convention Check", "DB/Liquibase Review", "API Review"
                    ), false),
                    InputField.textarea("context", "Context", "What does this change do? Any known concerns?", false)
                ))
                .build(),

            AgentDefinition.builder()
                .id("root-cause-analysis")
                .name("Root Cause Analysis")
                .category("Development")
                .description("Systematic root cause analysis for production incidents, recurring defects, or complex bugs using 5-Why methodology.")
                .icon("🔎")
                .phase("Development")
                .placeholder(false)
                .outputs(Arrays.asList("Root Cause", "5-Why Analysis", "Contributing Factors", "Prevention Plan"))
                .fields(Arrays.asList(
                    InputField.textarea("incident", "Incident / Problem Description", "Describe the incident, symptoms, and business impact...", true),
                    InputField.textarea("timeline", "Timeline of Events", "When did it start? What changed? Who noticed first?", false),
                    InputField.textarea("logs", "Relevant Logs / Error Messages", "Paste relevant log excerpts, stack traces, or error messages...", false),
                    InputField.textarea("attempts", "Previous Investigation", "What has already been investigated or tried?", false)
                ))
                .build(),

            AgentDefinition.builder()
                .id("create-open-prs")
                .name("Create / Open PRs")
                .category("Development")
                .description("Generate pull request titles, descriptions, and gh CLI commands for one or all repos from a Jira ticket and branch — ready to copy-paste or auto-submit.")
                .icon("🔀")
                .phase("Development")
                .placeholder(false)
                .outputs(Arrays.asList("PR Titles", "PR Descriptions", "gh CLI Commands", "Cross-Repo Checklist"))
                .fields(Arrays.asList(
                    InputField.text("jiraId", "Jira Story / Defect ID", "e.g. PS-12345", true),
                    InputField.text("branchName", "Feature Branch Name", "e.g. feature/PS-12345-my-feature", true),
                    InputField.select("repos", "Target Repos", Arrays.asList(
                        "workforce-telestaff only",
                        "workforce-telestaff + kronos-common",
                        "workforce-telestaff + wfts-bidding",
                        "All repos (telestaff + bidding + common + tsc)"
                    ), false),
                    InputField.text("baseBranch", "Base / Target Branch", "e.g. main or release/2025.1", false),
                    InputField.textarea("summary", "Change Summary", "What does this PR do? Key changes, affected areas, migration notes...", false),
                    InputField.select("prType", "PR Type", Arrays.asList("Feature", "Bug Fix", "Hotfix", "Refactor", "Chore"), false)
                ))
                .build(),

            AgentDefinition.builder()
                .id("create-ld-flag")
                .name("Create LaunchDarkly Flag")
                .category("Development")
                .description("Scaffold a new LaunchDarkly feature flag with correct naming conventions, default-off state, and wiring into LaunchDarklyFlagEnum.")
                .icon("🚩")
                .phase("Development")
                .placeholder(true)
                .outputs(Arrays.asList("Flag Definition", "LaunchDarklyFlagEnum Entry", "LD Dashboard Config", "Usage Snippet"))
                .fields(Arrays.asList(
                    InputField.text("flagKey", "Flag Key", "e.g. PS-12345-my-new-feature", true),
                    InputField.text("jiraId", "Jira Story ID", "e.g. PS-12345", false),
                    InputField.textarea("description", "Flag Description", "What feature does this flag gate? When will it be cleaned up?", false),
                    InputField.select("scope", "Flag Scope", Arrays.asList("Global", "Per-Institution", "Per-Tenant"), false)
                ))
                .build(),

            AgentDefinition.builder()
                .id("create-branches")
                .name("Create Branches")
                .category("Development")
                .description("Create feature branches across workforce-telestaff, kronos-common, wfts-bidding, and tsc repos with consistent naming from a Jira ticket.")
                .icon("🌿")
                .phase("Development")
                .placeholder(true)
                .outputs(Arrays.asList("Branch Commands", "PR Templates", "Cross-Repo Checklist", "Branch Name Summary"))
                .fields(Arrays.asList(
                    InputField.text("jiraId", "Jira Story / Epic ID", "e.g. PS-12345", true),
                    InputField.text("branchName", "Branch Name Suffix", "e.g. my-feature-description", true),
                    InputField.select("repos", "Target Repos", Arrays.asList(
                        "workforce-telestaff only",
                        "workforce-telestaff + kronos-common",
                        "All repos (telestaff + bidding + common + tsc)"
                    ), false),
                    InputField.text("baseBranch", "Base Branch", "e.g. main or release/2024.3", false)
                ))
                .build(),

            AgentDefinition.builder()
                .id("ld-flag-archive")
                .name("LD Flag Archive")
                .category("Development")
                .description("Identify stale LaunchDarkly flags that are fully rolled out, generate cleanup PRs to remove the flag checks, and archive the flag in LD.")
                .icon("🗄️")
                .phase("Development")
                .placeholder(true)
                .outputs(Arrays.asList("Stale Flag Report", "Code Cleanup Plan", "Removal PR", "LD Archive Steps"))
                .fields(Arrays.asList(
                    InputField.text("flagKey", "Flag Key to Archive", "e.g. PS-12345-my-old-feature", true),
                    InputField.textarea("affectedModules", "Affected Modules / Classes", "Known classes or modules that reference this flag...", false),
                    InputField.select("cleanupScope", "Cleanup Scope", Arrays.asList(
                        "Identify usages only", "Generate removal PR", "Full archive + cleanup"
                    ), false)
                ))
                .build(),

            // ===================== Testing =====================

            AgentDefinition.builder()
                .id("junit-test-generator")
                .name("JUnit & Integration Test Generator")
                .category("Testing")
                .description("Generate comprehensive JUnit 4/5 unit tests and Spring integration tests following Telestaff testing conventions.")
                .icon("🧪")
                .phase("Testing")
                .placeholder(false)
                .outputs(Arrays.asList("JUnit Tests", "Integration Tests", "Test Data Setup", "Coverage Report"))
                .fields(Arrays.asList(
                    InputField.text("className", "Fully Qualified Class Name", "com.kronos.telestaff.service.impl.MyServiceImpl", true),
                    InputField.text("prBranch", "PR / Branch (for code lookup)", "e.g. PR-4521 or feature/PS-12345-my-feature", false),
                    InputField.textarea("methods", "Methods to Test", "List the method signatures to test (one per line)...", false),
                    InputField.select("testType", "Test Type", Arrays.asList(
                        "Unit Test", "JPA Integration Test", "Service Integration Test",
                        "Web Integration Test", "Data-Driven Test"
                    ), false),
                    InputField.textarea("classCode", "Class Source Code", "Paste the class source code (optional but improves quality)...", false)
                ))
                .build(),

            AgentDefinition.builder()
                .id("test-plan-generator")
                .name("Test Plan & Manual Test Generator")
                .category("Testing")
                .description("Generate detailed test plans, manual test cases, and traceability matrix from Jira tickets or feature descriptions.")
                .icon("📝")
                .phase("Testing")
                .placeholder(false)
                .outputs(Arrays.asList("Test Plan", "Test Cases", "Coverage Matrix", "Test Scenarios"))
                .fields(Arrays.asList(
                    InputField.text("jiraId", "Jira ID", "e.g. PS-12345", false),
                    InputField.textarea("featureDescription", "Feature / Story Description", "Describe the feature or paste the story description...", false),
                    InputField.textarea("acceptanceCriteria", "Acceptance Criteria", "List the acceptance criteria to derive test cases from...", false),
                    InputField.select("testingScope", "Testing Scope", Arrays.asList(
                        "Happy Path Only", "Happy Path + Edge Cases", "Full Coverage",
                        "Regression Focus", "Integration Focus"
                    ), false)
                ))
                .build(),

            AgentDefinition.builder()
                .id("automated-test-generator")
                .name("Automated Test Generator")
                .category("Testing")
                .description("Converts manual test cases into automated test scripts and generates automation framework recommendations.")
                .icon("🤖")
                .phase("Testing")
                .placeholder(true)
                .outputs(Arrays.asList("Automated Test Design", "Framework Recommendation", "Test Scripts"))
                .fields(Arrays.asList(
                    InputField.textarea("testCases", "Manual Test Cases", "Paste the manual test cases to automate...", true),
                    InputField.select("framework", "Test Framework", Arrays.asList("Selenium", "Playwright", "Cypress", "RestAssured"), false)
                ))
                .build(),

            AgentDefinition.builder()
                .id("risk-assessment")
                .name("Risk Assessment Agent")
                .category("Testing")
                .description("Identify technical, operational, and business risks for a feature or change with mitigation strategies.")
                .icon("⚠️")
                .phase("Planning")
                .placeholder(false)
                .outputs(Arrays.asList("Risk Register", "Mitigation Strategies", "Risk Score", "Contingency Plans"))
                .fields(Arrays.asList(
                    InputField.text("jiraId", "Jira ID", "e.g. PS-12345", false),
                    InputField.text("prBranch", "PR / Branch", "e.g. PR-4521 or feature/PS-12345-my-feature", false),
                    InputField.textarea("changeDescription", "Change Description", "Describe the feature, change, or deployment being assessed...", true),
                    InputField.textarea("scope", "Scope of Change", "What modules, services, or systems are affected?", false),
                    InputField.select("changeType", "Change Type", Arrays.asList(
                        "New Feature", "Defect Fix", "Refactoring", "Infrastructure Change",
                        "Database Migration", "Third-party Integration", "Configuration Change"
                    ), false)
                ))
                .build(),

            // ===================== Environment & Release =====================

            AgentDefinition.builder()
                .id("environment-provisioning")
                .name("Environment Provisioning Agent")
                .category("Environment & Release")
                .description("Generates step-by-step GCP environment provisioning plan, Helm/Kubernetes config checklist, and service deployment runbook.")
                .icon("☁️")
                .phase("Environment")
                .placeholder(false)
                .outputs(Arrays.asList("Provisioning Runbook", "GCP Resource Checklist", "Helm Config Guide", "Smoke Test Plan"))
                .fields(Arrays.asList(
                    InputField.text("envName", "Environment Name", "e.g. dev-ps-12345", true),
                    InputField.select("envType", "Environment Type", Arrays.asList(
                        "Dev", "Test / QA", "Staging", "Demo / UAT", "Production"
                    ), true),
                    InputField.text("releaseVersion", "Release Version / Branch", "e.g. 2024.3.1 or feature/PS-12345", false),
                    InputField.select("services", "Services to Deploy", Arrays.asList(
                        "All (telestaff-web + activiti-rest + auctions-web)",
                        "telestaff-web only",
                        "telestaff-activiti-rest only",
                        "auctions-web only",
                        "telestaff-web + telestaff-activiti-rest"
                    ), false),
                    InputField.text("gcpProject", "GCP Project ID", "e.g. ukg-telestaff-dev", false),
                    InputField.select("region", "GCP Region", Arrays.asList(
                        "us-central1", "us-east1", "us-west1", "europe-west1", "asia-southeast1"
                    ), false),
                    InputField.select("dbSize", "Cloud SQL Instance Tier", Arrays.asList(
                        "db-f1-micro (dev/test)", "db-g1-small (staging)", "db-n1-standard-2 (demo)", "db-n1-standard-4 (prod)"
                    ), false),
                    InputField.textarea("specialConfig", "Special Configuration / Notes", "Tenant setup, feature flags to enable, custom system.properties entries, SAML config, Redis config...", false)
                ))
                .build(),

            AgentDefinition.builder()
                .id("release-deployment")
                .name("Release Deployment Agent")
                .category("Environment & Release")
                .description("Generate a comprehensive deployment plan with release checklist, rollback procedures, and go/no-go criteria.")
                .icon("🚀")
                .phase("Release")
                .placeholder(false)
                .outputs(Arrays.asList("Deployment Plan", "Release Checklist", "Rollback Plan", "Go/No-Go Criteria"))
                .fields(Arrays.asList(
                    InputField.text("releaseVersion", "Release Version", "e.g. 2024.3.1", true),
                    InputField.textarea("features", "Features / Changes in Release", "List features, fixes, and changes included in this release...", true),
                    InputField.textarea("dbChanges", "Database Changes", "Describe any Liquibase changesets or DB migrations...", false),
                    InputField.select("deploymentType", "Deployment Type", Arrays.asList(
                        "Rolling Deploy", "Blue/Green", "Canary", "Full Cutover", "Hotfix"
                    ), false),
                    InputField.textarea("rollbackPlan", "Rollback Triggers", "What conditions should trigger a rollback?", false)
                ))
                .build(),

            AgentDefinition.builder()
                .id("release-notes")
                .name("Release Notes Generator")
                .category("Environment & Release")
                .description("Generate professional release notes for customers, internal teams, and executives from Jira tickets and change descriptions.")
                .icon("📄")
                .phase("Release")
                .placeholder(false)
                .outputs(Arrays.asList("Customer Release Notes", "Internal Release Notes", "Executive Summary"))
                .fields(Arrays.asList(
                    InputField.text("releaseVersion", "Release Version", "e.g. 2024.3.1", true),
                    InputField.textarea("jiraTickets", "Jira Tickets / Changes", "List Jira IDs and brief descriptions (one per line)...", true),
                    InputField.select("audience", "Primary Audience", Arrays.asList(
                        "Customers", "Internal Engineering", "Executive / Leadership", "All Audiences"
                    ), false),
                    InputField.text("releaseDate", "Release Date", "e.g. 2024-12-15", false)
                ))
                .build(),

            AgentDefinition.builder()
                .id("sprint-readiness")
                .name("Sprint Readiness Agent")
                .category("Environment & Release")
                .description("Evaluate sprint readiness by analyzing stories for completeness, dependencies, risks, and capacity alignment.")
                .icon("📊")
                .phase("Planning")
                .placeholder(false)
                .outputs(Arrays.asList("Readiness Score", "Story Analysis", "Risk Flags", "Recommendations"))
                .fields(Arrays.asList(
                    InputField.text("sprintName", "Sprint Name", "e.g. Sprint 2024-12", true),
                    InputField.textarea("stories", "Sprint Stories", "List the Jira IDs and story descriptions (one per line)...", true),
                    InputField.text("teamCapacity", "Team Capacity (Story Points)", "e.g. 40", false),
                    InputField.textarea("blockers", "Known Blockers / Risks", "List any known blockers or risks...", false)
                ))
                .build(),

            AgentDefinition.builder()
                .id("production-incident")
                .name("Production Incident Summarizer")
                .category("Environment & Release")
                .description("Produce a structured incident report with timeline, impact assessment, RCA summary, and action items.")
                .icon("🚨")
                .phase("Operations")
                .placeholder(false)
                .outputs(Arrays.asList("Incident Report", "Timeline", "Impact Assessment", "Action Items"))
                .fields(Arrays.asList(
                    InputField.text("incidentId", "Incident ID / Ticket", "e.g. INC-12345", false),
                    InputField.textarea("description", "Incident Description", "What happened? When? What was the impact?", true),
                    InputField.textarea("timeline", "Timeline of Events", "Chronological events with timestamps...", false),
                    InputField.textarea("resolution", "Resolution / Workaround", "How was it resolved or mitigated?", false),
                    InputField.select("severity", "Severity", Arrays.asList("P0 - Critical", "P1 - High", "P2 - Medium", "P3 - Low"), false)
                ))
                .build()
        );
    }
}
