package com.example.delespanish;

public final class VocabularyEntry {
    private final DeleLevel level;
    private final String spanish;
    private final String english;
    private final String partOfSpeech;
    private final String example;

    public VocabularyEntry(
            DeleLevel level,
            String spanish,
            String english,
            String partOfSpeech,
            String example
    ) {
        this.level = level;
        this.spanish = spanish;
        this.english = english;
        this.partOfSpeech = partOfSpeech;
        this.example = example;
    }

    public DeleLevel getLevel() {
        return level;
    }

    public String getSpanish() {
        return spanish;
    }

    public String getEnglish() {
        return english;
    }

    public String getTheme() {
        return partOfSpeech;
    }

    public String getExample() {
        return example;
    }
}
