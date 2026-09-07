# HANDOFF.md — Diario de proyecto

Documento vivo y compartido entre Vic y Claude. Aquí queda **todo**: qué se hizo, por qué se hizo
y para qué sirve. Nada se decide sin dejar rastro. Solo se compacta cuando el archivo pese
demasiado, y siempre conservando las decisiones y su justificación (se resume el detalle, nunca
se borra el porqué).

## Cómo usar este archivo

- **Al empezar una sesión**: leer "Estado actual" y "Pendientes" antes de tocar nada.
- **Al terminar una tarea**: añadir una entrada en "Registro de sesiones" y, si hubo una
  decisión con alternativas, otra en "Registro de decisiones".
- **Formato de decisión**: fecha, decisión, por qué (contexto/problema), para qué (objetivo),
  alternativas descartadas y motivo. Si una decisión se revierte, no se borra: se marca como
  `REVERTIDA` con enlace a la nueva.
- **Compactar**: solo cuando Vic lo pida. Se agrupan sesiones antiguas en un resumen, se mantiene
  íntegro el registro de decisiones.

---

## Estado actual

- **Fecha**: 2026-09-07
- **Repo**: `vicvilchez05-eng/test`, rama de trabajo `claude/android-personal-setup-hm95yj`.
- **Qué hay**: esqueleto de app Android (Kotlin + Jetpack Compose + Material 3) que compila,
  pasa tests y lint, y genera APK debug y release. Sin funcionalidad todavía.
- **Nombre y paquete**: `PersonalApp` / `com.personal.app`, **provisionales** hasta conocer la
  funcionalidad de la app.
- **Entorno**: hook de arranque que instala el SDK de Android en cada sesión web de Claude Code.
- **Siguiente paso**: Vic entrega la especificación completa de la app.

## Pendientes / preguntas abiertas

- [ ] Recibir la especificación de la app (qué hace, datos locales, internet/APIs, distribución).
- [ ] Decidir nombre definitivo y paquete (`applicationId`), renombrar `com.personal.app`.
- [ ] Decidir si la app vive en este repo (`test`) o en un repo propio.
- [ ] Decidir si el hook de arranque pasa a modo asíncrono (arranque más rápido, riesgo de
  usar Gradle antes de que el SDK esté listo).
- [ ] Fusionar la rama en `main` para que el hook aplique a todas las sesiones.

---

## Registro de decisiones

### D-001 · 2026-09-07 · Usar el repo `test` en vez de crear uno nuevo
- **Decisión**: montar el proyecto en `vicvilchez05-eng/test`, rama `claude/android-personal-setup-hm95yj`.
- **Por qué**: el repo estaba vacío (solo un README de 6 bytes) y era el único con acceso en la
  sesión. Vic dijo "si es necesario" crear otro repo; no era necesario para empezar.
- **Para qué**: no bloquear la preparación del entorno esperando una decisión de nombre.
- **Descartado**: crear repo nuevo. Es una acción externa que conviene hacer con el nombre
  definitivo de la app, no con un placeholder. Queda como pendiente.

### D-002 · 2026-09-07 · Kotlin + Jetpack Compose + Material 3, sin XML de layouts
- **Decisión**: UI 100 % Compose, un solo módulo `:app`.
- **Por qué**: es el stack recomendado por Google desde 2023 y el que mejor se mantiene a largo
  plazo. Para una app personal no hay motivo para arrastrar Views/XML.
- **Para qué**: menos código, previews en el IDE, tema dinámico (Material You) gratis.
- **Descartado**: Views/XML clásico (más verboso, en mantenimiento), Flutter/React Native
  (añaden toolchain ajeno a Android sin beneficio para uso personal en un solo sistema).

### D-003 · 2026-09-07 · Toolchain fijado en AGP 8.13.2 / compileSdk 36, no en lo último
- **Decisión**: AGP 8.13.2, Gradle 8.14.3, Kotlin 2.4.10, Compose BOM 2025.08.00, core-ktx 1.16.0,
  activity-compose 1.10.1, lifecycle 2.9.2. compileSdk = targetSdk = 36.
- **Por qué**: el primer intento con las librerías más nuevas (core-ktx 1.19, activity 1.13,
  BOM 2026.08) falló: exigen compileSdk 37 y AGP 9.1+. AGP 9 trae cambios de rotura (Kotlin
  integrado, plugins distintos) que no aportan nada hoy.
- **Para qué**: un entorno probado que compile a la primera en cada sesión y no nos haga
  perder tiempo depurando el toolchain en vez de la app.
- **Descartado**: AGP 9.4 + compileSdk 37 + BOM 2026.x. Se revisará cuando necesitemos una API
  que solo exista ahí. Al actualizar hay que subir a la vez: AGP, Gradle, `platforms;android-37`
  en el hook y las librerías.
- **Nota técnica**: Kotlin 2.4 eliminó `kotlinOptions { jvmTarget }`; se usa
  `kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }`.

### D-004 · 2026-09-07 · minSdk 26 (Android 8.0)
- **Decisión**: `minSdk = 26`.
- **Por qué**: cubre prácticamente cualquier móvil en uso en 2026 y permite icono adaptativo
  vectorial sin generar PNG para cada densidad.
- **Para qué**: menos recursos que mantener, APK más pequeño.
- **Descartado**: minSdk 24 (exigiría PNGs de fallback para el icono y no gana dispositivos
  reales para uso personal).
