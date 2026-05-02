# DELE Spanish Learning

An Android app prototype for learning Spanish against the DELE exam ladder from
A1 through C2.

## What the app includes

- First-run placement test that recommends a target DELE exam level.
- Dashboard showing the user's current recommended target and readiness focus.
- DELE-aligned grammar articles for A1, A2, B1, B2, C1, and C2.
- Dictionary entries tagged by level, theme, examples, and English meanings.
- Verb conjugation cards aligned to exam progression.
- Daily quiz flow that consolidates grammar and vocabulary up to the user's
  current target.
- Local progress storage with `SharedPreferences`.

## Build an installable Android APK

An installable debug APK is checked in at:

```text
dist/dele-spanish-learning-debug.apk
```

To rebuild it from source, run the helper script. It downloads local Android
build tooling into `.tools/` and produces a fresh debug APK.

```bash
./scripts/build-debug-apk.sh
```

When the build finishes, the APK will be available at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Install on your phone

1. Enable **Developer options** and **USB debugging** on your Android phone.
2. Connect the phone to your computer with USB.
3. Install with Android Debug Bridge:

   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

You can also copy `dist/dele-spanish-learning-debug.apk` or `app-debug.apk` to
the phone and open it there. Android may ask you to allow installs from that
source because this is a debug build.

## Project structure

```text
app/
  src/main/AndroidManifest.xml
  src/main/java/com/example/delespanish/
    MainActivity.java
    LearningRepository.java
    AssessmentEngine.java
    DeleLevel.java
  src/main/res/
scripts/build-debug-apk.sh
```
