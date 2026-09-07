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
- **Fase 1 terminada con la identidad visual de Esforia** (S-007, D-021), pendiente del
  feedback de Vic: paleta y tipografías de Esforia (Sora/Manrope/IBM Plex Mono), fondo
  ambiental de 3 blobs desenfocados en los márgenes con tono por pantalla, hero en degradado
  morado con stat pills, tarjetas opacas con línea fina, grupos de filas con teja de icono, y la
  barra flotante que se encoge con el scroll (exigencia del brief) vestida al estilo Esforia.
- **La identidad visual es la de Esforia**, documentada en `docs/design/identidad-esforia.md`
  (tokens exactos, tipografía, superficies, fondo, estructura). Léela antes de tocar la UI.
- **Referencia de código**: el repo `vicvilchez05-eng/esforia-app` (React/Capacitor) de Vic.
  Solo lectura: **no se modifica**. Se añade a la sesión con `add_repo` y se clona en
  `/home/user/esforia-app`. Archivos clave: `src/contexts/palettes.ts`, `src/services/themes.ts`,
  `src/styles/app.css`, `src/components/ui/*`, `src/pages/{Home,Finance,Settings}.tsx`.
- La guía de imagen `docs/design/guia-visual-vic-2026-09-07.png` queda como referencia
  **secundaria** (Vic la descartó en favor de Esforia, ver D-021).
- **Guía visual de referencia**: `docs/design/guia-visual-vic-2026-09-07.png`. Toda decisión
  de estilo se contrasta con ella. Léela (Read) antes de tocar la UI.
- **Verificación**: compila (debug y release), tests y lint en verde, y capturas de pantalla
  reales generadas en JVM con Roborazzi (`./gradlew recordRoborazziDebug` → `app/screenshots/`).
- **Nombre y paquete**: `PersonalApp` / `com.personal.app`, **provisionales**.
- **Entorno**: hook de arranque que instala el SDK de Android en cada sesión web de Claude Code.
- **Fase 2 aprobada por Vic** ("quedó hermoso, continúa con las demás fases").
- **Fase 3 terminada** (S-011): preferencias persistentes (nombre, moneda, tema, privacidad,
  notificaciones), Ajustes y Perfil funcionales, detalle de cuenta, lista completa de
  movimientos con borrado, desvincular banco, borrar todo.
- **Fase 4 terminada** (S-012): informes semanal/mensual, serie de 6 meses, evolución de 30 días,
  Balance completo, exportación CSV y PDF con hoja de compartir.
- **Las 4 fases del brief están hechas.** Después: deslizar entre pestañas (S-013, D-030),
  revisión de bugs, y **lectura de notificaciones de BBVA** con buzón de revisión (S-014, D-031).
- **Pendiente de Vic**: conceder el acceso a notificaciones en el móvil y, cuando llegue un aviso
  real de BBVA, pegarlo en Ajustes → "Leer notificaciones del banco" → "Pruébalo con un texto"
  para validar el analizador (los formatos son supuestos). Rama sin fusionar en `main`.

## Plan por fases (especificación de Vic, 2026-09-07)

Requisitos visuales comunes: estética premium, moderna y elegante. Fondo con blobs animados,
lentos y sutiles. Tarjetas y elementos estilo "liquid glass" (glassmorphism, como iOS moderno:
translucidez, bordes sutiles, sombras suaves). Barra inferior flotante tipo burbuja que se encoge
al hacer scroll hacia abajo y recupera su tamaño al subir. Pantallas: Home, Accounts, Total
Balance, Settings, Profile. Se desarrolla **por fases y Vic da feedback entre fase y fase**.

- **Fase 1 · Setup y arquitectura de UI** ✅ (S-004…S-009): framework, fondo animado, identidad
  Esforia, barra burbuja con lógica de scroll, 5 pantallas con navegación.
- **Fase 2 · Datos e integración bancaria** ✅ (S-010, sandbox local, sin Plaid/Tink): modelos de Accounts, Transactions y Balances.
  Sincronización primaria con Open Banking en modo mock/sandbox (Plaid o Tink) para tarjetas,
  saldos en tiempo real y transacciones. Sincronización secundaria manual: formulario glass para
  ingresos/gastos si el usuario no quiere vincular banco. Entregable: lógica de datos, plantillas
  de integración de API y UI de entrada manual.
- **Fase 3 · Pantallas y lógica** ✅ (S-011): Home (últimas transacciones, botones de añadir rápido,
  resumen). Accounts y Total Balance (tarjetas bancarias y saldos detallados en glass). Profile y
  Settings (preferencias, selección de moneda, cambio de tema). Entregable: componentes de cada
  pantalla alimentados con los modelos de la Fase 2.
- **Fase 4 · Informes y exportación** ✅ (S-012): resúmenes semanales y mensuales, gráficas simples y
  elegantes (líneas o barras) en Total Balance, exportación a PDF estructurado y CSV.
  Entregable: algoritmos de informes y utilidades de exportación.

## Pendientes / preguntas abiertas

- [x] Vic eligió: Esforia + estructura de Home de NavyGold (S-009). Fase 2 puede empezar.
- [ ] Decidir si esta app adopta también la marca Esforia (icono teja morada con "E", splash
  morado, nombre) o solo la identidad visual. Por ahora el icono solo toma el morado `#6C5CE7`.
- [ ] Decidir si se portan los temas de Esforia (rosa, sakura, lluvia, bosque, nieve, custom) y
  sus decoraciones. Hoy solo existe el tema por defecto "ritmo" en claro y oscuro.
- [ ] Decidir nombre definitivo y paquete (`applicationId`), renombrar `com.personal.app`.
- [x] Fase 2: proveedor Open Banking → Vic no conoce Plaid/Tink y no quiere darse de alta.
  Sandbox local (`MockBankProvider`) + plantilla documentada (`OpenBankingProviderTemplate`).
- [x] Feedback de Vic sobre la Fase 2 → aprobada, continuar con 3 y 4 sin parar.
- [x] Nombre del saludo → editable en Perfil (`UserPreferences.name`); vacío muestra "¡Hola!".
- [x] Exportación PDF **verificada por Vic en el móvil** (2026-09-08, captura del visor): dos
  páginas correctas, cabecera de tabla repetida, numeración. El test sigue omitido en Robolectric.
- [ ] Cosmética del sandbox vista en ese PDF (no pedida): la cuenta "Neo Account" acaba en
  negativo (saldo inicial 830 € frente a ~2.000 €/mes de gasto) y facturas/suscripciones
  (Vodafone, Spotify) se repiten varios días seguidos e incluso el mismo día. Arreglo barato:
  subir el saldo inicial de neo y hacer que facturas y suscripciones caigan en un día fijo del
  mes. Vic dijo "mejor lo dejamos así"; queda anotado por si se retoma.
