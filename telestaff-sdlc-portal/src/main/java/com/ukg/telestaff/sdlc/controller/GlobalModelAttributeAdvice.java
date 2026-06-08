package com.ukg.telestaff.sdlc.controller;

import com.ukg.telestaff.sdlc.config.DemoModeProperties;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Injects global model attributes into every Thymeleaf template rendered by
 * any {@code @Controller} in this application — no per-controller wiring needed.
 *
 * <p>Attributes added here:
 * <ul>
 *   <li>{@code demoMode} — {@code true} when the app is started with
 *       {@code --app.demo-mode=true}; drives the amber demo banner in the
 *       navbar fragment.</li>
 * </ul>
 */
@ControllerAdvice
public class GlobalModelAttributeAdvice {

    private final DemoModeProperties demoModeProperties;

    public GlobalModelAttributeAdvice(DemoModeProperties demoModeProperties) {
        this.demoModeProperties = demoModeProperties;
    }

    @ModelAttribute("demoMode")
    public boolean demoMode() {
        return demoModeProperties.isDemoMode();
    }
}
