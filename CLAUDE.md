# PersonalApp

Android app (Kotlin, Jetpack Compose, Material 3). Single module `:app`, package `com.personal.app`.

## HANDOFF.md is mandatory
- **Start of every session**: read `HANDOFF.md` ("Estado actual" and "Pendientes") before doing anything.
- **End of every task**: append a session entry (S-nnn) to `HANDOFF.md`. Any choice made between alternatives gets a decision entry (D-nnn) with *por qué* (context/problem), *para qué* (goal) and what was discarded and why.
- Never delete history from `HANDOFF.md`. A reverted decision is marked `REVERTIDA` and linked to its replacement. Compact only when Vic asks, and keep every decision intact.
- Write `HANDOFF.md` in Spanish; it is for Vic as much as for Claude.
- Commit `HANDOFF.md` together with the code change it describes.

## Build & test
- `export ANDROID_HOME=/opt/android-sdk` if not already set (the SessionStart hook sets it on the web).
- `./gradlew assembleDebug` builds the debug APK; `./gradlew testDebugUnitTest` runs unit tests; `./gradlew lintDebug` runs lint.
- Always build with the wrapper (`./gradlew`), never the system `gradle`.
- Filter noise from Gradle output with `grep -v JAVA_TOOL_OPTIONS`.

## Conventions
- Dependency versions live only in `gradle/libs.versions.toml`.
- Kotlin 2.4: use `kotlin { compilerOptions { ... } }`, not `kotlinOptions`.
- Toolchain is pinned to AGP 8.13.x / compileSdk 36 (see HANDOFF.md D-003). Newer AndroidX releases (core-ktx 1.19+, activity 1.13+, Compose BOM 2026.x) require AGP 9 and compileSdk 37; do not bump them without upgrading the whole toolchain and the SDK packages in `.claude/hooks/session-start.sh`.
- Keep `mipmap-anydpi-v26` as is; renaming it breaks AAPT (HANDOFF.md D-004).
- Release builds are signed with the debug key (personal use, no Play Store).
- Strings go in `res/values/strings.xml`; UI is Compose only, no XML layouts.
