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
- Home's structure (greeting + avatar, centred hero with trend, account tile strip, monthly bars, recent transactions) comes from the NavyGold study (`docs/design/navy-gold-home-claro-2026-09-07.png`, HANDOFF D-024); its look is Esforia's. Charts live in `ui/components/Charts.kt` (`BarChart`, `DonutChart`, `Sparkline`) and take colours from `Palette.chart`.
- The earlier image guide (`docs/design/guia-visual-vic-2026-09-07.png`) is superseded; keep it only as history. The NavyGold skin was removed in D-024 (recoverable from commit `0b3c5cb`).

## Data layer (HANDOFF D-025, D-026)
- No Room/KSP (none exists for Kotlin 2.4.x) and no DI framework. Persistence is `FinanceStore` → `JsonFileFinanceStore` (`files/finance.json`, atomic writes); tests use `InMemoryFinanceStore`. `AppContainer` (built in `PersonalApplication`) wires store, providers and `FinanceRepository`; composables get it via `LocalAppContainer` and `appViewModel { }`.
- `FinanceRepository` is the only writer. Money is `Long` minor units + ISO code (`Money.format`/`parseToMinor`); timestamps are epoch millis; "now" always comes from `repository.clock` (injectable) — never `System.currentTimeMillis()`/`YearMonth.now()` in ViewModels.
- Totals and breakdowns are computed only in `domain/FinanceCalculator`.
- Banks go through `BankProvider`. `MockBankProvider` is a deterministic local sandbox (seeded per institution and day); `OpenBankingProviderTemplate` documents how a real aggregator plugs in. Record ids for linked data are `"<providerId>:<externalId>"` so re-sync upserts.
- Forms use `ui/components/Forms.kt`; category/account icons and labels come from `CategoryUi.kt`. Form routes live in `Routes` and hide the bottom bar.
- Screenshot tests seed an in-memory container with the sandbox bank at a fixed clock.
- Bank notifications (HANDOFF D-031): `data/capture/BankNotificationListener` (NotificationListenerService, only packages in `BankNotificationParser.bankApps`) → `BankNotificationParser` → `FinanceData.inbox` (`CapturedTransaction`, PENDING/ACCEPTED/DISMISSED) → user accepts in `InboxScreen` → `Transaction` with `Source.CAPTURED`. Formats are assumed BBVA España wording; every real sample Vic provides becomes a case in `BankNotificationParserTest`. The "try it with a text" box in `CaptureSettingsScreen` runs the same path as a real notification.
- Reports are pure functions in `domain/Reports.kt` (week = Monday–Sunday, month, 6-month series); the Balance screen and the PDF both read them. Exports live in `data/export/ExportManager.kt` (CSV + `PdfDocument` PDF, `cacheDir/exports`, shared via FileProvider). `PdfDocument` does not run in Robolectric: the PDF test is skipped with `Assume`; verify PDF changes on a device.

## Conventions
- Dependency versions live only in `gradle/libs.versions.toml`.
- Kotlin 2.4: use `kotlin { compilerOptions { ... } }`, not `kotlinOptions`.
- Toolchain is pinned to AGP 8.13.x / compileSdk 36 (see HANDOFF.md D-003). Newer AndroidX releases (core-ktx 1.19+, activity 1.13+, Compose BOM 2026.x) require AGP 9 and compileSdk 37; do not bump them without upgrading the whole toolchain and the SDK packages in `.claude/hooks/session-start.sh`.
- Keep `mipmap-anydpi-v26` as is; renaming it breaks AAPT (HANDOFF.md D-004).
- Launcher icon (HANDOFF D-032): adaptive icon with PNG layers (`ic_launcher_background`, `ic_launcher_foreground`, `ic_launcher_monochrome`) in `mipmap-*dpi`, generated by `tools/make_launcher_icon.py` from `docs/design/icono-app-2026-09-07.png`. To change the icon, replace the source image and rerun the script (`pip install pillow numpy`); never edit the PNGs by hand.
- Release builds are signed with the debug key (personal use, no Play Store).
- Strings go in `res/values/strings.xml` (English) and `res/values-es/strings.xml` (Spanish); UI is Compose only, no XML layouts.
- Never use Material `Card`/`Surface`/`Button` directly; use the Esforia primitives in `Surfaces.kt`. Colours only from `LocalPalette`, never hard-coded hex in screens.
- Every scrollable screen is a `LazyColumn` with bottom padding `NavBarExpandedHeight + NavBarBottomMargin + navigation bars`; the bubble bar collapses via the nested-scroll connection in `FinanceApp`, screens must not manage it.
- The five tabs are pages of a `HorizontalPager` inside the single `Routes.TABS` route (HANDOFF D-030); `pagerState.currentPage` is the source of truth for the active tab, background tone and bar. Switching tabs from a screen goes through the `onTab` lambda, never the NavController. Forms/details are routes on top. In screenshot tests, swipe on `onRoot()` (two `screen_list` nodes exist at once).
- System bar icon colours are re-applied in `MainActivity` whenever the app theme changes; don't rely on `enableEdgeToEdge()` defaults.
- Work proceeds in the 4 phases listed in `HANDOFF.md`; do not start the next phase until Vic has given feedback on the current one.
