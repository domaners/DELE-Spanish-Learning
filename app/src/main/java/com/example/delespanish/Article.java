package com.example.delespanish;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Article {
    private final DeleLevel level;
    private final String title;
    private final String grammarFocus;
    private final String body;
    private final List<String> vocabulary;

    Article(DeleLevel level, String title, String grammarFocus, String body, List<String> vocabulary) {
        this(level, title, grammarFocus, "", body, vocabulary);
    }

    Article(DeleLevel level, String title, String grammarFocus, String summary, String body, List<String> vocabulary) {
        this.level = level;
        this.title = title;
        this.grammarFocus = summary.isEmpty() ? grammarFocus : grammarFocus + "\n" + summary;
        this.body = body;
        this.vocabulary = Collections.unmodifiableList(new ArrayList<>(vocabulary));
    }

    public DeleLevel getLevel() {
        return level;
    }

    public String getTitle() {
        return title;
    }

    public String getGrammarFocus() {
        return grammarFocus;
    }

    public String getBody() {
        return body;
    }

    public List<String> getVocabulary() {
        return vocabulary;
    }
}
