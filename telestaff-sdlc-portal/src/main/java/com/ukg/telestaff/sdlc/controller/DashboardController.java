package com.ukg.telestaff.sdlc.controller;

import com.ukg.telestaff.sdlc.model.ExecutionStatus;
import com.ukg.telestaff.sdlc.service.AgentRegistryService;
import com.ukg.telestaff.sdlc.service.ExecutionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardController {

    private final AgentRegistryService registryService;
    private final ExecutionService executionService;

    public DashboardController(AgentRegistryService registryService, ExecutionService executionService) {
        this.registryService = registryService;
        this.executionService = executionService;
    }

    @GetMapping("/")
    public String dashboard(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            Model model) {

        var allAgents = (q != null && !q.isBlank()) ? registryService.search(q) : registryService.getAllAgents();

        var agentsByCategory = allAgents.stream()
                .filter(a -> category == null || category.isBlank() || a.getCategory().equals(category))
                .collect(java.util.stream.Collectors.groupingBy(
                        com.ukg.telestaff.sdlc.model.AgentDefinition::getCategory,
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));

        model.addAttribute("agentsByCategory", agentsByCategory);
        model.addAttribute("allCategories", registryService.getAgentsByCategory().keySet());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("searchQuery", q);
        model.addAttribute("totalAgents", registryService.getAllAgents().size());
        model.addAttribute("activeAgents", registryService.countActive());
        model.addAttribute("recentExecutions", executionService.getRecentExecutions(5));
        model.addAttribute("totalExecutions", executionService.totalExecutions());
        model.addAttribute("completedCount", executionService.countByStatus(ExecutionStatus.COMPLETED));
        model.addAttribute("runningCount", executionService.countByStatus(ExecutionStatus.RUNNING));

        return "dashboard";
    }
}