- [ ] Ideas de continuación (no pedidas): presupuestos por categoría, metas de ahorro,
  recordatorio de registro, keystore propio para actualizar sin desinstalar (D-005),
  icono/splash propios, temas de Esforia.
- [x] El interruptor "Notificaciones" muerto se ha sustituido por la lectura de notificaciones
  del banco (D-031). Recordatorios/resúmenes push siguen sin existir; decidir en el futuro.
- [ ] **Validar el analizador BBVA con avisos reales** (Vic no tenía ninguno a mano). Cada
  formato nuevo se añade como caso en `BankNotificationParserTest`. Si Vic usa otro banco,
  añadir su paquete a `BankNotificationParser.bankApps`.
- [ ] OPPO/ColorOS mata servicios en segundo plano con agresividad: si las notificaciones no
  llegan al buzón, hay que quitar la app de la optimización de batería (Ajustes → Batería →
  la app → Sin restricciones) y permitir inicio automático.
- [ ] Multi-moneda: los totales suman céntimos sin convertir. Si Vic mezcla monedas, hará
  falta una tabla de cambio (manual o API).
- [ ] Vigilar KSP para Kotlin 2.4.x: si aparece, valorar Room (D-025 lo deja preparado).
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

### D-019 · 2026-09-07 · Giro visual: se adopta la guía de Vic (lavanda, gotas 3D, blanco esmerilado) · **REVERTIDA por D-021**
- **Decisión**: se abandona el look oscuro y saturado de S-004 y se reconstruye la capa visual
  siguiendo `docs/design/guia-visual-vic-2026-09-07.png`:
  - **Fondo**: base lavanda muy clara (`#E2E5F6`) con 5 gotas de cristal en periwinkle, champán
    y lila. Cada gota es una elipse sombreada como una cuenta de vidrio (lado iluminado → cuerpo
    → borde profundo, banda de refracción, brillo amplio y punto especular), con deriva Lissajous
    lenta más un leve bamboleo de rotación y aplastamiento. Blur de canvas de solo 3dp (API 31+).
  - **Tarjetas**: blanco al 82→70 %, radio 16dp, borde blanco arriba y línea azul marino al 8 %
    abajo, sombra muy suave (alfa 0,10). Listas agrupadas en una tarjeta con filas de 44dp y
    separadores finos, como los grupos de ajustes de la guía.
  - **Tipografía**: Inter (OFL, 4 pesos en `res/font`, ~1,7 MB). Cabeceras de pantalla en
    versalitas con tracking (`labelLarge`), números grandes en Bold con tracking negativo.
  - **Texto**: azul marino `#1B2140` y gris azulado `#6B7194`. Acento verde `#2E9E5B` para el
    chip "+ Trend".
  - **Barra inferior**: píldora blanca esmerilada de 64dp (50dp encogida), iconos Outlined de
    22dp con etiqueta de 10sp, y un círculo blanco elevado de 40dp que se desliza bajo el icono
    activo. Encogida: 50dp, sin etiquetas, márgenes 56dp.
  - **Cabecera**: `ScreenHeader` de 48dp con título centrado en versalitas y acciones de icono
    a izquierda (atrás) y derecha (sync, campana, +). Solo visual en Fase 1.
  - **Modo oscuro**: traducción fiel (base `#11152A`, tarjetas azul marino esmerilado al 82 %,
    gotas con tonos "Night" más saturados para no ensuciarse sobre azul marino). La guía es
    solo clara; el oscuro existe porque la guía muestra un toggle "Dark Mode".
- **Por qué**: Vic dijo "no me gusta el estilo visual, te dejo una guía". La guía es luminosa,
  limpia y de contraste bajo; lo de S-004 era oscuro, saturado y con blobs planos.
- **Para qué**: que cada pantalla futura tenga una referencia concreta que imitar en vez de un
  adjetivo ("premium").
- **Descartado**: mantener el tema oscuro como principal (la guía es clara); backdrop blur real
  (sigue sin compensar, ver D-010); fuente del sistema (Inter es lo que da el acabado de la guía
  y pesa poco tras R8).
- **Actualiza**: D-012 (blobs planos → gotas 3D), D-015 (paleta índigo/violeta/teal/rosa →
  periwinkle/champán/lila), D-018 (iconos Rounded → Outlined). Las tres siguen vigentes en lo
  demás.

