# HANDOFF.md — Diario de proyecto

Documento vivo y compartido entre Vic y Claude. Aquí queda **todo**: qué se hizo, por qué se hizo
y para qué sirve. Nada se decide sin dejar rastro. Para que no crezca sin límite y se coma el
contexto de Claude, al superar las **1000 líneas** se borran las fases más antiguas que ya estén
terminadas (ver "Compactar" abajo y D-009).

## Cómo usar este archivo

- **Al empezar una sesión**: leer "Estado actual" y "Pendientes" antes de tocar nada.
- **Al terminar una tarea**: añadir una entrada en "Registro de sesiones" y, si hubo una
  decisión con alternativas, otra en "Registro de decisiones".
- **Formato de decisión**: fecha, decisión, por qué (contexto/problema), para qué (objetivo),
  alternativas descartadas y motivo. Si una decisión se revierte, no se borra: se marca como
  `REVERTIDA` con enlace a la nueva.
- **Compactar** (regla D-009): al empezar una sesión, `wc -l HANDOFF.md`. Si supera **1000 líneas**:
  1. Borrar las entradas de sesión (S-nnn) más antiguas, empezando por la primera, **solo si todo
     lo que describen está terminado** (sin pendientes abiertos que dependan de ellas).
  2. De las decisiones (D-nnn) de esas fases, borrar las revertidas u obsoletas; las que siguen
     afectando al código se reducen a una línea: id, decisión y el porqué en pocas palabras.
  3. Parar en cuanto el archivo baje de ~600 líneas, para no compactar más de lo necesario.
  4. Anotar en la sesión en curso qué rango se compactó (por ejemplo "compactadas S-001..S-012").
  "Estado actual" y "Pendientes" siempre reflejan la verdad completa, así que borrar sesiones
  cerradas no pierde nada que siga importando. Fuera de ese umbral, no se compacta.

---

## Estado actual

- **Fecha**: 2026-09-07
- **Repo**: `vicvilchez05-eng/test`, rama de trabajo `claude/android-personal-setup-hm95yj`.
- **Qué es**: app de **finanzas personales** (ver "Plan por fases"). Kotlin + Jetpack Compose +
  Material 3.
- **Fase 1 terminada, pendiente de feedback de Vic**: fondo de blobs animado, tema liquid glass,
  barra inferior burbuja que se encoge al bajar y se expande al subir, y las 5 pantallas vacías
  (Home, Accounts, Total Balance, Settings, Profile) con navegación.
- **Verificación**: compila (debug y release), tests y lint en verde, y capturas de pantalla
  reales generadas en JVM con Roborazzi (`./gradlew recordRoborazziDebug` → `app/screenshots/`).
- **Nombre y paquete**: `PersonalApp` / `com.personal.app`, **provisionales**.
- **Entorno**: hook de arranque que instala el SDK de Android en cada sesión web de Claude Code.
- **Siguiente paso**: Vic revisa la Fase 1 y da el visto bueno (o cambios) antes de la Fase 2.

## Plan por fases (especificación de Vic, 2026-09-07)

Requisitos visuales comunes: estética premium, moderna y elegante. Fondo con blobs animados,
lentos y sutiles. Tarjetas y elementos estilo "liquid glass" (glassmorphism, como iOS moderno:
translucidez, bordes sutiles, sombras suaves). Barra inferior flotante tipo burbuja que se encoge
al hacer scroll hacia abajo y recupera su tamaño al subir. Pantallas: Home, Accounts, Total
Balance, Settings, Profile. Se desarrolla **por fases y Vic da feedback entre fase y fase**.

- **Fase 1 · Setup y arquitectura de UI** ✅ (S-004): framework, fondo animado, estilo glass,
  barra burbuja con lógica de scroll, 5 pantallas vacías con navegación.
