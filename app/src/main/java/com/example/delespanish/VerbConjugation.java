package com.example.delespanish;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class VerbConjugation {
    private final DeleLevel level;
    private final String infinitive;
    private final String tense;
    private final String meaning;
    private final Map<String, String> forms;

    public VerbConjugation(
            DeleLevel level,
            String infinitive,
            String tense,
            String meaning,
            Map<String, String> forms
    ) {
        this.level = level;
        this.infinitive = infinitive;
        this.tense = tense;
        this.meaning = meaning;
        this.forms = Collections.unmodifiableMap(new LinkedHashMap<>(forms));
    }

    public DeleLevel getLevel() {
        return level;
    }

    public String getInfinitive() {
        return infinitive;
    }

    public String getTense() {
        return tense;
    }

    public String getMeaning() {
        return meaning;
    }

    public Map<String, String> getForms() {
        return forms;
    }

    public String describeForms() {
        StringBuilder builder = new StringBuilder();
        builder.append(tense).append(": ");
        boolean first = true;
        for (Map.Entry<String, String> entry : forms.entrySet()) {
            if (!first) {
                builder.append(", ");
            }
            builder.append(entry.getKey()).append(" ").append(entry.getValue());
            first = false;
        }
        return builder.toString();
    }
}