- **Nota técnica**: lint avisa de que la carpeta `mipmap-anydpi-v26` es "innecesaria" con
  minSdk 26, pero al renombrarla a `mipmap-anydpi` AAPT no encuentra el icono y el build
  falla. Se mantiene `-v26` y se ignora el aviso. No volver a intentarlo.

### D-005 · 2026-09-07 · Release firmado con la clave de debug, R8 activado
- **Decisión**: `assembleRelease` usa `signingConfigs.getByName("debug")`, con minify y
  shrinkResources activados.
- **Por qué**: uso personal, sin Play Store. Firmar con debug permite instalar el APK
  directamente con `adb install` o desde el móvil sin gestionar keystores.
- **Para qué**: tener un APK optimizado (772 KB frente a 10,7 MB del debug) sin fricción.
- **Descartado**: keystore propio (necesario solo si se publica o se quiere actualizar sobre
  una instalación firmada distinta). Si algún día se publica, crear keystore y guardar la
  contraseña fuera del repo.
- **Riesgo conocido**: la clave de debug se regenera por máquina. Si el APK se instala desde
  otro entorno, Android obligará a desinstalar antes de actualizar. Aceptable por ahora.

### D-006 · 2026-09-07 · Hook de arranque síncrono que instala el SDK en `/opt/android-sdk`
- **Decisión**: `.claude/hooks/session-start.sh`, registrado en `.claude/settings.json`,
  se ejecuta solo en Claude Code web (`CLAUDE_CODE_REMOTE=true`). Instala command-line
  tools, `platform-tools`, `platforms;android-36`, `build-tools;35.0.0`, escribe
  `local.properties`, exporta `ANDROID_HOME` y precompila para calentar la caché de Gradle.
- **Por qué**: el contenedor de cada sesión es efímero y no trae SDK de Android. Sin el hook,
  cada sesión empezaría reinstalando a mano.
- **Para qué**: que `./gradlew assembleDebug` funcione desde el primer minuto de cada sesión.
- **Descartado**: modo asíncrono (arranca antes, pero Claude podría lanzar Gradle antes de que
  el SDK exista). Pendiente de decidir con Vic.
- **Validado**: con SDK ya presente tarda 4 s; desde cero, 56 s. Idempotente.

### D-007 · 2026-09-07 · Catálogo de versiones y caché de Gradle
- **Decisión**: todas las versiones en `gradle/libs.versions.toml`; `org.gradle.caching` y
  `org.gradle.configuration-cache` activados en `gradle.properties`.
- **Por qué**: un único sitio para actualizar dependencias; builds repetidos mucho más rápidos.
- **Para qué**: evitar versiones duplicadas o inconsistentes y acortar cada iteración.

### D-008 · 2026-09-07 · HANDOFF.md como registro obligatorio
- **Decisión**: este archivo se lee al empezar cada sesión y se actualiza al cerrar cada tarea.
  Regla recogida también en `CLAUDE.md` para que Claude la aplique siempre.
- **Por qué**: petición explícita de Vic. El proyecto avanzará por sesiones separadas en el
  tiempo y con contexto que se pierde; sin registro se repiten errores y se olvidan motivos.
- **Para qué**: que cualquiera de los dos retome el proyecto en cualquier momento sin perder
  la pista. Solo se compacta cuando Vic lo pida, preservando siempre las decisiones.

---

## Registro de sesiones

### S-001 · 2026-09-07 · Preparación del entorno
- **Petición de Vic**: "vamos a empezar con otro proyecto [...] app para Android, de momento
  para uso personal, ve preparando el entorno".
- **Hecho**:
  - Instalado Android SDK en `/opt/android-sdk` (cmdline-tools 11076708, platform-tools 37.0.1,
    platforms 35 y 36, build-tools 35.0.0). JDK 21 y Gradle 8.14.3 ya venían en el contenedor.
  - Creado el proyecto: `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`,
    catálogo de versiones, manifest, `MainActivity.kt` con pantalla de saludo, tema
    Material 3 con color dinámico, icono adaptativo vectorial, test JUnit de ejemplo.
  - Generado el Gradle wrapper 8.14.3.
  - Creado el hook de arranque y su registro en `.claude/settings.json`.
  - Escritos `README.md` (comandos y versiones) y `CLAUDE.md` (convenciones para Claude).
- **Problemas y cómo se resolvieron**:
  1. `kotlinOptions` da error en Kotlin 2.4 → migrado a `compilerOptions` (ver D-003).
  2. Librerías AndroidX más nuevas exigen compileSdk 37 / AGP 9 → fijadas versiones 2025 y
     compileSdk 36 (ver D-003).
  3. Renombrar `mipmap-anydpi-v26` rompe el build → revertido (ver D-004).
- **Resultado**: `assembleDebug`, `assembleRelease`, `testDebugUnitTest` y `lintDebug` en verde.
  Commit `aed6501` pusheado a `claude/android-personal-setup-hm95yj`.
- **Lint**: 7 avisos, ninguno bloqueante (versiones más nuevas disponibles y el `-v26`).

### S-002 · 2026-09-07 · Creación de HANDOFF.md
- **Petición de Vic**: "todo debe quedar documentado, tanto para mí como para ti, el handoff.md
  debe ser nuestro aliado, siempre registro del porqué y para qué de todo".
- **Hecho**: creado este archivo con las decisiones D-001 a D-008 y las sesiones S-001 y S-002.
  `CLAUDE.md` actualizado con la regla de lectura/actualización obligatoria. Referencia añadida
  en `README.md`.
- **Resultado**: commit pusheado a `claude/android-personal-setup-hm95yj`.