- **Fase 2 · Datos e integración bancaria**: modelos de Accounts, Transactions y Balances.
  Sincronización primaria con Open Banking en modo mock/sandbox (Plaid o Tink) para tarjetas,
  saldos en tiempo real y transacciones. Sincronización secundaria manual: formulario glass para
  ingresos/gastos si el usuario no quiere vincular banco. Entregable: lógica de datos, plantillas
  de integración de API y UI de entrada manual.
- **Fase 3 · Pantallas y lógica**: Home (últimas transacciones, botones de añadir rápido,
  resumen). Accounts y Total Balance (tarjetas bancarias y saldos detallados en glass). Profile y
  Settings (preferencias, selección de moneda, cambio de tema). Entregable: componentes de cada
  pantalla alimentados con los modelos de la Fase 2.
- **Fase 4 · Informes y exportación**: resúmenes semanales y mensuales, gráficas simples y
  elegantes (líneas o barras) en Total Balance, exportación a PDF estructurado y CSV.
  Entregable: algoritmos de informes y utilidades de exportación.

## Pendientes / preguntas abiertas

- [ ] **Feedback de Vic sobre la Fase 1** antes de empezar la Fase 2.
- [ ] Decidir nombre definitivo y paquete (`applicationId`), renombrar `com.personal.app`.
- [ ] Fase 2: elegir proveedor de Open Banking sandbox (Plaid vs Tink) y si Vic tiene cuenta.
- [ ] Probar en un móvil real: rendimiento del fondo (blur + 4 gradientes por frame) y tacto del
  spring de la barra. Solo se ha verificado en capturas estáticas.
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
  la pista.
- **Actualizada por D-009**: la compactación ya no espera a que Vic la pida; se dispara sola a
  las 1000 líneas.

### D-009 · 2026-09-07 · Compactación automática a partir de 1000 líneas
- **Decisión**: cuando `HANDOFF.md` supere 1000 líneas, Claude borra las fases más antiguas ya
  terminadas hasta bajar de ~600 líneas. Procedimiento exacto en "Cómo usar este archivo".
- **Por qué**: petición de Vic. Un handoff enorme se lee entero en cada sesión y consume tokens
  y contexto que hacen falta para trabajar en la app.
- **Para qué**: mantener el archivo útil y barato de leer sin perder lo que sigue vigente.
- **Interpretación de "borrar fases"**: se borran las sesiones cerradas completas. Las
  decisiones que aún condicionan el código no se borran, se comprimen a una línea, porque el
  porqué es lo que Vic pidió no perder nunca. Si Vic prefiere borrarlas también, se cambia aquí.
- **Descartado**: compactar solo a petición (D-008 original): obliga a Vic a vigilar el tamaño.
  Resumir en vez de borrar: el resumen sigue creciendo y no ataja el problema.

### D-010 · 2026-09-07 · "Glassmorphism CSS" se traduce a modificadores Compose, sin blur de fondo real
- **Decisión**: `GlassSurface` = relleno degradado translúcido + borde degradado de 1dp (más
  brillante arriba-izquierda) + brillo radial en la esquina + línea clara en el borde superior.
  **No** se aplica desenfoque del contenido de detrás (backdrop blur).
- **Por qué**: el brief habla de CSS porque está pensado para web; en Android nativo el backdrop
  blur exige `RenderEffect` (API 31+) y una pasada de render extra por cada tarjeta. Como el
  fondo de blobs ya está desenfocado, la simple translucidez se lee como cristal.
- **Para qué**: 60 fps en cualquier móvil y el mismo aspecto en API 26 y en API 36.
- **Descartado**: backdrop blur real por tarjeta (caro, solo API 31+); librerías de terceros tipo
  Haze (dependencia extra para un efecto que ya conseguimos).

### D-011 · 2026-09-07 · Sombra suave propia, recortada del interior de la tarjeta
- **Decisión**: `Modifier.glassShadow`: 10 rectángulos redondeados apilados, cada vez mayores y
  más tenues, dibujados con `clipPath(Difference)` para que no pinten dentro de la tarjeta.
