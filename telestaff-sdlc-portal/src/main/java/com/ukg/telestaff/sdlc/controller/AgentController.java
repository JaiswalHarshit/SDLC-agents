package com.ukg.telestaff.sdlc.controller;

import com.ukg.telestaff.sdlc.agent.AgentExecutor;
import com.ukg.telestaff.sdlc.model.AgentDefinition;
import com.ukg.telestaff.sdlc.model.AgentExecution;
import com.ukg.telestaff.sdlc.service.AgentRegistryService;
import com.ukg.telestaff.sdlc.service.ExecutionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/agents")
public class AgentController {

    private final AgentRegistryService registryService;
    private final ExecutionService executionService;
    private final AgentExecutor agentExecutor;

    public AgentController(AgentRegistryService registryService,
                           ExecutionService executionService,
                           AgentExecutor agentExecutor) {
        this.registryService = registryService;
        this.executionService = executionService;
        this.agentExecutor = agentExecutor;
    }

    @GetMapping("/{agentId}")
    public String agentForm(@PathVariable String agentId, Model model) {
        AgentDefinition agent = registryService.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));

        model.addAttribute("agent", agent);
        model.addAttribute("recentExecutions", executionService.getRecentExecutions(3));
        return "agent-form";
    }

    @PostMapping("/{agentId}/execute")
    public String execute(@PathVariable String agentId,
                          HttpServletRequest request,
                          RedirectAttributes redirectAttrs) {

        AgentDefinition agent = registryService.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));

        Map<String, String> inputs = extractInputs(request, agent);

        AgentExecution execution = executionService.createExecution(agent, inputs);

        agentExecutor.execute(agent, execution, inputs);

        return "redirect:/executions/" + execution.getExecutionId();
    }

    private Map<String, String> extractInputs(HttpServletRequest request, AgentDefinition agent) {
        Map<String, String> inputs = new HashMap<>();
        agent.getFields().forEach(field -> {
            String value = request.getParameter(field.getId());
            if (value != null && !value.isBlank()) {
                inputs.put(field.getId(), value.trim());
            }
        });
        return inputs;
    }
}
