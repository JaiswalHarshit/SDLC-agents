package com.ukg.telestaff.sdlc.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class InputField {
    String id;
    String label;
    FieldType type;
    String placeholder;
    boolean required;
    String helpText;
    List<String> options;

    public static InputField text(String id, String label, String placeholder, boolean required) {
        return InputField.builder()
                .id(id).label(label).type(FieldType.TEXT)
                .placeholder(placeholder).required(required).build();
    }

    public static InputField textarea(String id, String label, String placeholder, boolean required) {
        return InputField.builder()
                .id(id).label(label).type(FieldType.TEXTAREA)
                .placeholder(placeholder).required(required).build();
    }

    public static InputField select(String id, String label, List<String> options, boolean required) {
        return InputField.builder()
                .id(id).label(label).type(FieldType.SELECT)
                .options(options).required(required).build();
    }

    public static InputField markdown(String id, String label, String placeholder, boolean required) {
        return InputField.builder()
                .id(id).label(label).type(FieldType.MARKDOWN)
                .placeholder(placeholder).required(required).build();
    }
}
