# DELE Spanish Learning

An Android app prototype for learning Spanish against the DELE exam ladder from A1 through C2.

## What the app includes

- First-run placement test that recommends a target DELE exam level.
- Dashboard showing the user's current recommended target and readiness focus.
- DELE-aligned grammar articles for A1, A2, B1, B2, C1, and C2 loaded from PostgreSQL.
- Dictionary entries tagged by level, theme, examples, and English meanings loaded from PostgreSQL.
- Verb conjugation cards aligned to exam progression.
- Daily quiz flow that consolidates grammar and vocabulary up to the user's current target.
- Local progress storage with `SharedPreferences`.

## Project layout

```text
app/src/main/java/com/example/delespanish/
  MainActivity.java          Single-activity native Android UI
  LearningRepository.java    HTTP content client for the backend API
  AssessmentEngine.java      Placement and daily quiz scoring
  DeleLevel.java             DELE level metadata
backend/
  app.py                     Flask API that reads PostgreSQL content
database/
  schema.sql                 PostgreSQL schema
  seed.sql                   Initial DELE learning content
```

## PostgreSQL-backed content

The Android app does not connect directly to PostgreSQL. Direct database connections from a mobile app would expose credentials in the APK, so content is served through the Flask API in `backend/app.py`.

### Configure the database

Create a PostgreSQL database, then apply the schema and seed content:

```bash
createdb dele_spanish
psql "$DATABASE_URL" -f database/schema.sql
psql "$DATABASE_URL" -f database/seed.sql
```

`DATABASE_URL` should be a PostgreSQL connection string, for example:

```bash
export DATABASE_URL="postgresql://dele_user:password@localhost:5432/dele_spanish"
```

### Content tables and data format

All level fields must be one of `A1`, `A2`, `B1`, `B2`, `C1`, or `C2`.

#### `articles`

Required columns:

- `level`
- `title`
- `grammar_focus`
- `summary`
- `body`
- `vocabulary` as `TEXT[]`

Example:

```sql
INSERT INTO articles (level, title, grammar_focus, summary, body, vocabulary)
VALUES (
  'B1',
  'Narrating experiences with preterite and imperfect',
  'Tell stories by separating completed events from background context.',
  'B1 writing and speaking ask you to recount trips, memories and problems.',
  'Use the preterite for completed actions and the imperfect for descriptions.',
  ARRAY['preterite', 'imperfect', 'storytelling']
);
```

#### `vocabulary_entries`

Required columns:

- `level`
- `spanish`
- `english`
- `theme`
- `example`

Example:

```sql
INSERT INTO vocabulary_entries (level, spanish, english, theme, example)
VALUES ('B1', 'alquilar', 'to rent', 'verb / housing', 'Queremos alquilar un piso cerca del centro.');
```

#### `verb_conjugations`

`forms` is a JSON object keyed by pronoun or label:

```sql
INSERT INTO verb_conjugations (level, infinitive, tense, meaning, forms)
VALUES ('A2', 'hablar', 'presente', 'to speak', '{"yo":"hablo","tu":"hablas","el/ella":"habla"}');
```

#### `quiz_questions`

`quiz_questions.quiz_type` must be either `placement` or `daily`.
`options` is a `TEXT[]`; `correct_answer_index` is zero-based.

```sql
INSERT INTO quiz_questions (quiz_type, level, prompt, options, correct_answer_index)
VALUES ('daily', 'B2', 'Complete: No creo que ___ facil.', ARRAY['es', 'sea', 'fue', 'sera'], 1);
```

### Run the backend API

```bash
python3 -m venv .venv
. .venv/bin/activate
pip install -r backend/requirements.txt
export DATABASE_URL="postgresql://dele_user:password@localhost:5432/dele_spanish"
python backend/app.py
```

The app fetches content from:

```text
http://10.0.2.2:5000/api/content
```

`10.0.2.2` is the Android emulator's alias for the host machine. For a physical Android device, update `DEFAULT_CONTENT_URL` in `LearningRepository.java` to point to a reachable backend URL.
The backend defaults to port `5000`; override it with `PORT` if needed and keep the Android URL in sync.

## Build

Open the repository in Android Studio or run the following in an environment with Gradle and the Android SDK:

```bash
gradle test assembleDebug
```

The project uses the Android Gradle Plugin and plain Java Android views. Runtime learning content is provided by the PostgreSQL-backed backend API.
