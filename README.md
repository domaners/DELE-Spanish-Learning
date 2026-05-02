# DELE Spanish Learning

A lightweight native Android starter app for DELE Spanish study practice.

The app currently provides an installable Android shell with a focused home screen
for study areas such as vocabulary, listening, writing, and speaking. It is ready
for future lesson and exercise content to be added under `app/src/main`.

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
  src/main/java/com/dele/spanishlearning/MainActivity.java
  src/main/res/
scripts/build-debug-apk.sh
```
