import os
from contextlib import asynccontextmanager
from typing import Any

import psycopg
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from psycopg.rows import dict_row


DATABASE_URL = os.environ.get(
    "DATABASE_URL",
    "postgresql://dele_app:dele_app_password@localhost:5432/dele_spanish",
)


@asynccontextmanager
async def lifespan(app: FastAPI):
    async with await psycopg.AsyncConnection.connect(DATABASE_URL, row_factory=dict_row) as connection:
        app.state.db = connection
        yield


app = FastAPI(title="DELE Spanish Content API", lifespan=lifespan)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["GET"],
    allow_headers=["*"],
)


@app.get("/health")
async def health() -> dict[str, str]:
    return {"status": "ok"}


@app.get("/articles")
async def articles() -> list[dict[str, Any]]:
    async with app.state.db.cursor() as cursor:
        await cursor.execute(
            """
            SELECT
                level_code AS level,
                title,
                grammar_focus AS "grammarFocus",
                summary,
                body,
                vocabulary
            FROM articles
            WHERE is_published = true
            ORDER BY display_order, id
            """
        )
        return await cursor.fetchall()


@app.get("/vocabulary")
async def vocabulary() -> list[dict[str, str]]:
    async with app.state.db.cursor() as cursor:
        await cursor.execute(
            """
            SELECT
                level_code AS level,
                spanish,
                english,
                theme,
                example
            FROM vocabulary_entries
            WHERE is_published = true
            ORDER BY display_order, id
            """
        )
        return await cursor.fetchall()
