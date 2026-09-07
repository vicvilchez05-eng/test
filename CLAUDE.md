# PersonalApp

Android app (Kotlin, Jetpack Compose, Material 3). Single module `:app`, package `com.personal.app`.

## HANDOFF.md is mandatory
- **Start of every session**: read `HANDOFF.md` ("Estado actual" and "Pendientes") before doing anything.
- **End of every task**: append a session entry (S-nnn) to `HANDOFF.md`. Any choice made between alternatives gets a decision entry (D-nnn) with *por qué* (context/problem), *para qué* (goal) and what was discarded and why.
- A reverted decision is marked `REVERTIDA` and linked to its replacement; do not silently drop it.
- **Compaction (D-009)**: at session start run `wc -l HANDOFF.md`. If it exceeds 1000 lines, delete the oldest session entries (S-nnn) whose work is fully finished, drop decisions that are reverted or obsolete, and shrink still-relevant decisions from those phases to one line each (id, decision, short why). Stop once the file is under ~600 lines. Note the compacted range in the current session entry. Below 1000 lines, never compact.
- Write `HANDOFF.md` in Spanish; it is for Vic as much as for Claude.
- Commit `HANDOFF.md` together with the code change it describes.

## Build & test
- `export ANDROID_HOME=/opt/android-sdk` if not already set (the SessionStart hook sets it on the web).
- `./gradlew assembleDebug` builds the debug APK; `./gradlew testDebugUnitTest` runs unit tests; `./gradlew lintDebug` runs lint.
- `./gradlew recordRoborazziDebug` renders every screen to `app/screenshots/*.png` on the JVM (no emulator). **Look at the PNGs with the Read tool after any UI change** and send them to Vic with SendUserFile. Screenshots are git-ignored.
- Lint must pass with zero errors before a commit (warnings about newer versions and `-v26` are expected).
- Always build with the wrapper (`./gradlew`), never the system `gradle`.
- Filter noise from Gradle output with `grep -v JAVA_TOOL_OPTIONS`.

## Visual reference
- The look is defined by Vic's guide at `docs/design/guia-visual-vic-2026-09-07.png` (Read it before any UI work) and by HANDOFF.md D-019. Light theme is primary; dark is a translation.
- Font is Inter (`ui/theme/Type.kt`); text is navy on white frosted cards; screen titles are tracked small caps via `ScreenHeader`; grouped lists use `GlassRowGroup`.

## Conventions
- Dependency versions live only in `gradle/libs.versions.toml`.
- Kotlin 2.4: use `kotlin { compilerOptions { ... } }`, not `kotlinOptions`.
- Toolchain is pinned to AGP 8.13.x / compileSdk 36 (see HANDOFF.md D-003). Newer AndroidX releases (core-ktx 1.19+, activity 1.13+, Compose BOM 2026.x) require AGP 9 and compileSdk 37; do not bump them without upgrading the whole toolchain and the SDK packages in `.claude/hooks/session-start.sh`.
- Keep `mipmap-anydpi-v26` as is; renaming it breaks AAPT (HANDOFF.md D-004).
- Release builds are signed with the debug key (personal use, no Play Store).
- Strings go in `res/values/strings.xml` (English) and `res/values-es/strings.xml` (Spanish); UI is Compose only, no XML layouts.
- All surfaces use `GlassCard`/`GlassSurface`; never a plain Material `Card`/`Surface` with an opaque background. Glass tokens live in `ui/theme/Glass.kt` (`LocalGlass`).
- Every scrollable screen is a `LazyColumn` with bottom padding `NavBarExpandedHeight + NavBarBottomMargin + navigation bars`; the bubble bar collapses via the nested-scroll connection in `FinanceApp`, screens must not manage it.
- Work proceeds in the 4 phases listed in `HANDOFF.md`; do not start the next phase until Vic has given feedback on the current one.
