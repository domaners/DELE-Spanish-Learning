# DELE Spanish Learning

An Android app prototype for learning Spanish against the DELE exam ladder from A1 through C2.

## What the app includes

- First-run placement test that recommends a target DELE exam level.
- Dashboard showing the user's current recommended target and readiness focus.
- DELE-aligned grammar articles for A1, A2, B1, B2, C1, and C2, loaded from PostgreSQL through the content API.
- Dictionary entries tagged by level, theme, examples, and English meanings, loaded from PostgreSQL through the content API.
- Verb conjugation cards aligned to exam progression.
- Daily quiz flow that consolidates grammar and vocabulary up to the user's current target.
- Local progress storage with `SharedPreferences`.

## Project layout

```text
app/src/main/java/com/example/delespanish/
  MainActivity.java          Single-activity native Android UI
  ContentApiClient.java      HTTP client for database-backed article and vocabulary content
  LearningRepository.java    Seed fallback content, verbs, and quiz content
  AssessmentEngine.java      Placement and daily quiz scoring
  DeleLevel.java             DELE level metadata
database/
  schema.sql                 PostgreSQL tables for articles and vocabulary
  seed.sql                   Initial article and vocabulary rows
  import_articles.sql        CSV import helper for article data
server/
  main.py                    FastAPI content API backed by PostgreSQL
```

## PostgreSQL content database

Start PostgreSQL and the content API:

```bash
docker compose up
```

This creates a `dele_spanish` PostgreSQL database with `articles` and `vocabulary_entries` tables, then exposes the app content API at `http://localhost:8000`.

The Android emulator reaches the host API at `http://10.0.2.2:8000`, configured in `app/src/main/res/values/strings.xml` as `content_api_base_url`. Change that value if the API is deployed elsewhere.

Check the API:

```bash
curl http://localhost:8000/health
curl http://localhost:8000/articles
curl http://localhost:8000/vocabulary
```

## Import article data

Create `database/import_articles.csv` with these columns:

```csv
level,title,grammar_focus,summary,body,vocabulary
A1,"Introducing yourself with ser","Start with identity.","DELE A1 personal introductions.","Use ser for identity: Soy Ana.","ser;llamarse;identity"
```

`level` must be one of `A1`, `A2`, `B1`, `B2`, `C1`, or `C2`. Separate article vocabulary terms with semicolons in the `vocabulary` column.

With the compose stack running, import or update article rows:

```bash
psql postgresql://dele_app:dele_app_password@localhost:5432/dele_spanish \
  -v article_csv=database/import_articles.csv \
  -f database/import_articles.sql
```

Rows are upserted by article `title`, so rerunning the import updates existing articles with the same title.

## Build

Open the repository in Android Studio or run the following in an environment with Gradle and the Android SDK:

```bash
./gradlew test assembleDebug
```

The checked-in debug APK is available at `dist/dele-spanish-learning-debug.apk`.

The project uses the Android Gradle Plugin and plain Java Android views. If the content API is unavailable, article and vocabulary screens fall back to the bundled seed data.
