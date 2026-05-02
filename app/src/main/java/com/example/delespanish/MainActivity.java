package com.example.delespanish;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    private static final String PREFS = "dele_spanish_progress";
    private static final String KEY_PLACEMENT_COMPLETE = "placementComplete";
    private static final String KEY_TARGET_LEVEL = "targetLevel";
    private static final String KEY_DAILY_SCORE = "dailyScore";

    private LearningRepository repository;
    private AssessmentEngine assessmentEngine;
    private SharedPreferences preferences;
    private LinearLayout root;
    private final Map<QuizQuestion, Integer> selectedAnswers = new java.util.HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new LearningRepository();
        assessmentEngine = new AssessmentEngine();
        preferences = getSharedPreferences(PREFS, MODE_PRIVATE);

        ScrollView scrollView = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(24));
        root.setBackgroundResource(R.drawable.app_background);
        scrollView.addView(root);
        setContentView(scrollView);

        if (preferences.getBoolean(KEY_PLACEMENT_COMPLETE, false)) {
            showHome();
        } else {
            showPlacement();
        }
    }

    private void showPlacement() {
        root.removeAllViews();
        selectedAnswers.clear();
        addTitle("Spanish DELE Coach");
        addBody("Start with a short adaptive placement. Your result suggests the DELE exam level to aim for and unlocks daily review themes.");

        List<QuizQuestion> questions = repository.getPlacementQuestions();
        for (QuizQuestion question : questions) {
            addQuestion(question);
        }

        Button submit = addButton("Get my DELE recommendation");
        submit.setOnClickListener(view -> {
            if (selectedAnswers.size() < questions.size()) {
                Toast.makeText(this, "Answer every placement question first.", Toast.LENGTH_SHORT).show();
                return;
            }
            AssessmentEngine.AssessmentResult result = assessmentEngine.evaluate(questions, selectedAnswers);
            preferences.edit()
                    .putBoolean(KEY_PLACEMENT_COMPLETE, true)
                    .putString(KEY_TARGET_LEVEL, result.getSuggestedLevel().name())
                    .putInt(KEY_DAILY_SCORE, result.getCorrectAnswers())
                    .apply();
            showPlacementResult(result);
        });
    }

    private void showPlacementResult(AssessmentEngine.AssessmentResult result) {
        root.removeAllViews();
        addTitle("Recommended target: " + result.getSuggestedLevel().getLabel());
        addBody("Placement score: " + result.getCorrectAnswers() + "/" + result.getTotalQuestions()
                + ". " + result.getMessage());
        addLevelSummary(result.getSuggestedLevel());
        Button continueButton = addButton("Start learning");
        continueButton.setOnClickListener(view -> showHome());
    }

    private void showHome() {
        root.removeAllViews();
        DeleLevel level = getTargetLevel();
        addTitle("Today in Spanish");
        addBody("Target exam: " + level.getLabel() + "\n"
                + level.getExamFocus() + "\n\n"
                + "Latest daily quiz score: " + preferences.getInt(KEY_DAILY_SCORE, 0));

        addLevelSummary(level);
        addButton("Daily quiz").setOnClickListener(view -> showDailyQuiz(level));
        addButton("Grammar articles").setOnClickListener(view -> showArticles(level));
        addButton("Dictionary and verbs").setOnClickListener(view -> showDictionary(level));
        addButton("Retake placement").setOnClickListener(view -> showPlacement());
    }

    private void showDailyQuiz(DeleLevel targetLevel) {
        root.removeAllViews();
        selectedAnswers.clear();
        addTitle("Daily consolidation quiz");
        addBody("Questions are selected up to " + targetLevel.getLabel()
                + " so review grows with your DELE target.");

        List<QuizQuestion> questions = repository.getDailyQuestions(targetLevel);
        for (QuizQuestion question : questions) {
            addQuestion(question);
        }

        addButton("Score daily quiz").setOnClickListener(view -> {
            if (selectedAnswers.size() < questions.size()) {
                Toast.makeText(this, "Complete every daily question first.", Toast.LENGTH_SHORT).show();
                return;
            }
            AssessmentEngine.AssessmentResult result = assessmentEngine.evaluate(questions, selectedAnswers);
            preferences.edit()
                    .putString(KEY_TARGET_LEVEL, result.getSuggestedLevel().name())
                    .putInt(KEY_DAILY_SCORE, result.getCorrectAnswers())
                    .apply();
            showQuizResult(result);
        });
        addBackHomeButton();
    }

    private void showQuizResult(AssessmentEngine.AssessmentResult result) {
        root.removeAllViews();
        addTitle("Daily score: " + result.getCorrectAnswers() + "/" + result.getTotalQuestions());
        addBody(result.getMessage() + "\n\nUpdated recommendation: " + result.getSuggestedLevel().getLabel());
        addLevelSummary(result.getSuggestedLevel());
        addButton("Review suggested articles").setOnClickListener(view -> showArticles(result.getSuggestedLevel()));
        addBackHomeButton();
    }

    private void showArticles(DeleLevel targetLevel) {
        root.removeAllViews();
        addTitle("Grammar articles");
        addBody("DELE-aligned explanations are grouped from A1 to " + targetLevel.getLabel() + ".");
        for (Article article : repository.getArticlesUpTo(targetLevel)) {
            LinearLayout card = addCard();
            addCardTitle(card, article.getLevel().getLabel() + " - " + article.getTitle());
            addCardBody(card, article.getGrammarFocus());
            addCardBody(card, article.getBody());
            addCardBody(card, "Key vocabulary: " + TextUtils.join(", ", article.getVocabulary()));
        }
        addBackHomeButton();
    }

    private void showDictionary(DeleLevel targetLevel) {
        root.removeAllViews();
        addTitle("Dictionary and conjugations");
        addBody("Vocabulary and verb forms are tagged by the DELE level where they become most useful.");

        addSectionHeader("Vocabulary");
        for (VocabularyEntry entry : repository.getVocabularyUpTo(targetLevel)) {
            LinearLayout card = addCard();
            addCardTitle(card, entry.getSpanish() + " - " + entry.getEnglish());
            addCardBody(card, entry.getLevel().getLabel() + " | " + entry.getTheme());
            addCardBody(card, entry.getExample());
        }

        addSectionHeader("Verb conjugations");
        for (VerbConjugation verb : repository.getVerbsUpTo(targetLevel)) {
            LinearLayout card = addCard();
            addCardTitle(card, verb.getInfinitive() + " - " + verb.getMeaning());
            addCardBody(card, verb.getLevel().getLabel());
            addCardBody(card, verb.describeForms());
        }
        addBackHomeButton();
    }

    private void addQuestion(QuizQuestion question) {
        LinearLayout card = addCard();
        addCardTitle(card, question.getLevel().getLabel() + " - " + question.getPrompt());
        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.VERTICAL);
        List<String> options = question.getOptions();
        for (int i = 0; i < options.size(); i++) {
            RadioButton option = new RadioButton(this);
            option.setText(options.get(i));
            option.setTextSize(16);
            option.setId(View.generateViewId());
            option.setTag(i);
            group.addView(option);
        }
        group.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            RadioButton checked = radioGroup.findViewById(checkedId);
            if (checked != null) {
                selectedAnswers.put(question, (Integer) checked.getTag());
            }
        });
        card.addView(group);
    }

    private void addLevelSummary(DeleLevel level) {
        LinearLayout card = addCard();
        addCardTitle(card, level.getLabel() + " readiness path");
        addCardBody(card, level.getCanDoStatement());
        addCardBody(card, "Exam focus: " + level.getExamFocus());
    }

    private void addTitle(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(30);
        view.setTypeface(Typeface.DEFAULT_BOLD);
        view.setTextColor(getColor(R.color.text_primary));
        view.setPadding(0, 0, 0, dp(12));
        root.addView(view);
    }

    private void addSectionHeader(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(22);
        view.setTypeface(Typeface.DEFAULT_BOLD);
        view.setTextColor(getColor(R.color.text_primary));
        view.setPadding(0, dp(20), 0, dp(8));
        root.addView(view);
    }

    private void addBody(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(16);
        view.setLineSpacing(4, 1.0f);
        view.setTextColor(getColor(R.color.text_secondary));
        view.setPadding(0, 0, 0, dp(16));
        root.addView(view);
    }

    private Button addButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(8), 0, dp(8));
        root.addView(button, params);
        return button;
    }

    private void addBackHomeButton() {
        Button button = addButton("Back to dashboard");
        button.setOnClickListener(view -> showHome());
    }

    private LinearLayout addCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackgroundResource(R.drawable.card_background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(8), 0, dp(8));
        root.addView(card, params);
        return card;
    }

    private void addCardTitle(LinearLayout card, String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(18);
        view.setTypeface(Typeface.DEFAULT_BOLD);
        view.setTextColor(getColor(R.color.text_primary));
        view.setPadding(0, 0, 0, dp(6));
        card.addView(view);
    }

    private void addCardBody(LinearLayout card, String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(15);
        view.setLineSpacing(4, 1.0f);
        view.setTextColor(getColor(R.color.text_secondary));
        view.setPadding(0, 0, 0, dp(8));
        card.addView(view);
    }

    private DeleLevel getTargetLevel() {
        String value = preferences.getString(KEY_TARGET_LEVEL, DeleLevel.A1.name());
        return DeleLevel.valueOf(value);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
