package com.example.delespanish;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String PREFS = "dele_spanish_progress";
    private static final String KEY_PLACEMENT_COMPLETE = "placementComplete";
    private static final String KEY_TARGET_LEVEL = "targetLevel";
    private static final String KEY_DAILY_SCORE = "dailyScore";
    private static final String KEY_QUESTION_COUNT = "questionCount";
    private static final String KEY_TEST_HISTORY = "testHistory";
    private static final String KEY_FAVORITES = "favorites";
    private static final String KEY_SRS_PROGRESS = "srsProgress";
    private static final int DEFAULT_QUESTION_COUNT = 10;
    private static final int READY_PROFICIENCY = 80;

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
        addMenuButton("Vocabulary", () -> showVocabulary(getTargetLevel(), ""));
        addMenuButton("Verbs", () -> showVerbs(getTargetLevel(), ""));
        addMenuButton("Favourites", () -> showFavorites());
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
        menuButton.setText("☰");
        menuButton.setAllCaps(false);
        menuButton.setTextSize(24);
        menuButton.setTextColor(getColor(R.color.text_primary));
        menuButton.setBackgroundColor(Color.WHITE);
        menuButton.setMinWidth(0);
        menuButton.setMinHeight(0);
        menuButton.setOnClickListener(view -> showMenu());
        header.addView(menuButton, new LinearLayout.LayoutParams(
                dp(48),
                dp(48)));

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
        int readiness = getLevelReadiness(level);
        addBody("Target exam: " + level.getLabel() + "\n"
                + level.getExamFocus() + "\n\n"
                + "Quiz size: " + getQuestionCount() + " questions\n"
                + "Latest daily quiz score: " + preferences.getInt(KEY_DAILY_SCORE, 0) + "\n"
                + "Exam readiness: " + readiness + "% - " + getReadinessMessage(readiness));

        addLevelSummary(level);
        addButton("Daily quiz").setOnClickListener(view -> showDailyQuiz(level));
        addButton("Grammar articles").setOnClickListener(view -> showArticles(level));
        addButton("Vocabulary").setOnClickListener(view -> showVocabulary(level, ""));
        addButton("Verbs").setOnClickListener(view -> showVerbs(level, ""));
        addButton("Favourites").setOnClickListener(view -> showFavorites());
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
        List<QuizQuestion> questions = selectSrsQuestions(repository.getDailyQuestions(targetLevel));
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
            updateSrsProgress(questions);
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
            String key = articleKey(article);
            LinearLayout card = addCard();
            addFavoriteCardTitle(card, article.getLevel().getLabel() + " - " + article.getTitle(), key);
            addCardBody(card, article.getGrammarFocus());
            addCardBody(card, article.getBody());
            addCardBody(card, "Key vocabulary: " + TextUtils.join(", ", article.getVocabulary()));
        }
        addBackHomeButton();
    }

    private void showVocabulary(DeleLevel targetLevel, String query) {
        resetScreen("Vocabulary");
        addBody("Search filters anywhere in the Spanish word, English definition, theme, or example.");
        LinearLayout results = createResultsContainer();
        addSearchBox(query, text -> renderVocabularyResults(results, targetLevel, text));
        addResultsContainer(results);
        renderVocabularyResults(results, targetLevel, query);
        addBackHomeButton();
    }

    private void renderVocabularyResults(LinearLayout results, DeleLevel targetLevel, String query) {
        results.removeAllViews();
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        boolean hasMatches = false;
        for (VocabularyEntry entry : repository.getVocabularyUpTo(targetLevel)) {
            if (!matchesVocabulary(entry, normalizedQuery)) {
                continue;
            }
            hasMatches = true;
            String key = vocabularyKey(entry);
            LinearLayout card = addCard(results);
            addFavoriteCardTitle(card, entry.getSpanish() + " - " + entry.getEnglish(), key);
            addCardBody(card, entry.getLevel().getLabel() + " | " + entry.getTheme());
            addCardBody(card, entry.getExample() + "\nProficiency: " + getItemProficiency(key) + "%");
        }
        if (!hasMatches) {
            addCardBody(results, "No vocabulary matches found.");
        }
    }

    private void showVerbs(DeleLevel targetLevel, String query) {
        resetScreen("Verbs");
        addBody("Search filters anywhere in the infinitive, definition, tense, or conjugated forms.");
        LinearLayout results = createResultsContainer();
        addSearchBox(query, text -> renderVerbResults(results, targetLevel, text));
        addResultsContainer(results);
        renderVerbResults(results, targetLevel, query);
        addBackHomeButton();
    }

    private void renderVerbResults(LinearLayout results, DeleLevel targetLevel, String query) {
        results.removeAllViews();
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        boolean hasMatches = false;
        for (VerbConjugation verb : repository.getVerbsUpTo(targetLevel)) {
            if (!matchesVerb(verb, normalizedQuery)) {
                continue;
            }
            hasMatches = true;
            String key = verbKey(verb);
            LinearLayout card = addCard(results);
            addFavoriteCardTitle(card, verb.getInfinitive() + " - " + verb.getMeaning(), key);
            addCardBody(card, verb.getLevel().getLabel() + " | " + verb.getTense());
            addCardBody(card, verb.describeForms() + "\nProficiency: " + getItemProficiency(key) + "%");
        }
        if (!hasMatches) {
            addCardBody(results, "No verb matches found.");
        }
    }

    private void showFavorites() {
        resetScreen("Favourites");
        Set<String> favorites = getFavorites();
        if (favorites.isEmpty()) {
            addBody("No favourites yet. Use the favourite buttons on vocabulary, verbs, and grammar articles.");
            addBackHomeButton();
            return;
        }
        addFavoriteArticles(favorites);
        addFavoriteVocabulary(favorites);
        addFavoriteVerbs(favorites);
        addBackHomeButton();
    }

    private void addFavoriteArticles(Set<String> favorites) {
        boolean hasItems = false;
        for (Article article : repository.getArticlesUpTo(DeleLevel.C2)) {
            String key = articleKey(article);
            if (!favorites.contains(key)) {
                continue;
            }
            if (!hasItems) {
                addSectionHeader("Grammar articles");
                hasItems = true;
            }
            LinearLayout card = addCard();
            addFavoriteCardTitle(card, article.getLevel().getLabel() + " - " + article.getTitle(), key);
            addCardBody(card, article.getGrammarFocus());
        }
    }

    private void addFavoriteVocabulary(Set<String> favorites) {
        boolean hasItems = false;
        for (VocabularyEntry entry : repository.getVocabularyUpTo(DeleLevel.C2)) {
            String key = vocabularyKey(entry);
            if (!favorites.contains(key)) {
                continue;
            }
            if (!hasItems) {
                addSectionHeader("Vocabulary");
                hasItems = true;
            }
            LinearLayout card = addCard();
            addFavoriteCardTitle(card, entry.getSpanish() + " - " + entry.getEnglish(), key);
            addCardBody(card, "Proficiency: " + getItemProficiency(key) + "%\n" + entry.getExample());
        }
    }

    private void addFavoriteVerbs(Set<String> favorites) {
        boolean hasItems = false;
        for (VerbConjugation verb : repository.getVerbsUpTo(DeleLevel.C2)) {
            String key = verbKey(verb);
            if (!favorites.contains(key)) {
                continue;
            }
            if (!hasItems) {
                addSectionHeader("Verbs");
                hasItems = true;
            }
            LinearLayout card = addCard();
            addFavoriteCardTitle(card, verb.getInfinitive() + " - " + verb.getMeaning(), key);
            addCardBody(card, verb.describeForms() + "\nProficiency: " + getItemProficiency(key) + "%");
        }
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

    private List<QuizQuestion> selectSrsQuestions(List<QuizQuestion> questions) {
        List<QuizQuestion> due = new ArrayList<>();
        List<QuizQuestion> upcoming = new ArrayList<>();
        long now = System.currentTimeMillis();
        JSONObject progress = getSrsProgress();
        for (QuizQuestion question : questions) {
            JSONObject item = progress.optJSONObject(question.getId());
            if (item == null || item.optLong("dueAt", 0) <= now) {
                due.add(question);
            } else {
                upcoming.add(question);
            }
        }
        due.addAll(upcoming);
        int max = Math.min(getQuestionCount(), due.size());
        return new ArrayList<>(due.subList(0, max));
    }

    private void updateSrsProgress(List<QuizQuestion> questions) {
        JSONObject progress = getSrsProgress();
        long now = System.currentTimeMillis();
        for (QuizQuestion question : questions) {
            Integer selectedIndex = selectedAnswers.get(question);
            boolean correct = question.isCorrect(selectedIndex);
            JSONObject item = progress.optJSONObject(question.getId());
            if (item == null) {
                item = new JSONObject();
            }
            int attempts = item.optInt("attempts", 0) + 1;
            int correctAttempts = item.optInt("correct", 0) + (correct ? 1 : 0);
            int interval = correct ? Math.max(1, item.optInt("interval", 0) * 2) : 0;
            int dueHours = correct ? (interval == 1 ? 24 : interval * 24) : 4;
            int proficiency = Math.max(0, Math.min(100, item.optInt("proficiency", 0) + (correct ? 20 : -15)));
            try {
                item.put("attempts", attempts);
                item.put("correct", correctAttempts);
                item.put("interval", interval);
                item.put("dueAt", now + dueHours * 60L * 60L * 1000L);
                item.put("proficiency", proficiency);
                item.put("level", question.getLevel().name());
                item.put("studyItemKey", question.getStudyItemKey());
                progress.put(question.getId(), item);
            } catch (JSONException exception) {
                Toast.makeText(this, "Could not update quiz progress.", Toast.LENGTH_SHORT).show();
            }
        }
        preferences.edit().putString(KEY_SRS_PROGRESS, progress.toString()).apply();
    }

    private JSONObject getSrsProgress() {
        try {
            return new JSONObject(preferences.getString(KEY_SRS_PROGRESS, "{}"));
        } catch (JSONException exception) {
            return new JSONObject();
        }
    }

    private int getItemProficiency(String itemKey) {
        JSONObject progress = getSrsProgress();
        int total = 0;
        int count = 0;
        for (QuizQuestion question : getAllQuestions()) {
            if (!itemKey.equals(question.getStudyItemKey())) {
                continue;
            }
            JSONObject item = progress.optJSONObject(question.getId());
            if (item != null) {
                total += item.optInt("proficiency", 0);
                count++;
            }
        }
        return count == 0 ? 0 : Math.round((float) total / count);
    }

    private List<QuizQuestion> getAllQuestions() {
        List<QuizQuestion> questions = new ArrayList<>(repository.getPlacementQuestions());
        questions.addAll(repository.getDailyQuestions(DeleLevel.C2));
        return questions;
    }

    private int getLevelReadiness(DeleLevel level) {
        JSONObject progress = getSrsProgress();
        int total = 0;
        int count = 0;
        for (QuizQuestion question : repository.getDailyQuestions(level)) {
            if (question.getLevel() != level) {
                continue;
            }
            JSONObject item = progress.optJSONObject(question.getId());
            total += item == null ? 0 : item.optInt("proficiency", 0);
            count++;
        }
        return count == 0 ? 0 : Math.round((float) total / count);
    }

    private String getReadinessMessage(int readiness) {
        if (readiness >= READY_PROFICIENCY) {
            return "ready to book the exam";
        }
        return "keep reviewing with SRS";
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
        return addCard(root);
    }

    private LinearLayout addCard(LinearLayout parent) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackgroundResource(R.drawable.card_background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(8), 0, dp(8));
        parent.addView(card, params);
        return card;
    }

    private LinearLayout createResultsContainer() {
        LinearLayout results = new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);
        return results;
    }

    private void addResultsContainer(LinearLayout results) {
        root.addView(results, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
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

    private void addFavoriteCardTitle(LinearLayout card, String text, String key) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, dp(6));

        TextView title = new TextView(this);
        title.setText(text);
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(getColor(R.color.text_primary));
        row.addView(title, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f));

        Button heart = new Button(this);
        heart.setAllCaps(false);
        heart.setText(isFavorite(key) ? "♥" : "♡");
        heart.setTextSize(24);
        heart.setTextColor(getColor(R.color.primary));
        heart.setBackgroundColor(Color.TRANSPARENT);
        heart.setMinWidth(0);
        heart.setMinHeight(0);
        heart.setPadding(0, 0, 0, 0);
        heart.setOnClickListener(view -> {
            toggleFavorite(key);
            heart.setText(isFavorite(key) ? "♥" : "♡");
        });
        row.addView(heart, new LinearLayout.LayoutParams(dp(48), dp(48)));
        card.addView(row);
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

    private void addSearchBox(String query, SearchHandler handler) {
        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setHint("Search");
        search.setText(query);
        search.setSelection(search.getText().length());
        search.setTextColor(getColor(R.color.text_primary));
        search.setHintTextColor(getColor(R.color.text_secondary));
        search.setPadding(dp(12), dp(8), dp(12), dp(8));
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                handler.onSearchChanged(text.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(12));
        root.addView(search, params);
    }

    private boolean matchesVocabulary(VocabularyEntry entry, String query) {
        if (query.isEmpty()) {
            return true;
        }
        return contains(entry.getSpanish(), query)
                || contains(entry.getEnglish(), query)
                || contains(entry.getTheme(), query)
                || contains(entry.getExample(), query);
    }

    private boolean matchesVerb(VerbConjugation verb, String query) {
        if (query.isEmpty()) {
            return true;
        }
        return contains(verb.getInfinitive(), query)
                || contains(verb.getMeaning(), query)
                || contains(verb.getTense(), query)
                || contains(verb.describeForms(), query);
    }

    private boolean contains(String value, String query) {
        return value.toLowerCase(Locale.ROOT).contains(query);
    }

    private String articleKey(Article article) {
        return "article:" + article.getLevel().name() + ":" + slug(article.getTitle());
    }

    private String vocabularyKey(VocabularyEntry entry) {
        return "vocab:" + entry.getSpanish();
    }

    private String verbKey(VerbConjugation verb) {
        return "verb:" + verb.getInfinitive() + ":" + verb.getTense();
    }

    private String slug(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private Set<String> getFavorites() {
        return new HashSet<>(preferences.getStringSet(KEY_FAVORITES, new HashSet<>()));
    }

    private boolean isFavorite(String key) {
        return getFavorites().contains(key);
    }

    private void toggleFavorite(String key) {
        Set<String> favorites = getFavorites();
        if (favorites.contains(key)) {
            favorites.remove(key);
        } else {
            favorites.add(key);
        }
        preferences.edit().putStringSet(KEY_FAVORITES, favorites).apply();
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

    private interface SearchHandler {
        void onSearchChanged(String text);
    }
}
