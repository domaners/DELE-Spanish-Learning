CREATE TEMP TABLE staged_articles (
    level_code TEXT,
    title TEXT,
    grammar_focus TEXT,
    summary TEXT,
    body TEXT,
    vocabulary TEXT
);

\copy staged_articles (level_code, title, grammar_focus, summary, body, vocabulary) FROM :'article_csv' WITH (FORMAT csv, HEADER true);

INSERT INTO articles (level_code, title, grammar_focus, summary, body, vocabulary)
SELECT
    level_code,
    title,
    grammar_focus,
    COALESCE(summary, ''),
    body,
    CASE
        WHEN COALESCE(vocabulary, '') = '' THEN '{}'
        ELSE string_to_array(vocabulary, ';')
    END
FROM staged_articles
ON CONFLICT (title) DO UPDATE
SET level_code = EXCLUDED.level_code,
    grammar_focus = EXCLUDED.grammar_focus,
    summary = EXCLUDED.summary,
    body = EXCLUDED.body,
    vocabulary = EXCLUDED.vocabulary,
    updated_at = now();
