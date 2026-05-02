package com.example.delespanish;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {
    private static final String PREFS = "dele_spanish_progress";
    private static final String KEY_PLACEMENT_COMPLETE = "placementComplete";
    private static final String KEY_TARGET_LEVEL = "targetLevel";
    private static final String KEY_DAILY_SCORE = "dailyScore";
    private static final String KEY_QUESTION_COUNT = "questionCount";
    private static final String KEY_TEST_HISTORY = "testHistory";
    private static final int DEFAULT_QUESTION_COUNT = 10;

    private LearningRepository repository;
    private AssessmentEngine assessmentEngine;
    private SharedPreferences preferences;
    private LinearLayout root;
    private LinearLayout menuPanel;
    private View dimOverlay;
    private boolean menuVisible;
    private final Map<QuizQuestion, Integer> selectedAnswers = new java.util.HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new LearningRepository();
        assessmentEngine = new AssessmentEngine();
        preferences = getSharedPreferences(PREFS, MODE_PRIVATE);

        FrameLayout frame = new FrameLayout(this);
        ScrollView scrollView = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(24));
        root.setBackgroundResource(R.drawable.app_background);
        scrollView.addView(root);
        frame.addView(scrollView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        dimOverlay = new View(this);
        dimOverlay.setBackgroundColor(Color.argb(110, 0, 0, 0));
        dimOverlay.setVisibility(View.GONE);
        dimOverlay.setOnClickListener(view -> hideMenu());
        frame.addView(dimOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        menuPanel = new LinearLayout(this);
        menuPanel.setOrientation(LinearLayout.VERTICAL);
        menuPanel.setPadding(dp(20), dp(36), dp(20), dp(20));
        menuPanel.setBackgroundColor(getColor(R.color.surface));
        FrameLayout.LayoutParams menuParams = new FrameLayout.LayoutParams(
                getResources().getDisplayMetrics().widthPixels * 3 / 4,
                FrameLayout.LayoutParams.MATCH_PARENT);
        menuParams.gravity = Gravity.START;
        frame.addView(menuPanel, menuParams);
        buildMenu();
        menuPanel.post(() -> {
            if (!menuVisible) {
                menuPanel.setTranslationX(-menuPanel.getWidth());
            }
        });

        setContentView(frame);

        if (preferences.getBoolean(KEY_PLACEMENT_COMPLETE, false)) {
            showHome();
        } else {
            showPlacement();
        }
    }

    private void buildMenu() {
        menuPanel.removeAllViews();
        TextView title = new TextView(this);
        title.setText("Menu");
        title.setTextSize(26);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(getColor(R.color.text_primary));
        title.setPadding(0, 0, 0, dp(18));
        menuPanel.addView(title);

        addMenuButton("Dashboard", () -> showHome());
        addMenuButton("Daily quiz", () -> showDailyQuiz(getTargetLevel()));
        addMenuButton("Grammar articles", () -> showArticles(getTargetLevel()));
        addMenuButton("Dictionary and verbs", () -> showDictionary(getTargetLevel()));
        addMenuButton("Test history", () -> showTestHistory());
        addMenuButton("Settings", () -> showSettings());
        addMenuButton("Retake placement", () -> showPlacement());
    }

    private void addMenuButton(String text, Runnable action) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        button.setOnClickListener(view -> {
            hideMenu();
            action.run();
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(5), 0, dp(5));
        menuPanel.addView(button, params);
    }

    private void showMenu() {
        menuVisible = true;
        dimOverlay.setVisibility(View.VISIBLE);
        menuPanel.animate().translationX(0).setDuration(180).start();
    }

    private void hideMenu() {
        menuVisible = false;
        menuPanel.animate()
                .translationX(-menuPanel.getWidth())
                .setDuration(180)
                .withEndAction(() -> dimOverlay.setVisibility(View.GONE))
                .start();
    }

    private void resetScreen(String title) {
        hideMenu();
        root.removeAllViews();
        addMenuHeader(title);
    }

    private void addMenuHeader(String title) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(12));

        Button menuButton = new Button(this);
        menuButton.setText("Menu");
        menuButton.setAllCaps(false);
        menuButton.setOnClickListener(view -> showMenu());
        header.addView(menuButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(26);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setTextColor(getColor(R.color.text_primary));
        titleView.setPadding(dp(12), 0, 0, 0);
        header.addView(titleView, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f));
        root.addView(header);
    }

    private void showPlacement() {
        resetScreen("Spanish DELE Coach");
        selectedAnswers.clear();
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
        resetScreen("Recommended target: " + result.getSuggestedLevel().getCode());
        addBody("Placement score: " + result.getCorrectAnswers() + "/" + result.getTotalQuestions()
                + ". " + result.getMessage());
        addLevelSummary(result.getSuggestedLevel());
        Button continueButton = addButton("Start learning");
        continueButton.setOnClickListener(view -> showHome());
    }

    private void showHome() {
        resetScreen("Today in Spanish");
        DeleLevel level = getTargetLevel();
        addBody("Target exam: " + level.getLabel() + "\n"
                + level.getExamFocus() + "\n\n"
                + "Quiz size: " + getQuestionCount() + " questions\n"
                + "Latest daily quiz score: " + preferences.getInt(KEY_DAILY_SCORE, 0));

        addLevelSummary(level);
        addButton("Daily quiz").setOnClickListener(view -> showDailyQuiz(level));
        addButton("Grammar articles").setOnClickListener(view -> showArticles(level));
        addButton("Dictionary and verbs").setOnClickListener(view -> showDictionary(level));
        addButton("Test history").setOnClickListener(view -> showTestHistory());
        addButton("Settings").setOnClickListener(view -> showSettings());
    }

    private void showSettings() {
        resetScreen("Settings");
        addBody("Choose the DELE exam level you want to train toward and how many questions appear in the daily quiz.");

        LinearLayout levelCard = addCard();
        addCardTitle(levelCard, "Target exam level");
        RadioGroup levelGroup = new RadioGroup(this);
        levelGroup.setOrientation(RadioGroup.VERTICAL);
        DeleLevel currentLevel = getTargetLevel();
        for (DeleLevel level : DeleLevel.values()) {
            RadioButton option = new RadioButton(this);
            option.setId(View.generateViewId());
            option.setTag(level.name());
            option.setText(level.getLabel());
            option.setTextSize(16);
            levelGroup.addView(option);
            if (level == currentLevel) {
                levelGroup.check(option.getId());
            }
        }
        levelCard.addView(levelGroup);

        LinearLayout countCard = addCard();
        addCardTitle(countCard, "Number of daily questions");
        RadioGroup countGroup = new RadioGroup(this);
        countGroup.setOrientation(RadioGroup.VERTICAL);
        int currentCount = getQuestionCount();
        for (int count : new int[]{5, 10, 15, 20}) {
            RadioButton option = new RadioButton(this);
            option.setId(View.generateViewId());
            option.setTag(count);
            option.setText(count + " questions");
            option.setTextSize(16);
            countGroup.addView(option);
            if (count == currentCount) {
                countGroup.check(option.getId());
            }
        }
        countCard.addView(countGroup);

        addButton("Save settings").setOnClickListener(view -> {
            RadioButton selectedLevel = levelGroup.findViewById(levelGroup.getCheckedRadioButtonId());
            RadioButton selectedCount = countGroup.findViewById(countGroup.getCheckedRadioButtonId());
            if (selectedLevel == null || selectedCount == null) {
                Toast.makeText(this, "Choose both settings first.", Toast.LENGTH_SHORT).show();
                return;
            }
            preferences.edit()
                    .putString(KEY_TARGET_LEVEL, (String) selectedLevel.getTag())
                    .putInt(KEY_QUESTION_COUNT, (Integer) selectedCount.getTag())
                    .apply();
            Toast.makeText(this, "Settings saved.", Toast.LENGTH_SHORT).show();
            showHome();
        });
        addBackHomeButton();
    }

    private void showDailyQuiz(DeleLevel targetLevel) {
        resetScreen("Daily consolidation quiz");
        selectedAnswers.clear();
        List<QuizQuestion> questions = limitQuestions(repository.getDailyQuestions(targetLevel));
        addBody(questions.size() + " questions are selected up to " + targetLevel.getLabel()
                + ". Verb conjugation forms are included as individual recall prompts.");

        for (QuizQuestion question : questions) {
            addQuestion(question);
        }

        addButton("Score daily quiz").setOnClickListener(view -> {
            if (selectedAnswers.size() < questions.size()) {
                Toast.makeText(this, "Complete every daily question first.", Toast.LENGTH_SHORT).show();
                return;
            }
            AssessmentEngine.AssessmentResult result = assessmentEngine.evaluate(questions, selectedAnswers);
            saveTestHistory(result, questions, targetLevel);
            preferences.edit()
                    .putString(KEY_TARGET_LEVEL, result.getSuggestedLevel().name())
                    .putInt(KEY_DAILY_SCORE, result.getCorrectAnswers())
                    .apply();
            showQuizResult(result);
        });
        addBackHomeButton();
    }

    private void showQuizResult(AssessmentEngine.AssessmentResult result) {
        resetScreen("Daily score: " + result.getCorrectAnswers() + "/" + result.getTotalQuestions());
        addBody(result.getMessage() + "\n\nUpdated recommendation: " + result.getSuggestedLevel().getLabel());
        addLevelSummary(result.getSuggestedLevel());
        addButton("Review suggested articles").setOnClickListener(view -> showArticles(result.getSuggestedLevel()));
        addButton("View test history").setOnClickListener(view -> showTestHistory());
        addBackHomeButton();
    }

    private void showTestHistory() {
        resetScreen("Test history");
        JSONArray history = getHistory();
        if (history.length() == 0) {
            addBody("No daily quiz attempts yet. Complete a quiz and the score will appear here.");
            addBackHomeButton();
            return;
        }

        for (int i = 0; i < history.length(); i++) {
            JSONObject item = history.optJSONObject(i);
            if (item == null) {
                continue;
            }
            addHistoryItem(item);
        }
        addBackHomeButton();
    }

    private void addHistoryItem(JSONObject item) {
        LinearLayout card = addCard();
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView summary = new TextView(this);
        summary.setText(item.optString("date") + "\nScore: "
                + item.optInt("score") + "/" + item.optInt("total")
                + " | Level: " + item.optString("level"));
        summary.setTextSize(16);
        summary.setTypeface(Typeface.DEFAULT_BOLD);
        summary.setTextColor(getColor(R.color.text_primary));
        header.addView(summary, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f));

        TextView toggleIcon = new TextView(this);
        toggleIcon.setText("v");
        toggleIcon.setTextSize(24);
        toggleIcon.setGravity(Gravity.CENTER);
        header.addView(toggleIcon, new LinearLayout.LayoutParams(dp(40), dp(40)));
        card.addView(header);

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setVisibility(View.GONE);
        JSONArray missed = item.optJSONArray("missed");
        if (missed == null || missed.length() == 0) {
            addCardBody(details, "All answers were correct.");
        } else {
            addCardBody(details, "Incorrect answers:");
            for (int i = 0; i < missed.length(); i++) {
                JSONObject miss = missed.optJSONObject(i);
                if (miss == null) {
                    continue;
                }
                addCardBody(details, "- " + miss.optString("prompt")
                        + "\n  Your answer: " + miss.optString("selected")
                        + "\n  Correct: " + miss.optString("correct"));
            }
        }
        card.addView(details);

        View.OnClickListener toggle = view -> {
            boolean expand = details.getVisibility() != View.VISIBLE;
            details.setVisibility(expand ? View.VISIBLE : View.GONE);
            toggleIcon.setText(expand ? "^" : "v");
        };
        header.setOnClickListener(toggle);
        toggleIcon.setOnClickListener(toggle);
    }

    private void showArticles(DeleLevel targetLevel) {
        resetScreen("Grammar articles");
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
        resetScreen("Dictionary and conjugations");
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
            addCardBody(card, verb.getLevel().getLabel() + " | " + verb.getTense());
            addCardBody(card, verb.describeForms());
        }
        addBackHomeButton();
    }

    private void saveTestHistory(AssessmentEngine.AssessmentResult result, List<QuizQuestion> questions, DeleLevel targetLevel) {
        JSONArray history = getHistory();
        JSONObject entry = new JSONObject();
        JSONArray missed = new JSONArray();
        try {
            entry.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()));
            entry.put("score", result.getCorrectAnswers());
            entry.put("total", result.getTotalQuestions());
            entry.put("level", targetLevel.getCode());
            for (QuizQuestion question : questions) {
                Integer selectedIndex = selectedAnswers.get(question);
                if (!question.isCorrect(selectedIndex)) {
                    JSONObject miss = new JSONObject();
                    miss.put("prompt", question.getPrompt());
                    miss.put("selected", selectedIndex == null ? "No answer" : question.getOptions().get(selectedIndex));
                    miss.put("correct", question.getOptions().get(question.getCorrectAnswerIndex()));
                    missed.put(miss);
                }
            }
            entry.put("missed", missed);
            JSONArray updated = new JSONArray();
            updated.put(entry);
            for (int i = 0; i < Math.min(history.length(), 24); i++) {
                updated.put(history.getJSONObject(i));
            }
            preferences.edit().putString(KEY_TEST_HISTORY, updated.toString()).apply();
        } catch (JSONException exception) {
            Toast.makeText(this, "Could not save test history.", Toast.LENGTH_SHORT).show();
        }
    }

    private JSONArray getHistory() {
        try {
            return new JSONArray(preferences.getString(KEY_TEST_HISTORY, "[]"));
        } catch (JSONException exception) {
            return new JSONArray();
        }
    }

    private List<QuizQuestion> limitQuestions(List<QuizQuestion> questions) {
        int max = Math.min(getQuestionCount(), questions.size());
        return new ArrayList<>(questions.subList(0, max));
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
        try {
            return DeleLevel.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return DeleLevel.A1;
        }
    }

    private int getQuestionCount() {
        return preferences.getInt(KEY_QUESTION_COUNT, DEFAULT_QUESTION_COUNT);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
