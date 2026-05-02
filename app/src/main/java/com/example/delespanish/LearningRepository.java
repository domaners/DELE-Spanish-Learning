package com.example.delespanish;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class LearningRepository {
    private static final String DEFAULT_CONTENT_URL = "http://10.0.2.2:5000/api/content";

    private final String contentUrl;
    private List<Article> articles = Collections.emptyList();
    private List<VocabularyEntry> vocabulary = Collections.emptyList();
    private List<VerbConjugation> conjugations = Collections.emptyList();
    private List<QuizQuestion> placementQuestions = Collections.emptyList();
    private Map<DeleLevel, List<QuizQuestion>> dailyQuestions = emptyDailyQuestions();

    LearningRepository() {
        this(DEFAULT_CONTENT_URL);
    }

    LearningRepository(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    void refresh() throws IOException, JSONException {
        JSONObject content = readContent();
        articles = parseArticles(content.getJSONArray("articles"));
        vocabulary = parseVocabulary(content.getJSONArray("vocabulary"));
        conjugations = parseConjugations(content.getJSONArray("verbConjugations"));
        placementQuestions = parseQuestions(content.getJSONArray("placementQuestions"));
        dailyQuestions = parseDailyQuestions(content.getJSONArray("dailyQuestions"));
    }

    boolean hasContent() {
        return !placementQuestions.isEmpty();
    }

    List<QuizQuestion> getPlacementQuestions() {
        return placementQuestions;
    }

    List<QuizQuestion> getDailyQuestions(DeleLevel level) {
        List<QuizQuestion> selected = new ArrayList<>();
        for (DeleLevel candidate : DeleLevel.values()) {
            selected.addAll(dailyQuestions.get(candidate));
            if (candidate == level) {
                break;
            }
        }
        return Collections.unmodifiableList(selected);
    }

    List<Article> getArticlesUpTo(DeleLevel level) {
        List<Article> result = new ArrayList<>();
        for (Article article : articles) {
            if (article.getLevel().ordinal() <= level.ordinal()) {
                result.add(article);
            }
        }
        return Collections.unmodifiableList(result);
    }

    List<VocabularyEntry> getVocabularyUpTo(DeleLevel level) {
        List<VocabularyEntry> result = new ArrayList<>();
        for (VocabularyEntry entry : vocabulary) {
            if (entry.getLevel().ordinal() <= level.ordinal()) {
                result.add(entry);
            }
        }
        return Collections.unmodifiableList(result);
    }

    List<VerbConjugation> getVerbsUpTo(DeleLevel level) {
        List<VerbConjugation> result = new ArrayList<>();
        for (VerbConjugation conjugation : conjugations) {
            if (conjugation.getLevel().ordinal() <= level.ordinal()) {
                result.add(conjugation);
            }
        }
        return Collections.unmodifiableList(result);
    }

    private JSONObject readContent() throws IOException, JSONException {
        HttpURLConnection connection = (HttpURLConnection) new URL(contentUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(7000);
        connection.setReadTimeout(7000);
        connection.setRequestProperty("Accept", "application/json");

        int statusCode = connection.getResponseCode();
        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException("Content API returned HTTP " + statusCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                connection.getInputStream(),
                StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } finally {
            connection.disconnect();
        }

        return new JSONObject(response.toString());
    }

    private List<Article> parseArticles(JSONArray rows) throws JSONException {
        List<Article> parsed = new ArrayList<>();
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.getJSONObject(i);
            parsed.add(new Article(
                    parseLevel(row),
                    row.getString("title"),
                    row.getString("grammarFocus"),
                    row.getString("summary"),
                    row.getString("body"),
                    parseStringArray(row.getJSONArray("vocabulary"))
            ));
        }
        return Collections.unmodifiableList(parsed);
    }

    private List<VocabularyEntry> parseVocabulary(JSONArray rows) throws JSONException {
        List<VocabularyEntry> parsed = new ArrayList<>();
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.getJSONObject(i);
            parsed.add(new VocabularyEntry(
                    parseLevel(row),
                    row.getString("spanish"),
                    row.getString("english"),
                    row.getString("theme"),
                    row.getString("example")
            ));
        }
        return Collections.unmodifiableList(parsed);
    }

    private List<VerbConjugation> parseConjugations(JSONArray rows) throws JSONException {
        List<VerbConjugation> parsed = new ArrayList<>();
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.getJSONObject(i);
            parsed.add(new VerbConjugation(
                    parseLevel(row),
                    row.getString("infinitive"),
                    row.getString("tense"),
                    row.getString("meaning"),
                    parseForms(row.getJSONArray("forms"))
            ));
        }
        return Collections.unmodifiableList(parsed);
    }

    private List<QuizQuestion> parseQuestions(JSONArray rows) throws JSONException {
        List<QuizQuestion> parsed = new ArrayList<>();
        for (int i = 0; i < rows.length(); i++) {
            parsed.add(parseQuestion(rows.getJSONObject(i)));
        }
        return Collections.unmodifiableList(parsed);
    }

    private Map<DeleLevel, List<QuizQuestion>> parseDailyQuestions(JSONArray rows) throws JSONException {
        Map<DeleLevel, List<QuizQuestion>> grouped = emptyMutableDailyQuestions();
        for (int i = 0; i < rows.length(); i++) {
            QuizQuestion question = parseQuestion(rows.getJSONObject(i));
            grouped.get(question.getLevel()).add(question);
        }
        return freezeDailyQuestions(grouped);
    }

    private QuizQuestion parseQuestion(JSONObject row) throws JSONException {
        return new QuizQuestion(
                parseLevel(row),
                row.getString("prompt"),
                parseStringArray(row.getJSONArray("options")),
                row.getInt("correctAnswerIndex")
        );
    }

    private DeleLevel parseLevel(JSONObject row) throws JSONException {
        return DeleLevel.valueOf(row.getString("level"));
    }

    private List<String> parseStringArray(JSONArray array) throws JSONException {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            values.add(array.getString(i));
        }
        return Collections.unmodifiableList(values);
    }

    private Map<String, String> parseForms(JSONArray forms) throws JSONException {
        Map<String, String> parsed = new LinkedHashMap<>();
        for (int i = 0; i < forms.length(); i++) {
            JSONObject form = forms.getJSONObject(i);
            parsed.put(form.getString("pronoun"), form.getString("form"));
        }
        return Collections.unmodifiableMap(parsed);
    }

    private static Map<DeleLevel, List<QuizQuestion>> emptyDailyQuestions() {
        return freezeDailyQuestions(emptyMutableDailyQuestions());
    }

    private static Map<DeleLevel, List<QuizQuestion>> emptyMutableDailyQuestions() {
        Map<DeleLevel, List<QuizQuestion>> grouped = new EnumMap<>(DeleLevel.class);
        for (DeleLevel level : DeleLevel.values()) {
            grouped.put(level, new ArrayList<QuizQuestion>());
        }
        return grouped;
    }

    private static Map<DeleLevel, List<QuizQuestion>> freezeDailyQuestions(Map<DeleLevel, List<QuizQuestion>> grouped) {
        Map<DeleLevel, List<QuizQuestion>> frozen = new EnumMap<>(DeleLevel.class);
        for (Map.Entry<DeleLevel, List<QuizQuestion>> entry : grouped.entrySet()) {
            frozen.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(frozen);
    }
}