- **Por qué**: la sombra estándar (`Modifier.shadow`) se ve a través de superficies translúcidas
  y las oscurece; el `dropShadow` de Compose 1.9 rellena también el interior.
- **Para qué**: sombra que funciona en todas las APIs y en Robolectric, sin ensuciar el cristal.

### D-012 · 2026-09-07 · Fondo de blobs en Canvas con trayectorias Lissajous
- **Decisión**: `BlobBackground`: 4 círculos con degradado radial que se desvanece, cada uno con
  su ciclo de 23 a 37 s (cos en X, sin(2t) en Y), radio con un pulso del 6 %, y `Modifier.blur`
  de 56dp sobre todo el canvas (solo API 31+; en menores el degradado ya es suave).
- **Por qué**: "lento y no distrae" → ciclos largos y desfasados, sin saltos al reiniciar el loop
  (frecuencias enteras). Un solo `Canvas` es más barato que cuatro composables animados.
- **Para qué**: sensación fluida constante con coste fijo por frame.
- **Parámetros**: `animated` y `blur` se desactivan en tests para capturas deterministas.

### D-013 · 2026-09-07 · Barra burbuja controlada por NestedScrollConnection en la raíz
- **Decisión**: `NavBarScrollState` instalado con `Modifier.nestedScroll` en el contenedor de
  todas las pantallas. Mide el scroll **consumido** en `onPostScroll`, con umbral de 24 px y
  reinicio al cambiar de dirección; al llegar arriba siempre se expande. Un único `progress`
  animado con spring mueve altura (72→54dp), márgenes (20→44dp), tamaño de icono y opacidad de
  etiquetas a la vez.
- **Por qué**: ninguna pantalla tiene que saber que existe la barra; basta con que use un
  `LazyColumn`. Usar el scroll consumido (y no el gesto) evita que la barra se encoja en una
  lista que no puede moverse (detectado en la primera captura).
- **Para qué**: comportamiento uniforme en las 5 pantallas y en las futuras.
- **Descartado**: `onPreScroll` con el delta del gesto (primera versión); `TopAppBarScrollBehavior`
  de Material (pensado para barras superiores, acopla la UI a Material).

### D-014 · 2026-09-07 · Navigation Compose con rutas de texto
- **Decisión**: `navigation-compose` 2.9.8, rutas `String` en el enum `Destination`, una entrada
  de back stack por pestaña con `saveState`/`restoreState`, transición cross-fade.
- **Por qué**: las rutas tipadas exigen el plugin kotlinx-serialization; con 5 destinos planos
  no compensa. El cross-fade mantiene el fondo quieto, que es lo que da la sensación fluida.
- **Para qué**: navegación estándar, fácil de ampliar con sub-pantallas en Fase 3.

### D-015 · 2026-09-07 · Color dinámico (Material You) desactivado
- **Decisión**: paleta fija (índigo, violeta, teal, rosa) en claro y oscuro.
- **Por qué**: el look glass depende de esa paleta; los colores del fondo de pantalla del usuario
  chocarían con ella.
- **Para qué**: aspecto consistente y controlado.

### D-016 · 2026-09-07 · Capturas en JVM con Roborazzi como verificación visual
- **Decisión**: `ScreenshotTest` (Robolectric 4.16.1 + Roborazzi 1.73.0, SDK 35, Pixel 7) genera
  PNG de cada pantalla, en claro y oscuro y con la barra encogida. `app/screenshots/` está en
  `.gitignore`.
- **Por qué**: no hay emulador en el entorno web (sin KVM). Sin capturas, Claude estaría
  diseñando a ciegas. Los PNG pesan ~2 MB cada uno; no se versionan.
- **Para qué**: ver el resultado real antes de enseñárselo a Vic y detectar regresiones visuales.
- **Comando**: `./gradlew recordRoborazziDebug`.

