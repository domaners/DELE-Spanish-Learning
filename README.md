# DELE Spanish Learning

An Android app prototype for learning Spanish against the DELE exam ladder from A1 through C2.

## What the app includes

- First-run placement test that recommends a target DELE exam level.
- Dashboard showing the user's current recommended target and readiness focus.
- DELE-aligned grammar articles for A1, A2, B1, B2, C1, and C2.
- Dictionary entries tagged by level, theme, examples, and English meanings.
- Verb conjugation cards aligned to exam progression.
- Daily quiz flow that consolidates grammar and vocabulary up to the user's current target.
- Local progress storage with `SharedPreferences`.

## Project layout

```text
app/src/main/java/com/example/delespanish/
  MainActivity.java          Single-activity native Android UI
  LearningRepository.java    Seed articles, dictionary, verbs, and quiz content
  AssessmentEngine.java      Placement and daily quiz scoring
  DeleLevel.java             DELE level metadata
```

## Build

Open the repository in Android Studio or run the following in an environment with Gradle and the Android SDK:

```bash
gradle test assembleDebug
```

The project uses the Android Gradle Plugin and plain Java Android views, with no runtime network dependency.
