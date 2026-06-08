package com.ukg.telestaff.sdlc.service;

import com.ukg.telestaff.sdlc.model.AgentDefinition;
import com.ukg.telestaff.sdlc.model.AgentExecution;
import com.ukg.telestaff.sdlc.model.ExecutionStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ExecutionService {

    private static final int MAX_EXECUTIONS = 50;

    private final ConcurrentHashMap<String, AgentExecution> executions = new ConcurrentHashMap<>();

    public AgentExecution createExecution(AgentDefinition agent, Map<String, String> inputs) {
        String executionId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        AgentExecution execution = AgentExecution.builder()
                .executionId(executionId)
                .agentId(agent.getId())
                .agentName(agent.getName())
                .inputs(inputs)
                .status(ExecutionStatus.PENDING)
                .build();
        executions.put(executionId, execution);
        trimIfNeeded();
        return execution;
    }

    public Optional<AgentExecution> findById(String executionId) {
        return Optional.ofNullable(executions.get(executionId));
    }

    public List<AgentExecution> getRecentExecutions(int limit) {
        return executions.values().stream()
                .sorted(Comparator.comparing(AgentExecution::getStartedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<AgentExecution> getAllExecutions() {
        return getRecentExecutions(MAX_EXECUTIONS);
    }

    public long countByStatus(ExecutionStatus status) {
        return executions.values().stream()
                .filter(e -> e.getStatus() == status)
                .count();
    }

    public long totalExecutions() {
        return executions.size();
    }

    private void trimIfNeeded() {
        if (executions.size() > MAX_EXECUTIONS) {
            executions.values().stream()
                    .sorted(Comparator.comparing(AgentExecution::getStartedAt))
                    .limit(executions.size() - MAX_EXECUTIONS)
                    .forEach(e -> executions.remove(e.getExecutionId()));
        }
    }
}