### D-017 · 2026-09-07 · Textos en inglés por defecto con traducción al español
- **Decisión**: `values/strings.xml` en inglés (nombres de pantalla del brief) y `values-es/`.
- **Por qué**: el brief usa nombres en inglés; el móvil de Vic probablemente esté en español.
- **Para qué**: la app se ve en el idioma del sistema sin tocar código.

### D-018 · 2026-09-07 · `material-icons-extended` para los iconos de la barra
- **Decisión**: dependencia completa de iconos extendidos.
- **Por qué**: "Wallet" e "Insights" no están en el set básico. R8 elimina los no usados en
  release, así que el peso solo afecta al APK de debug.

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
- **Resultado**: commit `03ff3e0` pusheado a `claude/android-personal-setup-hm95yj`.

### S-003 · 2026-09-07 · Regla de compactación automática
- **Petición de Vic**: "al llegar a las mil líneas hay que ir borrando las primeras fases siempre
  que esté todo terminado así no te comes tokens ni contexto por un handoff enorme".
- **Hecho**: procedimiento de compactación escrito en "Cómo usar este archivo", decisión D-009
  registrada, D-008 marcada como actualizada, regla añadida a `CLAUDE.md` con la comprobación
  `wc -l` al inicio de sesión.
- **Resultado**: commit `c038d15` pusheado a `claude/android-personal-setup-hm95yj`.

### S-004 · 2026-09-07 · Fase 1: fondo animado, glass, barra burbuja y pantallas vacías
- **Petición de Vic**: brief completo de la app de finanzas personales en 4 fases (copiado en
  "Plan por fases"). "Start by executing Phase 1 only [...] Wait for my feedback before moving
  to Phase 2."
- **Hecho**:
  - Tema: `Color.kt`, `Type.kt` (títulos compactos y pesados), `Glass.kt` (tokens `GlassTokens`
    claro/oscuro + `BlobSpec`), `Theme.kt` (provee `LocalGlass`, sin color dinámico).
  - Componentes: `BlobBackground`, `GlassSurface`/`GlassCard`/`glassShadow`, `BubbleNavBar`,
    `NavBarScrollState`.
  - Navegación: `Destination` (enum de 5 pestañas), `AppNavHost`, `navigateToTab`.
  - Pantallas: `PlaceholderScreen` compartido y las 5 pantallas en `Screens.kt` con secciones
    que anuncian en qué fase llegan.
  - `FinanceApp` (raíz: fondo → pantalla → barra) y `MainActivity` edge-to-edge.
  - Dependencias nuevas: navigation-compose, material-icons-extended, robolectric, roborazzi,
    ui-test-junit4, androidx.test.ext.junit.
  - Strings EN + ES.
- **Problemas y cómo se resolvieron**:
  1. `animateFloat` sin resolver → es una extensión, faltaba el import.
  2. Aviso de Gradle 8.14.3 "deprecated" por Kotlin 2.4 → silenciado en `gradle.properties`
     (`kotlin.suppressGradlePluginWarnings`); Gradle 8.14.4 queda como mejora futura.
  3. Primera captura: la barra se encogía con el gesto aunque la lista no se moviera → pasar a
     scroll consumido (D-013). Home tenía poco contenido para hacer scroll → 3 secciones más.
  4. Tarjeta tintada en modo claro demasiado saturada → alfa fija del tinte (0,30/0,14 claro,
     0,36/0,16 oscuro) en vez de derivarla del relleno.
  5. Lint `MissingTranslation` en `app_name` → `translatable="false"`.
  6. Lint `UseOfNonLambdaOffsetOverload` en el indicador → `offset { IntOffset(...) }`.
- **Resultado**: `assembleDebug`, `assembleRelease`, `testDebugUnitTest` (5 tests, 4 de captura)
  y `lintDebug` en verde. 7 capturas generadas y enviadas a Vic. Commit pusheado a
  `claude/android-personal-setup-hm95yj`.
- **Avisos de lint que quedan** (no bloqueantes): versiones más nuevas disponibles (por D-003)
  y el `-v26` del icono (por D-004).
