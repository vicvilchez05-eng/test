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

## Visual reference: Esforia
- The visual identity is Esforia's, Vic's other app (`vicvilchez05-eng/esforia-app`, React/Capacitor). It is **read-only reference**: never modify it. Attach with `add_repo` + shallow clone to `/home/user/esforia-app` when you need to check how something is done there.
- Everything extracted from it lives in `docs/design/identidad-esforia.md` (palette, fonts, surfaces, ambient background, screen structure). **Read it before any UI work.** HANDOFF.md D-021 explains the mapping to this codebase.
- Tokens: `LocalPalette` (`ui/theme/Palette.kt`) is the source of truth; Material `colorScheme` is derived from it. Fonts: Sora headings, Manrope body, IBM Plex Mono for money (`MonoText`).
- Surfaces (`ui/components/Surfaces.kt`): `SurfaceCard` (opaque, on purpose), `HeroCard` + `HeroStat`, `Group` + `GroupRow` + `GroupLabel`, `SectionLabel`, `Chip`, `OutlineButton`, `CircleIconButton`, `EsforiaSwitch`. `GlassSurface` is only for the floating bar. Page tops use `PageHeader` (eyebrow + left-aligned Sora title + circle actions). Screens use `ScreenScaffold`.
- Background is `AmbientBackground` with a per-destination `AmbientTone`; blobs stay in the margins and blurred, never hard-edged.
- The earlier image guide (`docs/design/guia-visual-vic-2026-09-07.png`) is superseded; keep it only as history.

## Conventions
- Dependency versions live only in `gradle/libs.versions.toml`.
- Kotlin 2.4: use `kotlin { compilerOptions { ... } }`, not `kotlinOptions`.
- Toolchain is pinned to AGP 8.13.x / compileSdk 36 (see HANDOFF.md D-003). Newer AndroidX releases (core-ktx 1.19+, activity 1.13+, Compose BOM 2026.x) require AGP 9 and compileSdk 37; do not bump them without upgrading the whole toolchain and the SDK packages in `.claude/hooks/session-start.sh`.
- Keep `mipmap-anydpi-v26` as is; renaming it breaks AAPT (HANDOFF.md D-004).
- Release builds are signed with the debug key (personal use, no Play Store).
- Strings go in `res/values/strings.xml` (English) and `res/values-es/strings.xml` (Spanish); UI is Compose only, no XML layouts.
- Never use Material `Card`/`Surface`/`Button` directly; use the Esforia primitives in `Surfaces.kt`. Colours only from `LocalPalette`, never hard-coded hex in screens.
- Every scrollable screen is a `LazyColumn` with bottom padding `NavBarExpandedHeight + NavBarBottomMargin + navigation bars`; the bubble bar collapses via the nested-scroll connection in `FinanceApp`, screens must not manage it.
- Work proceeds in the 4 phases listed in `HANDOFF.md`; do not start the next phase until Vic has given feedback on the current one.