### D-031 · 2026-09-08 · Lectura de notificaciones bancarias con buzón de revisión (BBVA)
- **Decisión**: `BankNotificationListener` (`NotificationListenerService`, permiso
  `BIND_NOTIFICATION_LISTENER_SERVICE`, el usuario lo concede a mano en "Acceso a
  notificaciones"). Solo procesa paquetes de `BankNotificationParser.bankApps`
  (`com.bbva.bbvacontigo`, `com.bbva.netcash`) o que contengan "bbva"; el resto ni se lee.
  `BankNotificationParser` saca importe (regex de euros con miles/decimales, símbolo antes o
  después), signo por palabras clave (ingreso/abono/nómina/recibido → ingreso; compra/pago/
  recibo/retirada → gasto), comercio ("en X", "de X por", "recibo de X") y categoría por
  diccionario de comercios. Cada aviso entra como `CapturedTransaction` en `FinanceData.inbox`
  (id SHA-1 de paquete+texto+minuto → sin duplicados; máx. 200) con estado PENDING. El usuario
  lo revisa en `InboxScreen` (cuenta, signo, importe, categoría, descripción editables) y al
  guardar se crea una `Transaction` con `Source.CAPTURED` (mueve saldo de cuentas manuales como
  una manual) o lo descarta. `CaptureSettingsScreen` muestra el estado del permiso (recheck en
  `ON_RESUME`), botón a los ajustes del sistema, pausa (`UserPreferences.captureEnabled`), apps
  vigiladas y un **campo de prueba** que interpreta un texto pegado y puede enviarlo al buzón
  por el mismo camino que una notificación real. La Home muestra una tarjeta "N por revisar".
- **Por qué**: Vic preguntó por registrar gastos leyendo notificaciones; usa BBVA y no tenía
  avisos a mano, así que los formatos son supuestos y el campo de prueba es la forma de
  validarlos sin esperar a que lleguen.
- **Para qué**: registrar gastos sin teclear, sin que un falso positivo ensucie el libro.
- **Descartado**: registrar automáticamente sin revisión (falsos positivos y notificaciones
  informativas sin importe); leer SMS (BBVA ya no manda SMS de compras y el permiso es peor
  visto); un modelo de lenguaje en el móvil para interpretar (2,5 GB para unas regex).
- **Privacidad**: el texto de la notificación se guarda en el JSON local como nota del
  movimiento aceptado; nunca sale del móvil. `UserPreferences.notifications` (muerta) se
  elimina; `ignoreUnknownKeys` absorbe el campo viejo.
- **Límites**: el servicio depende de que ColorOS no lo mate (ver Pendientes); los avisos
  que lleguen con la app "pausada" o sin permiso se pierden (no hay relectura del histórico).

### D-030 · 2026-09-07 · Las pestañas son un `HorizontalPager`; la barra solo lo refleja
- **Decisión**: el `NavHost` tiene una única ruta `tabs` que contiene un `HorizontalPager` con
  las 5 pantallas (`beyondViewportPageCount = 1`, clave por ruta). Deslizar o tocar la barra
  hacen lo mismo: `animateScrollToPage`. `current` sale de `pagerState.currentPage` y arrastra
  el tono del fondo. Formularios y detalles siguen siendo rutas encima que ocultan la barra.
  Ajustes salta a Cuentas/Balance con un `onTab` que mueve el pager.
- **Por qué**: Vic: "que se pueda deslizar con el dedo de pestaña a pestaña, sin tener que estar
  tocando siempre la barra". Navigation Compose no tiene gesto horizontal entre destinos.
- **Para qué**: navegación con el pulgar y una sola fuente de verdad para "qué pestaña está activa".
- **Descartado**: detectar el gesto a mano y navegar (sin arrastre visual ni cancelación);
  mantener las pestañas como rutas y animar (pierde el gesto continuo).
- **Efectos**: ya no hay pila de atrás entre pestañas (atrás en `tabs` sale de la app, como en
  la mayoría de apps con paginador). La posición de scroll de cada pestaña se conserva mientras
  la página siga compuesta (vecinas) y se restaura por `rememberSaveable` en las demás.
- **Tests**: con el pager hay dos `screen_list` compuestos a la vez (visible + vecina), así que
  los gestos de las capturas van sobre `onRoot()`, nunca sobre la etiqueta.

### D-028 · 2026-09-07 · Exportación con `PdfDocument` + `FileProvider`, sin librerías
- **Decisión**: `ExportManager` genera el CSV (RFC 4180, coma, comillas dobladas, importes con
  punto decimal, fecha y hora separadas, más antiguo primero) y el PDF (A4, `android.graphics.pdf
  .PdfDocument`: título, cajas KPI de mes y semana, cuentas con patrimonio neto, barras por
  categoría del mes, tabla paginada de movimientos del mes con cabecera repetida y número de
  página). Archivos en `cacheDir/exports/` servidos por `FileProvider` (`file_paths.xml`) y
  entregados con `Intent.ACTION_SEND` + selector. La UI recibe un `ShareRequest` por
  `SharedFlow` y lanza el selector desde el contexto de la Activity.
- **Por qué**: sin permisos de almacenamiento ni dependencias nuevas; el usuario elige destino
  (Drive, correo, Archivos) en la hoja de compartir. Una librería PDF (iText, PdfBox-Android)
  añadiría megas y licencias para un informe de dos páginas.
- **Para qué**: informes que Vic pueda guardar o mandar, generados en local.
- **Descartado**: escribir en Descargas (MediaStore + permisos según versión); librerías PDF;
  generar HTML e imprimir (depende del servicio de impresión).
- **Limitación**: `PdfDocument` es nativo y Robolectric devuelve "document is closed", así que
  `ExportManagerTest` omite la parte PDF con `Assume`. Verificar en dispositivo.

### D-029 · 2026-09-07 · Informes como funciones puras en `domain/Reports`
- **Decisión**: `Reports.summarize/week/month/monthlySeries` devuelven `PeriodSummary`
  (ingresos, gastos, neto, por categoría, gasto por día con ceros, nº de movimientos, media
  diaria) y `MonthPoint`. Semana = lunes a domingo. `FinanceCalculator.balanceHistory`
  reconstruye el saldo diario hacia atrás (saldo actual menos lo posterior a cada día).
- **Por qué**: las mismas cifras alimentan la pantalla de Balance y el PDF; una sola fuente.
- **Para qué**: tests deterministas sin Android y coherencia entre pantalla e informe.
- **Nota**: la variación mensual del hero (`monthOverMonthPercent`) compara el neto del mes con
  el patrimonio al cierre del mes anterior; con pocos datos puede ser llamativa (−7,8 % con
  solo gastos y la nómina el 28).

### D-027 · 2026-09-07 · Preferencias en `settings.json` con el mismo almacén genérico
- **Decisión**: `JsonFileFinanceStore` se generaliza a `JsonFileStore<T>` / `Store<T>`
  (`FinanceStore` = `Store<FinanceData>`); las preferencias son `UserPreferences` en
  `settings.json` vía `PreferencesRepository`. El tema (sistema/claro/oscuro) se aplica en
  `MainActivity`; moneda y privacidad llegan a la UI por `LocalMoneyDisplay` y el helper
  `money()` (que imprime "••••" en modo privacidad).
- **Por qué**: DataStore añadiría otra dependencia para cinco campos; el almacén JSON ya
  existe, es atómico y está probado. Separar preferencias del libro permite "borrar todos los
  datos" sin perder ajustes.
- **Para qué**: un solo mecanismo de persistencia en toda la app.
- **Descartado**: DataStore Preferences, SharedPreferences (sin tipos ni migración).
- **Límites conocidos**: la moneda de visualización no convierte importes (solo etiqueta y
  moneda por defecto de cuentas manuales); "Notificaciones" se guarda pero no hace nada aún.

### D-025 · 2026-09-07 · Persistencia en un archivo JSON (kotlinx.serialization), sin Room ni DI
- **Decisión**: `FinanceStore` (interfaz) con `JsonFileFinanceStore` (un archivo
  `files/finance.json`, escritura atómica temp+rename, mutex, archivo corrupto se aparta como
  `.corrupt`) e `InMemoryFinanceStore` (tests/capturas). Todo el dataset vive en memoria como
  `StateFlow<FinanceData>`; cada escritura es una transformación pura. Sin framework de DI: un
  `AppContainer` a mano expuesto por `LocalAppContainer`; los ViewModels lo reciben por
  `appViewModel { }`.
- **Por qué**: Room necesita KSP y **no existe KSP para Kotlin 2.4.10** en los repositorios
  (último: 2.2.21-2.0.5). Degradar Kotlin o usar kapt era peor. Además Esforia es offline-first
  con localStorage: el modelo "todo en memoria + un archivo" es el mismo. Un libro personal
  de años cabe en pocos MB.
- **Para qué**: cero riesgo de toolchain, exportación (Fase 4) trivial (ya es un JSON) y
  migraciones por campo `version`.
- **Descartado**: Room (KSP), SQLDelight (plugin no verificado con Kotlin 2.4), SQLite a mano
  (más código para lo mismo hoy). Hilt (mismo problema de KSP, y sobra para una docena de objetos).
- **Reserva**: si aparece KSP para 2.4.x y el dataset crece, `FinanceStore` se reimplementa
  con Room sin tocar repositorio ni UI.

### D-026 · 2026-09-07 · Open Banking: sandbox local determinista + plantilla, sin proveedor real
- **Decisión**: interfaz `BankProvider` (instituciones, link/consentimiento, cuentas,
  movimientos desde una fecha). `MockBankProvider` genera 3 bancos con cuentas y 90 días de
  historial **deterministas** (semilla por institución y día), con alquiler el día 1 y nómina
  el 28; la sincronización repetida es idempotente porque los ids son
  `"<proveedor>:<idExterno>"`. `OpenBankingProviderTemplate` documenta paso a paso cómo se
  enchufaría Plaid/Tink/GoCardless (link token, intercambio en backend, cuentas, sync
  incremental con cursor, consentimientos PSD2 de 90 días) y lanza `TODO()`.
- **Por qué**: Vic: "no tengo ni idea de qué es Plaid o Tink así que no". Un proveedor real
  exige alta, contrato, credenciales y un backend para no meter el secreto en el APK.
- **Para qué**: que toda la app funcione de extremo a extremo hoy y que conectar un banco real
  sea implementar una clase, no rehacer la app.
- **Descartado**: Plaid/Tink sandbox real (credenciales que Vic no tiene ni quiere), datos
  aleatorios no deterministas (rompen capturas y tests).
- **Sincronización**: `FinanceRepository.sync()` refresca saldos y trae movimientos desde
  `lastSyncAt − 1 día` (solapamiento por si un movimiento se contabiliza tarde). Estado en
  `syncState` (Idle/Syncing/Error), nunca lanza.
- **Entrada manual**: `addManualTransaction` mueve el saldo solo en cuentas MANUAL; en cuentas
  vinculadas el saldo lo manda el banco.
- **Dinero**: `Long` en céntimos + código ISO; `Money.format/parseToMinor` (acepta "1.234,56",
  "1,234.56", "1234.56").

### D-024 · 2026-09-07 · Esforia en todo; la Home toma la estructura de NavyGold; NavyGold se borra
- **Decisión**: una sola identidad (Esforia, D-021). `HomeScreen` se reescribe con la estructura
  del estudio NavyGold pero con superficies Esforia: fecha + "¡Hola, Vic!" (Sora) + avatar con
  degradado de marca; `HeroCard` con etiqueta en mayúsculas, cifra Sora 32 y línea de tendencia;
  `LazyRow` de `SurfaceCard` de 128dp con teja de icono `mossSoft`, nombre y cifra en Plex Mono
  (negativo en `danger`); `SectionLabel` + `SurfaceCard` con `BarChart`; `SectionLabel` +
  `SurfaceCard` con filas de transacción (teja `mossSoft`/`emberSoft`, cifra mono, ingreso en
  `emberText`, separadores `line`). El resto de pantallas queda como en S-007.
- **Por qué**: Vic: "usaremos el estilo visual de Esforia, pero heredamos la estructura del
  home de NavyGold, solo la estructura, el aspecto visual es de Esforia, todo lo demás se queda
  como está y puedes eliminar navygold".
- **Para qué**: cerrar la Fase 1 con una sola identidad que mantener.
- **Qué se conserva de NavyGold**: `BarChart`, `DonutChart` y `Sparkline` pasan a
  `ui/components/Charts.kt` pintados con `Palette.chart` (moss, blue, ember, mossText); harán
  falta en las fases 3 y 4. `Palette` conserva los alias `positive`/`negative`/`chart`.
  `docs/design/navy-gold-home-claro-2026-09-07.png` se guarda como referencia de la estructura.
- **Qué se borra**: `Skin.kt`, `LocalSkin`, paletas y tipografía NavyGold, Inter,
  `ui/components/navygold/`, `ui/screens/navygold/`, `DockedNavBar`, dos de las tres imágenes
  del diseño (siguen en el historial de git, commit `0b3c5cb`), strings solo usados por
  NavyGold.
- **Datos de muestra**: la Home mantiene cifras ilustrativas (45.820,50 €, Chase, BBVA…) hasta
  que la Fase 2 traiga modelos. Las demás pantallas muestran ceros/placeholders.
- **Cierra**: D-022 y D-023 quedan **REVERTIDAS** por esta decisión (la arquitectura de pieles
  ya no existe; si hiciera falta volver, está en `0b3c5cb`).

### D-022 · 2026-09-07 · Arquitectura de pieles: dos identidades visuales completas conmutables · **REVERTIDA por D-024**
- **Decisión**: `enum Skin { Esforia, NavyGold }` + `LocalSkin` + `DefaultSkin`. Una piel es
  paleta + tipografía + variantes de componentes + pantallas propias donde la disposición
  difiere. `Palette` gana campos "extra" con valores por defecto que reproducen Esforia
  (`headerBand`, `heroGradient`, `heroValueGradient`, `cardGradients`, `navBackground`, `gold`,
  `positive`/`negative`, `chart`); NavyGold los sobrescribe. `PersonalAppTheme(skin)` provee
  paleta y tipografía; `AppNavHost` y `FinanceApp` eligen pantallas, fondo y barra por piel.
- **Por qué**: Vic: "todo esto no lo borres, guárdatelo y ahora te paso otro diseño
  completamente distinto para ver cómo queda". Hay que poder comparar sin destruir.
- **Para qué**: elegir con las dos en la mano y descartar después con una línea de código.
- **Descartado**: rama git por piel (no se pueden ver ambas en el mismo APK ni compartir
  arreglos); solo cambiar la paleta (los layouts difieren: banda, hero, tarjetas de cuenta,
  barra fija).
- **Coste conocido**: dos juegos de pantallas en el árbol hasta que Vic elija. Las fuentes de
  ambas pieles van en el APK (Sora+Manrope+Plex Mono ≈ 550 KB, Inter ≈ 880 KB).

### D-023 · 2026-09-07 · Piel NavyGold: traducción del diseño navy/teal/dorado · **REVERTIDA por D-024**
- **Fuente**: `docs/design/navy-gold-{claro,oscuro,home-claro}-2026-09-07.png`.
- **Paleta claro**: fondo marfil `#F3EFE4`, banda petróleo `#235A68→#163C4A`, tinta navy
  `#1B2F3B`, acento navy `#1E4C5C` (botones), dorado `#C9A85B` (saludo, subrayado, gráficas),
  hero crema→dorado `#FAF6EC→#EDE0BA→#D9C48C`, tarjetas de cuenta en degradados navy→teal→oro,
  navy→azul grisáceo, navy→bronce→oro, verde→oro. Positivo `#3F9A6B`, negativo `#D9534F`.
- **Paleta oscuro**: fondo `#16222B→#0E171E`, banda `#1B3F49→#12242C`, superficie `#1C2A35`,
  tinta marfil `#F2EFE6`, hero `#223A48→#182B37` con la **cifra en degradado dorado**
  `#F3E4B6→#D4B36A`, barra `#0F1A21`, activo dorado.
- **Tipografía**: Inter variable para todo; título 26 bold, saludo 15 medium dorado, cifra
  hero 34 bold, importe de tarjeta 24 bold, secciones 16 semibold, barra 10.
- **Estructura**: banda superior recta (inset + 132dp) con título/saludo/avatar con aro; el
  hero empieza dentro de la banda y la solapa 32dp. Home: tira horizontal de mini-tarjetas,
  barras "Gastos mensuales", lista de transacciones con icono en círculo tintado y triángulo
  verde/rojo. Cuentas: tarjetas degradado con sparkline y botón "Añadir cuenta" + "+" redondo.
  Balance (≈ "Gastos" del diseño): donut con leyenda, dos botones pequeños de contorno, lista.
  Ajustes y Perfil reutilizan `Group`/`GroupRow` con la paleta de la piel (no estaban en el
  diseño; se derivan).
- **Barra**: fija, ancho completo, línea superior, subrayado dorado sobre el icono activo.
  Sigue encogiéndose con el scroll (etiquetas se ocultan) para respetar el brief.
- **Datos de muestra**: las pantallas NavyGold muestran cifras ILUSTRATIVAS (45.820,50 €,
  Chase, BBVA…) para poder juzgar el diseño; las de Esforia muestran ceros. Fase 2 lo sustituye.
- **Descartado**: barra flotante burbuja en esta piel (el diseño la muestra fija); título
  centrado en versalitas (el diseño lo lleva a la izquierda en la banda).

### D-021 · 2026-09-07 · La identidad visual de la app es la de Esforia, calcada del repo
- **Decisión**: se abandona la guía de imagen (D-019) y se adopta íntegra la identidad de
  `esforia-app`, extraída del código y documentada en `docs/design/identidad-esforia.md`:
  - **Paleta** `Palette.kt`: copia clave por clave de `palettes.ts` (tema "ritmo"): bg
    `#F8F7FD`, surface blanco, line `#E7E3F5`, ink `#211C36`, inkSoft `#665F87`, muted
    `#9891B4`, moss `#6C5CE7` (+soft `#EBE7FD`, text `#4B3FBF`), ember, blue, danger, track,
    financeGradient `#8B5CF6→#7C6CF0→#5B7FF5`, status*, glass. Oscuro: bg degradado
    `#17122A→#0C0A16`, surface `#181229`, moss `#8B7CF6`, etc. `LocalPalette` es el token real;
    el `ColorScheme` de Material se deriva de él.
  - **Tipografía** `Type.kt`: Sora (títulos, "voice"), Manrope (cuerpo), IBM Plex Mono
    (cifras). Sora y Manrope como fuentes variables (`FontVariation.weight`), Plex Mono en dos
    pesos estáticos. Escala: título 25/500, sección 19/500, hero 32/600, fila 14,5, valor 13,
    etiqueta de grupo 11,5 MAYÚSCULAS, etiqueta de hero 11/600 tracking 0,8, barra 9,5.
  - **Fondo** `AmbientBackground.kt`: calco de `.ambient-blob`: tres slots en los márgenes
    (a 240dp arriba-izq, b 200dp a 30 % derecha, c 220dp abajo al 20 %), blur 46dp (API 31+;
    debajo, degradado radial con caída desde el 45 %), keyframes drift1/2/3 (26/32/29 s,
    ida y vuelta, escala y opacidad propias), **tono por pantalla** con los mismos colores y
    fuerzas que `TONES` (home 0,9 · finance 1 · progress 0,7 · profile 0,6 · settings 0,4),
    con cross-fade de 600 ms al cambiar de pestaña.
  - **Superficies** `Surfaces.kt`: `SurfaceCard` (opaca, line, r18, p16), `HeroCard`
    (degradado 135°, r22, p20×18, sombra moss 24 %), `HeroStat` (blanco 16 % / destacada
    30 % + borde), `SectionLabel`, `GroupLabel`, `Group`+`GroupRow` (r16, filas ≥50, teja 28
    r9 mossSoft, separador inset 54), `Chip`, `OutlineButton`, `CircleIconButton` (34),
    `EsforiaSwitch` (48×28, degradado + sombra). `GlassSurface` se conserva solo para la barra
    flotante, con los valores `glass` de Esforia. Sin ripple rectangular (`pressable`).
  - **Cabecera** `PageHeader.kt`: línea de 13sp inkSoft (fecha/eyebrow) + título Sora a la
    izquierda, botones circulares a la derecha. Se elimina el título centrado en versalitas.
  - **Barra** `BubbleNavBar.kt`: sigue flotante y encogible (brief), pero vestida como la de
    Esforia: surface 94 %, borde line, icono 19dp Outlined/Filled, etiqueta 9,5sp, moss activo /
    muted inactivo, píldora `mossSoft` deslizante bajo el icono activo.
  - **Pantallas**: `ScreenScaffold` (22+statusBar / 18 / 28), y Home, Accounts, Balance,
    Settings, Profile compuestas con esas piezas siguiendo `Home.tsx`, `Finance.tsx` y
    `Settings.tsx`. Placeholders "Phase N" como valor de fila o chip.
  - Icono de launcher: fondo `#6C5CE7`. Strings EN/ES nuevas.
- **Por qué**: Vic, tras probar el APK: "de funcionar funciona, pero no me gusta el diseño
  visual" → "Identidad visual de Esforia, analiza el proyecto más a fondo sin tocar nada de él".
  Esforia es su otra app; ya tiene un módulo de finanzas con hero, stat pills y grupos, así que
  esta app debe leerse como de la misma familia.
- **Para qué**: una fuente de verdad concreta y ya probada en producción, con contraste
  verificado (Esforia testea los acentos contra WCAG), en vez de interpretar una imagen.
- **Descartado**: tarjetas translúcidas (Esforia las hace opacas a propósito para que el
  resplandor nunca reste contraste; el glass queda solo en elementos flotantes); backdrop blur
  (Esforia lo limita a 4 elementos); Inter (sustituida por las tres fuentes de Esforia, más
  ligeras además: 550 KB frente a 1,7 MB); barra fija tipo Esforia (el brief exige flotante y
  encogible; se mantiene la conducta y se adopta el estilo).
- **Revierte**: D-019 (guía de imagen) queda **REVERTIDA** salvo la decisión de fondo de usar
  una referencia concreta; D-020 se **reespecifica** con la geometría exacta de Esforia (3
  slots, tonos por pantalla). D-017 (EN + ES) y D-018 (icons extended) siguen vigentes.
- **Pendiente de Vic**: marca (icono/splash/nombre) y temas adicionales (ver Pendientes).

### D-020 · 2026-09-07 · Blobs como resplandores desenfocados (receta de Esforia), no como gotas 3D · **reespecificada por D-021**
- **Decisión**: cada blob es un degradado radial (núcleo claro → cuerpo → profundo al 55 % →
  transparente) que se funde por completo dentro de su propio radio, sin borde. En API 31+ se
  añade `Modifier.blur(36dp)` a todo el canvas; en API 26–30 el degradado ya es suave. Deriva
  Lissajous, bamboleo de rotación/aplastamiento y **respiración de opacidad** (±18 %) en el
  mismo ciclo. Blobs algo mayores (radio 0,24–0,42 del lado corto) y alfa 0,85 claro / 0,60
  oscuro.
- **Por qué**: Vic: "los blobs deben estar detrás de un blur, puedes fijarte cómo es Esforia en
  su repo". En Esforia cada `.ambient-blob` lleva `filter: blur(46px)` y pulsa entre 0,45 y 0,8
  de opacidad en ciclos de 26–32 s; las tarjetas son alfa plano encima y solo unos pocos
  elementos usan `backdrop-filter`. Las gotas 3D de D-019 tenían bordes nítidos que se veían a
  través de las tarjetas translúcidas, que es justo lo que no gustó.
- **Para qué**: que lo que se ve a través de una tarjeta sea siempre color difuso, de modo que
  la translucidez lea como cristal esmerilado sin pagar un backdrop blur real por tarjeta.
- **Descartado**: backdrop blur real (`RenderEffect`, solo API 31+, una pasada extra por
  tarjeta; Esforia también lo evita salvo en 4 elementos); render a bitmap de baja resolución
  como blur universal (más código para el mismo resultado que el degradado suave).
- **Actualiza**: D-019, apartado "Fondo": el sombreado de cuenta de vidrio en 4 pasadas queda
  **REVERTIDO** por esta decisión; el resto de D-019 sigue vigente. La guía visual de Vic
  muestra gotas 3D, pero su instrucción posterior (blur) prevalece.
- **Limitación conocida**: en las capturas de Robolectric `blur = false`, así que muestran el
  degradado sin el blur extra de API 31+. En móvil real se ve aún más suave.

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

### D-012 · 2026-09-07 · Fondo de blobs en Canvas con trayectorias Lissajous · **actualizada por D-019**
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

### D-015 · 2026-09-07 · Color dinámico (Material You) desactivado · **paleta actualizada por D-019**
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

### D-018 · 2026-09-07 · `material-icons-extended` para los iconos de la barra · **Outlined desde D-019**
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

### S-005 · 2026-09-07 · Reestilizado de la Fase 1 según la guía visual de Vic
- **Petición de Vic**: "no me gusta el estilo visual, te dejo una guía" + imagen con 4 pantallas
  (Home, Mis Cuentas, Balance Total Detallado, Perfil y Ajustes). Guardada en
  `docs/design/guia-visual-vic-2026-09-07.png`.
- **Hecho** (detalle en D-019):
  - `Color.kt`, `Type.kt` (Inter), `Glass.kt` (tokens nuevos: `cornerRadius`, `shadowElevation`,
    `divider`, `navSelected*`, `BlobSpec` con `aspect`/`rotation`/colores de sombreado),
    `Theme.kt` (claro como principal).
  - `BlobBackground` reescrito: `drawBead` en 4 pasadas por gota.
  - `GlassSurface`: radio y sombra desde tokens, brillo más contenido.
  - `BubbleNavBar` reescrito: círculo elevado deslizante, iconos Outlined.
  - Nuevos `ScreenHeader` (+ `HeaderAction`) y `GlassRowGroup`/`GlassRow` (+ `GlassRowItem`).
  - `PlaceholderScreen` rehecho: cabecera, hero centrado con chip, acciones rápidas (Home) y
    grupos de filas. Suficiente contenido en Home para hacer scroll.
  - Tests de captura ahora en claro (tema principal) + una en oscuro.
  - Fuente Inter 4.1 en `res/font` (licencia en `docs/design/INTER-LICENSE.txt`).
- **Problemas y cómo se resolvieron**:
  1. Captura "scrolled" idéntica a la normal (dos veces): la Home cabía en pantalla y la barra,
     correctamente, no se encogía. Se añadieron grupos hasta desbordar.
  2. Gotas champán turbias en oscuro → juego de tonos "Night" más saturados.
  3. Tarjetas oscuras demasiado transparentes → relleno azul marino al 82 % en vez de blanco al 12 %.
  4. Lint `UnusedResources` (`home_subtitle`) → eliminado.
- **Resultado**: `assembleDebug`, `assembleRelease` (1,9 MB), 5 tests y `lintDebug` en verde.
  7 capturas enviadas a Vic. Commit `cb1369d` pusheado a `claude/android-personal-setup-hm95yj`.

### S-006 · 2026-09-07 · Blobs detrás de un blur, al estilo Esforia
- **Petición de Vic**: "los blobs deben estar detrás de un blur, puedes fijarte cómo es Esforia
  en su repo".
- **Hecho**: añadido `esforia-app` a la sesión y leídos `AmbientBackground.tsx` y `app.css`.
  `BlobBackground` reescrito según D-020 (`drawGlow` en vez de `drawBead`), layout y alfas
  de los blobs ajustados en `Glass.kt`. Sin cambios en tarjetas ni barra.
- **Resultado**: `lintDebug`, `assembleRelease` y 5 tests en verde. 7 capturas enviadas a Vic.
  Commit `0982c65` pusheado a `claude/android-personal-setup-hm95yj`. APK entregado a Vic.

### S-014 · 2026-09-08 · Lectura de notificaciones de BBVA
- **Petición de Vic**: "¿al final hiciste lo de leer las notificaciones para registrar los
  gastos?" → no existía. "Uso BBVA, pero ahora mismo no tengo notificaciones".
- **Hecho** (D-031): modelos `CapturedTransaction`/`CaptureStatus`/`Source.CAPTURED`/`inbox`;
  `data/capture/BankNotificationParser` y `BankNotificationListener` (+ manifest);
  repositorio `addCaptured`/`acceptCaptured`/`dismissCaptured`/`pendingCaptures`
  (`addManualTransaction` acepta `source`); `InboxViewModel`, `CaptureSettingsViewModel`;
  pantallas `InboxScreen`, `CaptureSettingsScreen`; fila en Ajustes; tarjeta en Home; rutas
  `inbox` y `settings/capture`; `GroupRow.testTag`. Strings EN/ES.
- **Tests**: `BankNotificationParserTest` (compra, miles+EUR, Bizum recibido, nómina, recibo,
  € delante, texto sin importe, ids estables, filtro de paquetes) y regresión en
  `FinanceRepositoryTest` (deduplicación, aceptar/descartar). Capturas: buzón, buzón
  expandido, ajustes de captura.
- **Problemas**: el comercio "MERCADONA S.A." se cortaba en "Mercadona S" (el punto de la
  abreviatura se tomaba por fin de frase) → un punto solo cierra si le sigue mayúscula, y la
  limpieza cuenta letras sin puntos; "ANA" se trataba como sigla → lista explícita de
  acrónimos societarios (SL, SA, SLU…).
- **Resultado**: 49 tests (1 omitido), lint en verde, release 2,0 MB. APK y capturas enviados.
  Commit pusheado a `claude/android-personal-setup-hm95yj`.

### S-013 · 2026-09-07 · Deslizar entre pestañas + revisión de bugs
- **Petición de Vic**: deslizar con el dedo entre pestañas y "fíjate si hay algún error o bug".
- **Hecho**: `HorizontalPager` para las pestañas (D-030): `AppNavHost` con ruta `tabs` +
  `TabsPager`, `FinanceApp` con `pagerState`, `onTab`. Captura nueva `swipe_to_accounts_light`.
- **Bugs encontrados y corregidos en la revisión**:
  1. **Iconos de la barra de estado invisibles** al forzar tema oscuro con el sistema en claro
     (o al revés): `enableEdgeToEdge()` solo miraba el tema del sistema. Ahora se reaplica con
     `SystemBarStyle` cada vez que cambia el tema de la app.
  2. **Movimientos huérfanos tras sincronizar**: si el banco dejaba de devolver una cuenta, sus
     movimientos antiguos quedaban colgando. `syncConnection` ahora conserva solo los de cuentas
     vivas. Test `sync_drops_transactions_of_accounts_the_bank_no_longer_returns`.
  3. **Borrar una cuenta vinculada la resucitaba en la siguiente sincronización**: la UI ya
     ofrecía "desvincular" para esas cuentas, pero el repositorio lo permitía. Ahora
     `deleteAccount` lo rechaza (`require`). Test `deleting_a_linked_account_is_refused`.
  4. **Reloj mezclado**: "Hoy/Ayer" en Movimientos y la fecha del saludo usaban el reloj del
     sistema mientras todo lo demás usa `repository.clock`. Unificado (visible solo en tests,
     pero rompía la regla de CLAUDE.md).
- **Revisado sin hallazgos**: cálculo de totales y variación mensual, generación determinista
  del sandbox, idempotencia de sync, flujo alta de movimiento sin cuentas → alta de cuenta →
  vuelta con la cuenta seleccionada, gráficas con listas vacías o valores iguales (sin
  división por cero), FileProvider y autoridad, privacidad en todas las cifras, plurales.
- **Problema de test**: con el pager, `onNodeWithTag("screen_list")` tocaba la lista de la
  página vecina fuera de pantalla ("Failed to inject touch input") → gestos sobre `onRoot()`.
- **Resultado**: 37 tests (1 omitido: PDF en Robolectric), lint en verde, release 1,9 MB.
  Commit pusheado a `claude/android-personal-setup-hm95yj`. APK enviado a Vic.

### S-012 · 2026-09-07 · Fase 4: informes, gráfica de evolución, exportación PDF/CSV
- **Petición de Vic**: continuar con las fases restantes.
- **Hecho**: `domain/Reports.kt` (D-029); `ExportManager` + `PdfReport` + `FileProvider`
  (D-028); `LineChart` (área con degradado, máximo/mínimo/último, etiquetas de fecha) y
  `BarChart` con etiquetas de valor y color único; `BalanceViewModel` (patrimonio, activos,
  deudas, variación, histórico 30 días, resumen semana/mes conmutable, serie 6 meses, cuentas,
  exportación con estado y errores); `TotalBalanceScreen` reescrita: hero, Evolución, Resumen
  con `SegmentedToggle` (semana → barras por día; mes → donut por categoría) y media diaria,
  Últimos 6 meses, Por cuenta, Exportar (PDF/CSV) con nota de privacidad. `AppContainer.exporter`.
  Strings EN/ES del informe. `labelResOf()` para usar etiquetas de categoría fuera de Compose.
- **Tests**: `ReportsTest` (semana lunes–domingo con días vacíos, mes, serie),
  `ExportManagerTest` (CSV RFC 4180 con comillas y comas, archivos en caché, libro vacío; PDF
  omitido en Robolectric). Capturas de Balance en claro (arriba y desplazado) y oscuro.
- **Problemas**: `PdfDocument` no funciona en Robolectric (nativo) → `Assume` en el test.
- **Resultado**: 34 tests (1 omitido) y lint en verde. Release 1,9 MB. Commit pusheado a
  `claude/android-personal-setup-hm95yj`. APK y capturas enviados a Vic.

### S-011 · 2026-09-07 · Fase 3: preferencias, Ajustes, Perfil, detalle de cuenta, movimientos
- **Petición de Vic**: "quedó hermoso, continúa con las demás fases".
- **Hecho**: `Store<T>`/`JsonFileStore<T>` genérico; `UserPreferences` + `PreferencesRepository`
  (D-027); `AppContainer.preferences`; tema por preferencia en `MainActivity`;
  `LocalMoneyDisplay` + `money()`. `FinanceRepository.wipeAll()`/`account()`;
  `FinanceCalculator.balanceHistory()`/`groupByDay()`. ViewModels: Settings, Profile,
  Transactions, AccountDetail; Home usa el nombre de perfil y tiene ojo de privacidad, tap en
  tarjeta → detalle, "Ver todo" → movimientos. Pantallas: `SettingsScreen` (moneda → picker,
  notificaciones, privacidad, tema, atajos a bancos e informes, borrar todo con confirmación,
  versión), `CurrencyScreen`, `ProfileScreen` (avatar con inicial, editar nombre, chips de
  cuentas/movimientos, bancos vinculados con desvincular, privacidad, borrar todo),
  `TransactionsScreen` (agrupado por día, Hoy/Ayer, tap → detalle/borrar),
  `AccountDetailScreen` (hero con ingresos/gastos del mes, línea de 30 días, últimos
  movimientos, borrar/desvincular). `ConfirmDialog` en paleta. Rutas con argumentos
  (`account/{id}`, `transactions?accountId=`) que ocultan la barra. `BuildConfig` activado
  para mostrar la versión. Plurales `n_accounts`/`n_transactions`.
- **Tests**: `JsonFileStoreTest` (recarga, archivo corrupto, claves desconocidas). Capturas:
  ajustes y perfil en oscuro, detalle de cuenta, movimientos, Home en modo privacidad.
- **Resultado**: 27 tests y lint en verde. Commit pusheado a `claude/android-personal-setup-hm95yj`.

### S-010 · 2026-09-07 · Fase 2: datos, banco sandbox, entrada manual
- **Petición de Vic**: arrancar la Fase 2 sin Plaid/Tink ("no tengo ni idea de qué es Plaid o
  Tink así que no").
- **Hecho**:
  - `data/model` (`Account`, `Transaction`, `Category`, `BankConnection`, `FinanceData`, `Money`),
    `data/store` (D-025), `data/bank` (`BankProvider`, `MockBankProvider`,
    `OpenBankingProviderTemplate`), `data/repository/FinanceRepository`, `domain/FinanceCalculator`
    (totales, mes, gasto por categoría, variación mensual).
  - `AppContainer` + `PersonalApplication` (manifest) + `LocalAppContainer`; ViewModels
    (`HomeViewModel`, `AccountsViewModel`, `AddTransactionViewModel`, `AddAccountViewModel`,
    `LinkBankViewModel`).
  - UI: `Forms.kt` (`EsforiaTextField`, `AmountField`, `OptionPill`, `SegmentedToggle`,
    `PrimaryButton`, `FormHeader`, `FieldLabel`), `CategoryUi.kt` (icono y etiqueta por
    categoría y tipo de cuenta), pantallas `AddTransactionScreen`, `AddAccountScreen`,
    `LinkBankScreen`; rutas `Routes.*` con transición deslizante y **barra oculta** en formularios.
    Home y Cuentas con datos reales y estados vacíos; "+" en Home abre el alta de movimiento;
    Cuentas tiene sincronizar y añadir.
  - Dependencias: kotlinx-serialization (plugin + json 1.10.0), coroutines 1.10.2,
    lifecycle-viewmodel-compose y runtime-compose 2.9.2.
  - Tests: `MockBankProviderTest` (determinismo, filtro since, saldos), `FinanceRepositoryTest`
    (alta manual, borrado, vinculación + resync idempotente, desvincular),
    `FinanceCalculatorTest` (totales, cuotas, dinero). Capturas con contenedor sembrado y reloj
    fijo; nuevas: Home vacía, alta de movimiento, alta de cuenta, vincular banco.
- **Problemas**: sin KSP para Kotlin 2.4 (→ D-025); `NumberFormat` usa espacio duro (test
  normaliza); la Home usaba `YearMonth.now()` real en vez del reloj del repositorio (→ los
  ViewModels usan `repo.clock`); plural `accounts_imported` necesita `many` en español.
- **Resultado**: 20 tests y lint en verde; release 1,9 MB. Capturas y APK enviados a Vic.
  Commit pusheado a `claude/android-personal-setup-hm95yj`.
- **Nota de alcance**: Home y Cuentas ya muestran datos reales (adelanto parcial de la Fase 3)
  porque sin ello la Fase 2 no se podía verificar en pantalla. Balance, Ajustes y Perfil
  siguen como esqueleto.

### S-009 · 2026-09-07 · Decisión final de Fase 1: Esforia + estructura de Home de NavyGold
- **Petición de Vic**: "usaremos el estilo visual de esforia, pero heredamos la estructura del
  home de navygold, solo la estructura, el aspecto visual es de Esforia, todo lo demás se queda
  como está y puedes eliminar navygold".
- **Hecho**: D-024 aplicada. `HomeScreen.kt` reescrito; `Charts.kt` nuevo; NavyGold borrado
  (código, fuente Inter, dos imágenes); `Palette`/`Type`/`Theme`/`AppNavHost`/`FinanceApp`
  vuelven a una sola identidad; tests de captura vuelven a 5; strings huérfanos eliminados.
- **Resultado**: lint (0 errores), release (1,3 MB) y 5 tests en verde. 7 capturas y APK
  enviados a Vic. Commit pusheado a `claude/android-personal-setup-hm95yj`.
- **Siguiente**: Fase 2 (modelos de datos, Open Banking sandbox, entrada manual).

### S-008 · 2026-09-07 · Segunda piel NavyGold, conmutable, sin borrar Esforia
- **Petición de Vic**: "antes de continuar a la fase 2 quiero descartar otro aspecto visual,
  todo esto no lo borres guárdatelo y ahora te paso otro diseño completamente distinto para ver
  cómo queda, tienes tanto el modo claro como el oscuro" + 3 imágenes.
- **Hecho**: `Skin.kt`, extras en `Palette.kt` con `NavyGoldLightPalette`/`NavyGoldDarkPalette`,
  `NavyGoldTypography` + `moneyStyle()` en `Type.kt`, `Theme.kt` por piel. Componentes en
  `ui/components/navygold/` (`BandHeader`, `NavyHero`, `GradientCard`, `AccountCard`,
  `MiniAccountCard`, `Sparkline`, `NavyButton`, `SmallOutlineButton`, `SectionTitle`,
  `TransactionRow`, `DonutChart`, `BarChart`, `DockedNavBar`). Pantallas en
  `ui/screens/navygold/` (`NavyScaffold`, `NavyScreens.kt`). `AppNavHost` y `FinanceApp`
  ramifican por piel. Inter variable añadida a `res/font`. Tests de captura para ambas pieles
  (15 PNG). Imágenes del diseño en `docs/design/navy-gold-*.png`.
- **Problemas**: la banda con esquinas inferiores redondeadas asomaba como "hombros" a los
  lados del hero → banda recta como en el diseño. Sombra del hero en claro demasiado gris →
  alfa 0,09.
- **Resultado**: lint, release (1,8 MB) y 9 tests en verde. Capturas y APK enviados a Vic.
  Commit pusheado a `claude/android-personal-setup-hm95yj`.
- **Nota**: la Home NavyGold cabe en pantalla, así que su captura "scrolled" no ejercita el
  encogido de la barra; el mecanismo es el mismo `NavBarScrollState` y funciona en Esforia.

### S-007 · 2026-09-07 · Identidad visual de Esforia
- **Petición de Vic**: tras probar el APK, "de funcionar funciona, pero no me gusta el diseño
  visual". A la pregunta de qué falla: fondo y tipografía/colores/estructura; sobre cómo
  deberían verse: "Identidad visual de Esforia, analiza el proyecto más a fondo sin tocar nada
  de él".
- **Hecho**:
  1. Análisis de `esforia-app` (solo lectura): paletas, temas, CSS de shell y fondo, primitivas
     de UI, páginas Home/Finance/Settings, icono. Resultado en
     `docs/design/identidad-esforia.md` (5 apartados con valores exactos).
  2. Fuentes Sora, Manrope e IBM Plex Mono descargadas de `google/fonts` (OFL, licencias en
     `docs/design/OFL-*.txt`); Inter eliminada.
  3. Reescritura de la capa visual según D-021: `Palette.kt` (sustituye a `Color.kt` y
     `Glass.kt`), `Type.kt`, `Theme.kt`, `AmbientBackground.kt` (sustituye a
     `BlobBackground.kt`), `Surfaces.kt`, `PageHeader.kt` (sustituye a `ScreenHeader.kt`),
     `GlassSurface.kt` (solo barra; `softShadow` reemplaza a `glassShadow`), `BubbleNavBar.kt`,
     `Destination.kt` (icono activo Filled + tono ambiental), `FinanceApp.kt`, `ScreenScaffold`
     y una pantalla por archivo. `GlassRows.kt` y `PlaceholderScreen.kt` eliminados.
  4. Strings EN/ES reescritas (≈70 claves).
- **Problemas y cómo se resolvieron**:
  1. `FontVariation` es API experimental en Compose 1.9 → `@OptIn(ExperimentalTextApi::class)`.
- **Resultado**: `assembleDebug`, `assembleRelease` (1,3 MB), 5 tests y `lintDebug` en verde.
  7 capturas enviadas a Vic. Commit pusheado a `claude/android-personal-setup-hm95yj`.
