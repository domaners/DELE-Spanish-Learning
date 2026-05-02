package com.example.delespanish;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class QuizQuestion {
    private final String id;
    private final DeleLevel level;
    private final String prompt;
    private final List<String> options;
    private final int correctAnswerIndex;

    QuizQuestion(DeleLevel level, String prompt, List<String> options, int correctAnswerIndex) {
        this.id = level.name() + ":" + prompt;
        this.level = level;
        this.prompt = prompt;
        this.options = Collections.unmodifiableList(new ArrayList<>(options));
        this.correctAnswerIndex = correctAnswerIndex;
    }

    String getId() {
        return id;
    }

    DeleLevel getLevel() {
        return level;
    }

    String getPrompt() {
        return prompt;
    }

    List<String> getOptions() {
        return options;
    }

    int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    boolean isCorrect(Integer answerIndex) {
        return answerIndex != null && answerIndex == correctAnswerIndex;
    }
}
