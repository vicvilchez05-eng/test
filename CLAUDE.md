# PersonalApp

Android app (Kotlin, Jetpack Compose, Material 3). Single module `:app`, package `com.personal.app`.

## Build & test
- `export ANDROID_HOME=/opt/android-sdk` if not already set (the SessionStart hook sets it on the web).
- `./gradlew assembleDebug` builds the debug APK; `./gradlew testDebugUnitTest` runs unit tests; `./gradlew lintDebug` runs lint.
- Always build with the wrapper (`./gradlew`), never the system `gradle`.
- Filter noise from Gradle output with `grep -v JAVA_TOOL_OPTIONS`.

## Conventions
- Dependency versions live only in `gradle/libs.versions.toml`.
- Kotlin 2.4: use `kotlin { compilerOptions { ... } }`, not `kotlinOptions`.
- Toolchain is pinned to AGP 8.13.x / compileSdk 36. Newer AndroidX releases (core-ktx 1.19+, activity 1.13+, Compose BOM 2026.x) require AGP 9 and compileSdk 37; do not bump them without upgrading the whole toolchain and the SDK packages in `.claude/hooks/session-start.sh`.
- Release builds are signed with the debug key (personal use only, no Play Store).
- Strings go in `res/values/strings.xml`; UI is Compose only, no XML layouts.
