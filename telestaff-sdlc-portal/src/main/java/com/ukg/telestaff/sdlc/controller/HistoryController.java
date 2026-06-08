package com.ukg.telestaff.sdlc.controller;

import com.ukg.telestaff.sdlc.model.ExecutionStatus;
import com.ukg.telestaff.sdlc.service.ExecutionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/history")
public class HistoryController {

    private final ExecutionService executionService;

    public HistoryController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @GetMapping
    public String history(Model model) {
        model.addAttribute("executions", executionService.getAllExecutions());
        model.addAttribute("totalCount", executionService.totalExecutions());
        model.addAttribute("completedCount", executionService.countByStatus(ExecutionStatus.COMPLETED));
        model.addAttribute("failedCount", executionService.countByStatus(ExecutionStatus.FAILED));
        model.addAttribute("runningCount", executionService.countByStatus(ExecutionStatus.RUNNING));
        return "history";
    }
}
