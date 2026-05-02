package com.example.delespanish;

enum DeleLevel {
    A1(
            "A1",
            "Breakthrough",
            "Use familiar expressions, basic introductions, and simple present-tense statements.",
            "Personal information, classroom language, present tense, gender and number agreement."
    ),
    A2(
            "A2",
            "Waystage",
            "Handle routine exchanges, describe daily life, and talk about simple past experiences.",
            "Daily routines, shopping, appointments, present tense, near future, and common preterite forms."
    ),
    B1(
            "B1",
            "Threshold",
            "Manage travel situations, narrate events, and justify opinions in familiar contexts.",
            "Past narration, connectors, travel problems, descriptions, opinions, and familiar correspondence."
    ),
    B2(
            "B2",
            "Vantage",
            "Discuss abstract topics, defend arguments, and understand detailed texts.",
            "Argumentation, abstract vocabulary, subjunctive triggers, contrast, concession, and register."
    ),
    C1(
            "C1",
            "Effective operational proficiency",
            "Use nuanced structures flexibly for academic, professional, and social aims.",
            "Formal discourse, impersonal structures, passive voice, cohesion, inference, and precise register."
    ),
    C2(
            "C2",
            "Mastery",
            "Interpret subtle meaning, idioms, and complex discourse with near-native precision.",
            "Idioms, rhetorical stance, irony, literary nuance, advanced paraphrase, and tone control."
    );

    private final String code;
    private final String title;
    private final String canDoStatement;
    private final String examFocus;

    DeleLevel(String code, String title, String canDoStatement, String examFocus) {
        this.code = code;
        this.title = title;
        this.canDoStatement = canDoStatement;
        this.examFocus = examFocus;
    }

    String getLabel() {
        return code + " - " + title;
    }

    String getTitle() {
        return title;
    }

    String getCode() {
        return code;
    }

    String getCanDoStatement() {
        return canDoStatement;
    }

    String getExamFocus() {
        return examFocus;
    }

    DeleLevel next() {
        int nextOrdinal = Math.min(values().length - 1, ordinal() + 1);
        return values()[nextOrdinal];
    }
}
