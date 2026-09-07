# PersonalApp

App Android de finanzas personales, de uso personal (Kotlin + Jetpack Compose + Material 3).
Identidad visual de Esforia (`docs/design/identidad-esforia.md`): paleta morada, Sora/Manrope/
IBM Plex Mono, fondo ambiental de blobs desenfocados, hero en degradado, tarjetas opacas y barra
inferior flotante que se encoge al hacer scroll. Se desarrolla en 4 fases (ver `HANDOFF.md`).

> El nombre `PersonalApp` y el paquete `com.personal.app` son provisionales
> hasta definir la funcionalidad de la app.

## Documentación

- `HANDOFF.md`: diario del proyecto. Estado actual, pendientes y el porqué de cada decisión. Leer antes de tocar nada.
- `CLAUDE.md`: convenciones para Claude Code.

## Requisitos

- JDK 17 o superior (probado con JDK 21)
- Android SDK con `platforms;android-36`, `build-tools;35.0.0` y `platform-tools`
- `local.properties` con `sdk.dir=/ruta/al/sdk` (se genera solo en Claude Code web)

## Comandos

```bash
./gradlew assembleDebug        # APK de debug -> app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease      # APK release (R8, firmado con la clave debug) -> app/build/outputs/apk/release/
./gradlew testDebugUnitTest    # tests unitarios
./gradlew lintDebug            # Android Lint
./gradlew recordRoborazziDebug # capturas de cada pantalla en app/screenshots/ (sin emulador)
adb install -r app/build/outputs/apk/debug/app-debug.apk   # instalar en un móvil por USB
```

## Estructura

```
app/src/main/java/com/personal/app/
  FinanceApp.kt                       raiz de la UI: fondo -> pantalla -> barra
  ui/theme/                           Palette (tokens Esforia), Type (Sora/Manrope/Plex Mono), Theme
  ui/components/                      AmbientBackground, Surfaces (cards, hero, groups...), PageHeader, GlassSurface, BubbleNavBar, NavBarScrollState
  ui/navigation/                      Destination (5 pestañas + tono ambiental) y AppNavHost
  ui/screens/                         ScreenScaffold y una pantalla por archivo
app/src/main/res/                     strings (en + es), fuentes, tema, icono adaptativo
docs/design/                          identidad-esforia.md, licencias OFL, guia de imagen antigua
app/src/test/                         tests unitarios y de captura (Robolectric + Roborazzi)
gradle/libs.versions.toml             catalogo de versiones
.claude/hooks/session-start.sh        instala el SDK en Claude Code web
```

## Versiones

| Componente | Version |
|---|---|
| Gradle | 8.14.3 |
| Android Gradle Plugin | 8.13.2 |
| Kotlin | 2.4.10 |
| Compose BOM | 2025.08.00 |
| compileSdk / targetSdk | 36 |
| minSdk | 26 (Android 8.0) |
