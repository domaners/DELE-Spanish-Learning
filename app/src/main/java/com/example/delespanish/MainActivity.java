package com.example.delespanish;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String PREFS = "dele_spanish_progress";
    private static final String KEY_PLACEMENT_COMPLETE = "placementComplete";
    private static final String KEY_TARGET_LEVEL = "targetLevel";
    private static final String KEY_DAILY_SCORE = "dailyScore";
    private static final String KEY_TEST_HISTORY = "testHistory";
    private static final String KEY_FAVOURITES = "favourites";
    private static final String KEY_MAX_TEST_ITEMS = "maxTestItems";
    private static final int DEFAULT_MAX_TEST_ITEMS = 6;

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
        addMenuButton();
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
        addMenuButton();
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
        addMenuButton();
        addTitle("Today in Spanish");
        addBody("Target exam: " + level.getLabel() + "\n"
                + level.getExamFocus() + "\n\n"
                + "Latest daily quiz score: " + preferences.getInt(KEY_DAILY_SCORE, 0) + "\n"
                + "Maximum daily test items: " + describeMaxTestItems());

        addLevelSummary(level);
        addButton("Daily quiz").setOnClickListener(view -> showDailyQuiz(level));
        addButton("Grammar articles").setOnClickListener(view -> showArticles(level));
        addButton("Dictionary and verbs").setOnClickListener(view -> showDictionary(level));
        addButton("Settings").setOnClickListener(view -> showSettings());
        addButton("Test history").setOnClickListener(view -> showTestHistory());
        addButton("Retake placement").setOnClickListener(view -> showPlacement());
    }

    private void showDailyQuiz(DeleLevel targetLevel) {
        root.removeAllViews();
        selectedAnswers.clear();
        addMenuButton();
        addTitle("Daily consolidation quiz");
        int maxItems = getMaxTestItems();
        List<QuizQuestion> questions = repository.getDailyQuestions(
                targetLevel,
                getIncorrectQuestionIdsFromHistory(),
                maxItems
        );
        addBody("Questions are selected up to " + targetLevel.getLabel()
                + " and mix previous incorrect answers with new items for your level.\n"
                + "Maximum items: " + describeMaxTestItems());

        if (questions.isEmpty()) {
            addBody("Your max items setting is 0, so there are no questions in this daily test.");
            addBackHomeButton();
            return;
        }
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
            saveDailyTestHistory(targetLevel, questions, selectedAnswers, result);
            showQuizResult(result, questions);
        });
        addBackHomeButton();
    }

    private void showQuizResult(AssessmentEngine.AssessmentResult result, List<QuizQuestion> questions) {
        root.removeAllViews();
        addMenuButton();
        addTitle("Daily score: " + result.getCorrectAnswers() + "/" + result.getTotalQuestions());
        addBody(result.getMessage() + "\n\nUpdated recommendation: " + result.getSuggestedLevel().getLabel());
        addIncorrectSummary(questions, selectedAnswers);
        addLevelSummary(result.getSuggestedLevel());
        addButton("Review suggested articles").setOnClickListener(view -> showArticles(result.getSuggestedLevel()));
        addButton("View test history").setOnClickListener(view -> showTestHistory());
        addBackHomeButton();
    }

    private void showArticles(DeleLevel targetLevel) {
        root.removeAllViews();
        addMenuButton();
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
        addMenuButton();
        addTitle("Dictionary and conjugations");
        addBody("Vocabulary and verb forms are shown as rows tagged by the DELE level where they become most useful.");

        addSectionHeader("Vocabulary");
        for (VocabularyEntry entry : repository.getVocabularyUpTo(targetLevel)) {
            addDictionaryRow(
                    favouriteId("vocab", entry.getSpanish(), entry.getEnglish()),
                    entry.getSpanish() + " - " + entry.getEnglish(),
                    entry.getLevel().getLabel() + " | " + entry.getTheme() + "\n" + entry.getExample()
            );
        }

        addSectionHeader("Verb conjugations");
        for (VerbConjugation verb : repository.getVerbsUpTo(targetLevel)) {
            addDictionaryRow(
                    favouriteId("verb", verb.getInfinitive(), verb.getTense()),
                    verb.getInfinitive() + " - " + verb.getMeaning(),
                    verb.getLevel().getLabel() + "\n" + verb.describeForms()
            );
        }
        addBackHomeButton();
    }

    private void showSettings() {
        root.removeAllViews();
        addMenuButton();
        addTitle("Settings");
        addBody("Choose the maximum number of items on each daily test. Enter 0 for no questions, or any higher number.");

        EditText maxItemsInput = new EditText(this);
        maxItemsInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        maxItemsInput.setText(String.valueOf(getMaxTestItems()));
        maxItemsInput.setHint("Max test items");
        maxItemsInput.setSingleLine(true);
        root.addView(maxItemsInput, fullWidthParams());

        addButton("Save settings").setOnClickListener(view -> {
            String value = maxItemsInput.getText().toString().trim();
            if (value.isEmpty()) {
                Toast.makeText(this, "Enter a number from 0 or above.", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int maxItems = Integer.parseInt(value);
                if (maxItems < 0) {
                    Toast.makeText(this, "Enter a number from 0 or above.", Toast.LENGTH_SHORT).show();
                    return;
                }
                preferences.edit().putInt(KEY_MAX_TEST_ITEMS, maxItems).apply();
                Toast.makeText(this, "Settings saved.", Toast.LENGTH_SHORT).show();
                showHome();
            } catch (NumberFormatException exception) {
                Toast.makeText(this, "Enter a whole number from 0 or above.", Toast.LENGTH_SHORT).show();
            }
        });
        addBackHomeButton();
    }

    private void showTestHistory() {
        root.removeAllViews();
        addMenuButton();
        addTitle("Test history");
        JSONArray history = getHistory();
        if (history.length() == 0) {
            addBody("Completed daily tests will appear here with any incorrect answers.");
            addBackHomeButton();
            return;
        }

        TableLayout table = new TableLayout(this);
        table.setStretchAllColumns(true);
        addHistoryRow(table, "Date", "Level", "Score");
        for (int i = history.length() - 1; i >= 0; i--) {
            JSONObject test = history.optJSONObject(i);
            if (test == null) {
                continue;
            }
            addHistoryRow(
                    table,
                    test.optString("date"),
                    test.optString("level"),
                    test.optInt("correct") + "/" + test.optInt("total")
            );
        }
        root.addView(table, fullWidthParams());

        for (int i = history.length() - 1; i >= 0; i--) {
            JSONObject test = history.optJSONObject(i);
            if (test == null) {
                continue;
            }
            LinearLayout card = addCard();
            addCardTitle(card, test.optString("date") + " - " + test.optString("level")
                    + " - " + test.optInt("correct") + "/" + test.optInt("total"));
            JSONArray incorrect = test.optJSONArray("incorrect");
            if (incorrect == null || incorrect.length() == 0) {
                addCardBody(card, "No incorrect answers.");
                continue;
            }
            for (int j = 0; j < incorrect.length(); j++) {
                JSONObject item = incorrect.optJSONObject(j);
                if (item == null) {
                    continue;
                }
                addCardBody(card, item.optString("prompt")
                        + "\nYour answer: " + item.optString("selected")
                        + "\nCorrect answer: " + item.optString("correct"));
            }
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

    private void addMenuButton() {
        Button menuButton = new Button(this);
        menuButton.setText("☰ Menu");
        menuButton.setAllCaps(false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(110), LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END;
        params.setMargins(0, 0, 0, dp(8));
        root.addView(menuButton, params);
        menuButton.setOnClickListener(view -> showNavigationMenu());
    }

    private void showNavigationMenu() {
        DeleLevel level = getTargetLevel();
        String[] items = new String[]{
                "Dashboard",
                "Daily quiz",
                "Grammar articles",
                "Dictionary and verbs",
                "Settings",
                "Test history",
                "Retake placement"
        };
        new AlertDialog.Builder(this)
                .setTitle("Menu")
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showHome();
                            break;
                        case 1:
                            showDailyQuiz(level);
                            break;
                        case 2:
                            showArticles(level);
                            break;
                        case 3:
                            showDictionary(level);
                            break;
                        case 4:
                            showSettings();
                            break;
                        case 5:
                            showTestHistory();
                            break;
                        case 6:
                            showPlacement();
                            break;
                        default:
                            break;
                    }
                })
                .show();
    }

    private void addDictionaryRow(String favouriteId, String title, String detail) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setBackgroundResource(R.drawable.card_background);
        LinearLayout.LayoutParams params = fullWidthParams();
        params.setMargins(0, dp(4), 0, dp(4));
        root.addView(row, params);

        LinearLayout textColumn = new LinearLayout(this);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(textColumn, textParams);
        addCardTitle(textColumn, title);
        addCardBody(textColumn, detail);

        Button favourite = new Button(this);
        favourite.setText(isFavourite(favouriteId) ? "♥" : "♡");
        favourite.setTextSize(22);
        favourite.setAllCaps(false);
        favourite.setContentDescription("Toggle favourite");
        row.addView(favourite, new LinearLayout.LayoutParams(dp(56), dp(56)));
        favourite.setOnClickListener(view -> {
            boolean selected = toggleFavourite(favouriteId);
            favourite.setText(selected ? "♥" : "♡");
        });
    }

    private void addHistoryRow(TableLayout table, String first, String second, String third) {
        TableRow row = new TableRow(this);
        row.addView(historyCell(first));
        row.addView(historyCell(second));
        row.addView(historyCell(third));
        table.addView(row);
    }

    private TextView historyCell(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(14);
        view.setTextColor(getColor(R.color.text_primary));
        view.setPadding(dp(6), dp(6), dp(6), dp(6));
        return view;
    }

    private void addIncorrectSummary(List<QuizQuestion> questions, Map<QuizQuestion, Integer> answers) {
        List<String> incorrect = new ArrayList<>();
        for (QuizQuestion question : questions) {
            Integer answer = answers.get(question);
            if (!question.isCorrect(answer)) {
                incorrect.add(question.getPrompt()
                        + "\nYour answer: " + answerText(question, answer)
                        + "\nCorrect answer: " + answerText(question, question.getCorrectAnswerIndex()));
            }
        }
        if (incorrect.isEmpty()) {
            addBody("No incorrect answers in this test.");
            return;
        }
        addSectionHeader("Incorrect answers");
        for (String item : incorrect) {
            addBody(item);
        }
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
        LinearLayout.LayoutParams params = fullWidthParams();
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

    private int getMaxTestItems() {
        return preferences.getInt(KEY_MAX_TEST_ITEMS, DEFAULT_MAX_TEST_ITEMS);
    }

    private String describeMaxTestItems() {
        return String.valueOf(getMaxTestItems());
    }

    private JSONArray getHistory() {
        try {
            return new JSONArray(preferences.getString(KEY_TEST_HISTORY, "[]"));
        } catch (JSONException exception) {
            return new JSONArray();
        }
    }

    private List<String> getIncorrectQuestionIdsFromHistory() {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        JSONArray history = getHistory();
        for (int i = history.length() - 1; i >= 0; i--) {
            JSONObject test = history.optJSONObject(i);
            if (test == null) {
                continue;
            }
            JSONArray incorrect = test.optJSONArray("incorrect");
            if (incorrect == null) {
                continue;
            }
            for (int j = 0; j < incorrect.length(); j++) {
                JSONObject item = incorrect.optJSONObject(j);
                if (item != null) {
                    String id = item.optString("id", "");
                    if (!id.isEmpty()) {
                        ids.add(id);
                    }
                }
            }
        }
        return new ArrayList<>(ids);
    }

    private void saveDailyTestHistory(
            DeleLevel level,
            List<QuizQuestion> questions,
            Map<QuizQuestion, Integer> answers,
            AssessmentEngine.AssessmentResult result
    ) {
        JSONArray history = getHistory();
        JSONObject test = new JSONObject();
        JSONArray incorrect = new JSONArray();
        try {
            test.put("date", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date()));
            test.put("level", level.getLabel());
            test.put("correct", result.getCorrectAnswers());
            test.put("total", result.getTotalQuestions());
            for (QuizQuestion question : questions) {
                Integer answer = answers.get(question);
                if (!question.isCorrect(answer)) {
                    JSONObject item = new JSONObject();
                    item.put("id", question.getId());
                    item.put("prompt", question.getPrompt());
                    item.put("selected", answerText(question, answer));
                    item.put("correct", answerText(question, question.getCorrectAnswerIndex()));
                    incorrect.put(item);
                }
            }
            test.put("incorrect", incorrect);
            history.put(test);
            preferences.edit().putString(KEY_TEST_HISTORY, history.toString()).apply();
        } catch (JSONException exception) {
            Toast.makeText(this, "Could not save test history.", Toast.LENGTH_SHORT).show();
        }
    }

    private String answerText(QuizQuestion question, Integer answerIndex) {
        if (answerIndex == null || answerIndex < 0 || answerIndex >= question.getOptions().size()) {
            return "No answer";
        }
        return question.getOptions().get(answerIndex);
    }

    private String favouriteId(String type, String first, String second) {
        return type + ":" + first + ":" + second;
    }

    private boolean isFavourite(String id) {
        return getFavourites().contains(id);
    }

    private boolean toggleFavourite(String id) {
        Set<String> favourites = getFavourites();
        boolean selected;
        if (favourites.contains(id)) {
            favourites.remove(id);
            selected = false;
        } else {
            favourites.add(id);
            selected = true;
        }
        preferences.edit().putStringSet(KEY_FAVOURITES, favourites).apply();
        return selected;
    }

    private Set<String> getFavourites() {
        return new LinkedHashSet<>(preferences.getStringSet(KEY_FAVOURITES, new LinkedHashSet<>()));
    }

    private LinearLayout.LayoutParams fullWidthParams() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
