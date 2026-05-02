import os
from contextlib import contextmanager

import psycopg
from flask import Flask, jsonify
from flask_cors import CORS
from psycopg.rows import dict_row


app = Flask(__name__)
CORS(app)


def database_url():
    value = os.environ.get("DATABASE_URL")
    if not value:
        raise RuntimeError("DATABASE_URL must be set, for example postgresql://user:pass@host:5432/dele_spanish")
    return value


@contextmanager
def connection():
    with psycopg.connect(database_url(), row_factory=dict_row) as conn:
        yield conn


@app.get("/health")
def health():
    return jsonify({"status": "ok"})


@app.get("/api/content")
def content():
    with connection() as conn:
        return jsonify({
            "articles": fetch_articles(conn),
            "vocabulary": fetch_vocabulary(conn),
            "verbConjugations": fetch_verbs(conn),
            "placementQuestions": fetch_questions(conn, "placement"),
            "dailyQuestions": fetch_questions(conn, "daily"),
        })


def fetch_articles(conn):
    rows = conn.execute(
        """
        SELECT level, title, grammar_focus, summary, body, vocabulary
        FROM articles
        ORDER BY level_rank(level), sort_order, title
        """
    ).fetchall()
    return [
        {
            "level": row["level"],
            "title": row["title"],
            "grammarFocus": row["grammar_focus"],
            "summary": row["summary"],
            "body": row["body"],
            "vocabulary": row["vocabulary"],
        }
        for row in rows
    ]


def fetch_vocabulary(conn):
    rows = conn.execute(
        """
        SELECT level, spanish, english, theme, example
        FROM vocabulary_entries
        ORDER BY level_rank(level), spanish
        """
    ).fetchall()
    return [
        {
            "level": row["level"],
            "spanish": row["spanish"],
            "english": row["english"],
            "theme": row["theme"],
            "example": row["example"],
        }
        for row in rows
    ]


def fetch_verbs(conn):
    rows = conn.execute(
        """
        SELECT level, infinitive, tense, meaning, forms
        FROM verb_conjugations
        ORDER BY level_rank(level), infinitive, tense
        """
    ).fetchall()
    return [
        {
            "level": row["level"],
            "infinitive": row["infinitive"],
            "tense": row["tense"],
            "meaning": row["meaning"],
            "forms": forms_from_json(row["forms"]),
        }
        for row in rows
    ]


def fetch_questions(conn, question_type):
    rows = conn.execute(
        """
        SELECT level, prompt, options, correct_answer_index
        FROM quiz_questions
        WHERE quiz_type = %s
        ORDER BY level_rank(level), sort_order, id
        """,
        (question_type,),
    ).fetchall()
    return [
        {
            "level": row["level"],
            "prompt": row["prompt"],
            "options": row["options"],
            "correctAnswerIndex": row["correct_answer_index"],
        }
        for row in rows
    ]


def forms_from_json(forms):
    return [{"pronoun": pronoun, "form": form} for pronoun, form in forms.items()]


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.environ.get("PORT", "5000")))
