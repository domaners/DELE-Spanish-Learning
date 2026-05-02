package com.example.delespanish;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssessmentEngineTest {
    @Test
    public void placementSuggestsFirstWeakLevel() {
        AssessmentEngine engine = new AssessmentEngine();
        List<QuizQuestion> questions = Arrays.asList(
                question(DeleLevel.A1),
                question(DeleLevel.A1),
                question(DeleLevel.A2),
                question(DeleLevel.A2),
                question(DeleLevel.B1),
                question(DeleLevel.B1)
        );
        Map<QuizQuestion, Integer> answers = new HashMap<>();

        for (QuizQuestion question : questions) {
            if (question.getLevel().ordinal() <= DeleLevel.A2.ordinal()) {
                answers.put(question, question.getCorrectAnswerIndex());
            } else {
                answers.put(question, -1);
            }
        }

        AssessmentEngine.AssessmentResult result = engine.evaluate(questions, answers);

        Assert.assertEquals(4, result.getCorrectAnswers());
        Assert.assertEquals(6, result.getTotalQuestions());
        Assert.assertEquals(DeleLevel.B1, result.getSuggestedLevel());
        Assert.assertTrue(result.getMessage().contains("B1"));
    }

    @Test
    public void unansweredQuestionsFallBackToEntryLevel() {
        AssessmentEngine engine = new AssessmentEngine();

        AssessmentEngine.AssessmentResult result = engine.evaluate(
                Arrays.asList(question(DeleLevel.A1), question(DeleLevel.A2)),
                new HashMap<>()
        );

        Assert.assertEquals(DeleLevel.A1, result.getSuggestedLevel());
    }

    private QuizQuestion question(DeleLevel level) {
        return new QuizQuestion(
                level,
                "Sample prompt",
                Arrays.asList("correct", "incorrect", "also incorrect", "still incorrect"),
                0
        );
    }
}
