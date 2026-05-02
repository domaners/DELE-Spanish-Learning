package com.example.delespanish;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String PREFS = "dele_spanish_progress";
    private static final String KEY_PLACEMENT_COMPLETE = "placementComplete";
    private static final String KEY_PLACEMENT_LEVEL = "placementLevel";
    private static final String KEY_TARGET_LEVEL = "targetLevel";
    private static final String KEY_DAILY_SCORE = "dailyScore";
    private static final String KEY_FAVOURITE_WORDS = "favouriteWords";

    private LearningRepository repository;
    private AssessmentEngine assessmentEngine;
    private SharedPreferences preferences;
    private LinearLayout root;
    private LinearLayout bottomNavigation;
    private final Map<QuizQuestion, Integer> selectedAnswers = new java.util.HashMap<>();

    private enum Screen {
        HOME,
        DAILY,
        DICTIONARY,
        FAVOURITES,
        SETTINGS
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new LearningRepository();
        assessmentEngine = new AssessmentEngine();
        preferences = getSharedPreferences(PREFS, MODE_PRIVATE);

        LinearLayout appShell = new LinearLayout(this);
        appShell.setOrientation(LinearLayout.VERTICAL);
        appShell.setBackgroundResource(R.drawable.app_background);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(false);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(24));
        scrollView.addView(root);

        bottomNavigation = new LinearLayout(this);
        bottomNavigation.setOrientation(LinearLayout.HORIZONTAL);
        bottomNavigation.setGravity(Gravity.CENTER);
        bottomNavigation.setPadding(dp(6), dp(6), dp(6), dp(6));
        bottomNavigation.setBackgroundColor(getColor(R.color.surface));

        appShell.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1
        ));
        appShell.addView(bottomNavigation, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        setContentView(appShell);

        if (preferences.getBoolean(KEY_PLACEMENT_COMPLETE, false)) {
            showHome();
        } else {
            showPlacement();
        }
    }

    private void showPlacement() {
        prepareScreen(null);
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
                    .putString(KEY_PLACEMENT_LEVEL, result.getSuggestedLevel().name())
                    .putString(KEY_TARGET_LEVEL, result.getSuggestedLevel().name())
                    .putInt(KEY_DAILY_SCORE, result.getCorrectAnswers())
                    .apply();
            showPlacementResult(result);
        });
    }

    private void showPlacementResult(AssessmentEngine.AssessmentResult result) {
        prepareScreen(null);
        addTitle("Recommended target: " + result.getSuggestedLevel().getLabel());
        addBody("Placement score: " + result.getCorrectAnswers() + "/" + result.getTotalQuestions()
                + ". " + result.getMessage()
                + "\n\nYou can change this study level later from Settings.");
        addLevelSummary(result.getSuggestedLevel());
        Button continueButton = addButton("Start learning");
        continueButton.setOnClickListener(view -> showHome());
    }

    private void showHome() {
        prepareScreen(Screen.HOME);
        DeleLevel level = getTargetLevel();
        addTitle("Today in Spanish");
        addBody("Target exam: " + level.getLabel() + "\n"
                + level.getExamFocus() + "\n\n"
                + "Latest daily quiz score: " + preferences.getInt(KEY_DAILY_SCORE, 0) + "\n"
                + "Favourite words saved: " + getFavouriteVocabularyIds().size());

        addLevelSummary(level);
        addButton("Daily quiz").setOnClickListener(view -> showDailyQuiz(level));
        addButton("Grammar articles").setOnClickListener(view -> showArticles(level));
        addButton("Dictionary and verbs").setOnClickListener(view -> showDictionary(level));
        addButton("Favourite vocabulary").setOnClickListener(view -> showFavourites());
        addButton("Settings").setOnClickListener(view -> showSettings());
    }

    private void showDailyQuiz(DeleLevel targetLevel) {
        prepareScreen(Screen.DAILY);
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
                    .putInt(KEY_DAILY_SCORE, result.getCorrectAnswers())
                    .apply();
            showQuizResult(result);
        });
    }

    private void showQuizResult(AssessmentEngine.AssessmentResult result) {
        prepareScreen(Screen.DAILY);
        addTitle("Daily score: " + result.getCorrectAnswers() + "/" + result.getTotalQuestions());
        addBody(result.getMessage() + "\n\nYour study level stays at "
                + getTargetLevel().getLabel() + ". Change it any time in Settings.");
        addLevelSummary(result.getSuggestedLevel());
        addButton("Review suggested articles").setOnClickListener(view -> showArticles(result.getSuggestedLevel()));
    }

    private void showArticles(DeleLevel targetLevel) {
        prepareScreen(Screen.HOME);
        addTitle("Grammar articles");
        addBody("DELE-aligned explanations are grouped from A1 to " + targetLevel.getLabel() + ".");
        for (Article article : repository.getArticlesUpTo(targetLevel)) {
            LinearLayout card = addCard();
            addCardTitle(card, article.getLevel().getLabel() + " - " + article.getTitle());
            addCardBody(card, article.getGrammarFocus());
            addCardBody(card, article.getBody());
            addCardBody(card, "Key vocabulary: " + TextUtils.join(", ", article.getVocabulary()));
        }
    }

    private void showDictionary(DeleLevel targetLevel) {
        prepareScreen(Screen.DICTIONARY);
        addTitle("Dictionary and conjugations");
        addBody("Vocabulary and verb forms are tagged by the DELE level where they become most useful. Save vocabulary cards to review from Favourites.");

        addSectionHeader("Vocabulary");
        for (VocabularyEntry entry : repository.getVocabularyUpTo(targetLevel)) {
            addVocabularyCard(entry, true);
        }

        addSectionHeader("Verb conjugations");
        for (VerbConjugation verb : repository.getVerbsUpTo(targetLevel)) {
            LinearLayout card = addCard();
            addCardTitle(card, verb.getInfinitive() + " - " + verb.getMeaning());
            addCardBody(card, verb.getLevel().getLabel());
            addCardBody(card, verb.describeForms());
        }
    }

    private void showFavourites() {
        prepareScreen(Screen.FAVOURITES);
        addTitle("Favourite vocabulary");
        addBody("Words saved from the dictionary appear here for quick review.");

        boolean hasFavourites = false;
        Set<String> favouriteIds = getFavouriteVocabularyIds();
        for (VocabularyEntry entry : repository.getVocabulary()) {
            if (favouriteIds.contains(getVocabularyId(entry))) {
                addVocabularyCard(entry, false);
                hasFavourites = true;
            }
        }

        if (!hasFavourites) {
            addBody("No favourite words yet. Open the dictionary and save useful vocabulary for later.");
            addButton("Open dictionary").setOnClickListener(view -> showDictionary(getTargetLevel()));
        }
    }

    private void showSettings() {
        prepareScreen(Screen.SETTINGS);
        DeleLevel targetLevel = getTargetLevel();
        addTitle("Settings");
        addBody("Your DELE study level is set automatically after the pretest. You can override it here if your exam goal changes.");

        DeleLevel placementLevel = getPlacementLevel();
        if (placementLevel != null) {
            addBody("Pretest recommendation: " + placementLevel.getLabel());
        }

        addSectionHeader("Studying for");
        RadioGroup levelGroup = new RadioGroup(this);
        levelGroup.setOrientation(RadioGroup.VERTICAL);
        for (DeleLevel level : DeleLevel.values()) {
            RadioButton option = new RadioButton(this);
            option.setText(level.getLabel());
            option.setTextSize(16);
            option.setId(View.generateViewId());
            option.setTag(level);
            levelGroup.addView(option);
            if (level == targetLevel) {
                option.setChecked(true);
            }
        }
        levelGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checked = group.findViewById(checkedId);
            if (checked == null) {
                return;
            }
            DeleLevel selectedLevel = (DeleLevel) checked.getTag();
            preferences.edit()
                    .putString(KEY_TARGET_LEVEL, selectedLevel.name())
                    .apply();
            Toast.makeText(this, "Studying for " + selectedLevel.getCode(), Toast.LENGTH_SHORT).show();
            showSettings();
        });
        root.addView(levelGroup);

        addLevelSummary(targetLevel);
        addButton("Retake pretest").setOnClickListener(view -> showPlacement());
    }

    private void addVocabularyCard(VocabularyEntry entry, boolean returnToDictionary) {
        LinearLayout card = addCard();
        addCardTitle(card, entry.getSpanish() + " - " + entry.getEnglish());
        addCardBody(card, entry.getLevel().getLabel() + " | " + entry.getTheme());
        addCardBody(card, entry.getExample());

        String vocabularyId = getVocabularyId(entry);
        boolean isFavourite = getFavouriteVocabularyIds().contains(vocabularyId);
        Button favouriteButton = addCardButton(card, isFavourite ? "Remove from favourites" : "Save to favourites");
        favouriteButton.setOnClickListener(view -> {
            setVocabularyFavourite(vocabularyId, !isFavourite);
            if (returnToDictionary) {
                showDictionary(getTargetLevel());
            } else {
                showFavourites();
            }
        });
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

    private Button addCardButton(LinearLayout card, String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(6), 0, 0);
        card.addView(button, params);
        return button;
    }

    private void prepareScreen(Screen activeScreen) {
        root.removeAllViews();
        renderBottomNavigation(activeScreen);
    }

    private void renderBottomNavigation(Screen activeScreen) {
        bottomNavigation.removeAllViews();
        if (activeScreen == null || !preferences.getBoolean(KEY_PLACEMENT_COMPLETE, false)) {
            bottomNavigation.setVisibility(View.GONE);
            return;
        }

        bottomNavigation.setVisibility(View.VISIBLE);
        addNavigationButton("Home", activeScreen == Screen.HOME, view -> showHome());
        addNavigationButton("Daily", activeScreen == Screen.DAILY, view -> showDailyQuiz(getTargetLevel()));
        addNavigationButton("Dictionary", activeScreen == Screen.DICTIONARY, view -> showDictionary(getTargetLevel()));
        addNavigationButton("Favourites", activeScreen == Screen.FAVOURITES, view -> showFavourites());
        addNavigationButton("Settings", activeScreen == Screen.SETTINGS, view -> showSettings());
    }

    private void addNavigationButton(String text, boolean selected, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(11);
        button.setTextColor(getColor(selected ? R.color.brand_blue : R.color.text_secondary));
        button.setTypeface(Typeface.DEFAULT, selected ? Typeface.BOLD : Typeface.NORMAL);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setMinHeight(dp(44));
        button.setPadding(dp(2), 0, dp(2), 0);
        button.setOnClickListener(listener);
        bottomNavigation.addView(button, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
    }

    private DeleLevel getTargetLevel() {
        String value = preferences.getString(KEY_TARGET_LEVEL, DeleLevel.A1.name());
        try {
            return DeleLevel.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return DeleLevel.A1;
        }
    }

    private DeleLevel getPlacementLevel() {
        String value = preferences.getString(KEY_PLACEMENT_LEVEL, null);
        if (value == null) {
            return null;
        }
        try {
            return DeleLevel.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private Set<String> getFavouriteVocabularyIds() {
        return new HashSet<>(preferences.getStringSet(KEY_FAVOURITE_WORDS, new HashSet<>()));
    }

    private void setVocabularyFavourite(String vocabularyId, boolean favourite) {
        Set<String> favouriteIds = getFavouriteVocabularyIds();
        if (favourite) {
            favouriteIds.add(vocabularyId);
        } else {
            favouriteIds.remove(vocabularyId);
        }
        preferences.edit()
                .putStringSet(KEY_FAVOURITE_WORDS, favouriteIds)
                .apply();
    }

    private String getVocabularyId(VocabularyEntry entry) {
        return entry.getLevel().name() + ":" + entry.getSpanish();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
