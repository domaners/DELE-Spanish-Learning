CREATE TABLE IF NOT EXISTS articles (
    id BIGSERIAL PRIMARY KEY,
    level TEXT NOT NULL CHECK (level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    title TEXT NOT NULL,
    grammar_focus TEXT NOT NULL,
    summary TEXT NOT NULL DEFAULT '',
    body TEXT NOT NULL,
    vocabulary TEXT[] NOT NULL DEFAULT '{}',
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE OR REPLACE FUNCTION level_rank(level_code TEXT)
RETURNS INTEGER AS $$
    SELECT CASE level_code
        WHEN 'A1' THEN 1
        WHEN 'A2' THEN 2
        WHEN 'B1' THEN 3
        WHEN 'B2' THEN 4
        WHEN 'C1' THEN 5
        WHEN 'C2' THEN 6
        ELSE 99
    END;
$$ LANGUAGE SQL IMMUTABLE;

CREATE TABLE IF NOT EXISTS vocabulary_entries (
    id BIGSERIAL PRIMARY KEY,
    level TEXT NOT NULL CHECK (level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    spanish TEXT NOT NULL,
    english TEXT NOT NULL,
    theme TEXT NOT NULL,
    example TEXT NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS verb_conjugations (
    id BIGSERIAL PRIMARY KEY,
    level TEXT NOT NULL CHECK (level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    infinitive TEXT NOT NULL,
    tense TEXT NOT NULL,
    meaning TEXT NOT NULL,
    forms JSONB NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS quiz_questions (
    id BIGSERIAL PRIMARY KEY,
    quiz_type TEXT NOT NULL CHECK (quiz_type IN ('placement', 'daily')),
    level TEXT NOT NULL CHECK (level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    prompt TEXT NOT NULL,
    options TEXT[] NOT NULL,
    correct_answer_index INTEGER NOT NULL CHECK (correct_answer_index >= 0),
    sort_order INTEGER NOT NULL DEFAULT 0,
    CHECK (array_length(options, 1) > correct_answer_index)
);

CREATE INDEX IF NOT EXISTS idx_articles_level_order ON articles (level, sort_order);
CREATE INDEX IF NOT EXISTS idx_vocabulary_level_order ON vocabulary_entries (level, sort_order);
CREATE INDEX IF NOT EXISTS idx_verbs_level_order ON verb_conjugations (level, sort_order);
CREATE INDEX IF NOT EXISTS idx_questions_type_level_order ON quiz_questions (quiz_type, level, sort_order);
