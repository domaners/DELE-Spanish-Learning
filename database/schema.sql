CREATE TABLE IF NOT EXISTS articles (
    id BIGSERIAL PRIMARY KEY,
    level_code TEXT NOT NULL CHECK (level_code IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    title TEXT NOT NULL,
    grammar_focus TEXT NOT NULL,
    summary TEXT NOT NULL DEFAULT '',
    body TEXT NOT NULL,
    vocabulary TEXT[] NOT NULL DEFAULT '{}',
    display_order INTEGER NOT NULL DEFAULT 0,
    is_published BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT articles_title_key UNIQUE (title)
);

CREATE INDEX IF NOT EXISTS idx_articles_level_sort
    ON articles (level_code, display_order, id);

CREATE TABLE IF NOT EXISTS vocabulary_entries (
    id BIGSERIAL PRIMARY KEY,
    level_code TEXT NOT NULL CHECK (level_code IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    spanish TEXT NOT NULL,
    english TEXT NOT NULL,
    theme TEXT NOT NULL,
    example TEXT NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    is_published BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT vocabulary_entries_spanish_english_key UNIQUE (spanish, english)
);

CREATE INDEX IF NOT EXISTS idx_vocabulary_entries_level_sort
    ON vocabulary_entries (level_code, display_order, id);
