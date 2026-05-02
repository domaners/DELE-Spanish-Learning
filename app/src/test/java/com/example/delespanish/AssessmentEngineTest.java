package com.example.delespanish;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssessmentEngineTest {
    @Test
    public void placementSuggestsFirstWeakLevel() {
        LearningRepository repository = new LearningRepository();
        AssessmentEngine engine = new AssessmentEngine();
        List<QuizQuestion> questions = repository.getPlacementQuestions();
        Map<QuizQuestion, Integer> answers = new HashMap<>();

        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion question = questions.get(i);
            if (question.getLevel().ordinal() <= DeleLevel.A2.ordinal()) {
                answers.put(question, question.getCorrectAnswerIndex());
            } else {
                answers.put(question, -1);
            }
        }

        AssessmentEngine.AssessmentResult result = engine.evaluate(questions, answers);

        Assert.assertEquals(4, result.getCorrectAnswers());
        Assert.assertEquals(12, result.getTotalQuestions());
        Assert.assertEquals(DeleLevel.B1, result.getSuggestedLevel());
        Assert.assertTrue(result.getMessage().contains("B1"));
    }

    @Test
    public void unansweredQuestionsFallBackToEntryLevel() {
        LearningRepository repository = new LearningRepository();
        AssessmentEngine engine = new AssessmentEngine();

        AssessmentEngine.AssessmentResult result = engine.evaluate(
                repository.getPlacementQuestions(),
                new HashMap<>()
        );

        Assert.assertEquals(DeleLevel.A1, result.getSuggestedLevel());
    }

    @Test
    public void dailyQuestionsMixPreviousIncorrectItemsWithNewItems() {
        LearningRepository repository = new LearningRepository();
        List<QuizQuestion> available = repository.getDailyQuestions(DeleLevel.B1);
        List<String> incorrectIds = java.util.Arrays.asList(
                available.get(2).getId(),
                available.get(3).getId()
        );

        List<QuizQuestion> selected = repository.getDailyQuestions(DeleLevel.B1, incorrectIds, 4);

        Assert.assertEquals(4, selected.size());
        Assert.assertEquals(available.get(2), selected.get(0));
        Assert.assertEquals(available.get(3), selected.get(1));
        Assert.assertEquals(available.get(0), selected.get(2));
        Assert.assertEquals(available.get(1), selected.get(3));
    }

    @Test
    public void maxDailyQuestionsCanBeZero() {
        LearningRepository repository = new LearningRepository();

        List<QuizQuestion> selected = repository.getDailyQuestions(
                DeleLevel.B1,
                java.util.Collections.singletonList(repository.getDailyQuestions(DeleLevel.B1).get(0).getId()),
                0
        );

        Assert.assertTrue(selected.isEmpty());
    }
}
