package com.ukg.telestaff.sdlc.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class AgentDefinition {
    String id;
    String name;
    String category;
    String description;
    String icon;
    String phase;
    boolean placeholder;
    List<String> outputs;
    List<InputField> fields;

    public boolean isActive() {
        return !placeholder;
    }
}
