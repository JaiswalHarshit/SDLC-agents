package com.ukg.telestaff.sdlc.service;

import com.ukg.telestaff.sdlc.model.AgentDefinition;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AgentRegistryService {

    private final Map<String, AgentDefinition> agentMap;
    private final List<AgentDefinition> allAgents;

    public AgentRegistryService(List<AgentDefinition> agentDefinitions) {
        this.allAgents = agentDefinitions;
        this.agentMap = agentDefinitions.stream()
                .collect(Collectors.toMap(AgentDefinition::getId, a -> a,
                        (a1, a2) -> a1, LinkedHashMap::new));
    }

    public List<AgentDefinition> getAllAgents() {
        return allAgents;
    }

    public Optional<AgentDefinition> findById(String id) {
        return Optional.ofNullable(agentMap.get(id));
    }

    public Map<String, List<AgentDefinition>> getAgentsByCategory() {
        return allAgents.stream()
                .collect(Collectors.groupingBy(AgentDefinition::getCategory,
                        LinkedHashMap::new, Collectors.toList()));
    }

    public List<AgentDefinition> findByCategory(String category) {
        return allAgents.stream()
                .filter(a -> a.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public List<AgentDefinition> search(String query) {
        if (query == null || query.isBlank()) return allAgents;
        String q = query.toLowerCase();
        return allAgents.stream()
                .filter(a -> a.getName().toLowerCase().contains(q)
                        || a.getDescription().toLowerCase().contains(q)
                        || a.getCategory().toLowerCase().contains(q)
                        || a.getPhase().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public long countActive() {
        return allAgents.stream().filter(AgentDefinition::isActive).count();
    }

    public long countPlaceholder() {
        return allAgents.stream().filter(AgentDefinition::isPlaceholder).count();
    }
}
