package io.beanguard.server.services.impl;

import java.util.Map;

class TemplateRenderer {

    static String render(String template, Map<String, String> vars) {
        for (var entry : vars.entrySet()) {
            template = template.replace(
                    "{{" + entry.getKey() + "}}",
                    entry.getValue() != null ? entry.getValue() : "");
        }
        return template;
    }
}
