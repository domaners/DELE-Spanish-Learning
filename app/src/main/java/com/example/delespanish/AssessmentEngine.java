package com.example.delespanish;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AssessmentEngine {
    public AssessmentResult evaluate(List<QuizQuestion> questions, Map<QuizQuestion, Integer> answers) {
        Map<DeleLevel, Integer> attempts = new HashMap<>();
        Map<DeleLevel, Integer> correctByLevel = new HashMap<>();
        int correctAnswers = 0;

        for (QuizQuestion question : questions) {
            DeleLevel level = question.getLevel();
            attempts.put(level, attempts.getOrDefault(level, 0) + 1);
            Integer selectedAnswer = answers.get(question);
            if (selectedAnswer != null && question.isCorrect(selectedAnswer)) {
                correctAnswers++;
                correctByLevel.put(level, correctByLevel.getOrDefault(level, 0) + 1);
            }
        }

        DeleLevel suggestedLevel = recommendLevel(attempts, correctByLevel);
        return new AssessmentResult(
                correctAnswers,
                questions.size(),
                suggestedLevel,
                buildMessage(suggestedLevel, correctAnswers, questions.size())
        );
    }

    private DeleLevel recommendLevel(Map<DeleLevel, Integer> attempts, Map<DeleLevel, Integer> correctByLevel) {
        DeleLevel recommended = DeleLevel.A1;
        for (DeleLevel level : DeleLevel.values()) {
            int attempted = attempts.getOrDefault(level, 0);
            if (attempted == 0) {
                continue;
            }
            double score = (double) correctByLevel.getOrDefault(level, 0) / attempted;
            if (score >= 0.7) {
                recommended = level.next();
            } else {
                return level;
            }
        }
        return recommended;
    }

    private String buildMessage(DeleLevel level, int correctAnswers, int totalQuestions) {
        int percent = totalQuestions == 0 ? 0 : Math.round((correctAnswers * 100f) / totalQuestions);
        return "You scored " + percent + "%. Focus next on " + level.getLabel()
                + ": " + level.getCanDoStatement();
    }

    public static final class AssessmentResult {
        private final int correctAnswers;
        private final int totalQuestions;
        private final DeleLevel suggestedLevel;
        private final String message;

        AssessmentResult(int correctAnswers, int totalQuestions, DeleLevel suggestedLevel, String message) {
            this.correctAnswers = correctAnswers;
            this.totalQuestions = totalQuestions;
            this.suggestedLevel = suggestedLevel;
            this.message = message;
        }

        public int getCorrectAnswers() {
            return correctAnswers;
        }

        public int getTotalQuestions() {
            return totalQuestions;
        }

        public DeleLevel getSuggestedLevel() {
            return suggestedLevel;
        }

        public String getMessage() {
            return message;
        }
    }
}
