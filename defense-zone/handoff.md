# Defense Zone — Handoff / Decisiones de diseño

Documento de traspaso: qué se decidió, **por qué** y **para qué**, para poder
revisarlo si algo no cuadra o hay que cambiarlo. Cuando cambies algo, actualiza
la sección correspondiente.

---

## 1. Tecnología: HTML5 Canvas dentro de un WebView Android

**Decisión.** El juego está escrito en JavaScript (ES modules) + Canvas 2D y se
empaqueta en una app Android nativa (Kotlin) que sólo contiene un `WebView` a
pantalla completa. La carpeta `game/` es el juego; `android/` es el envoltorio.

**Por qué.**
- Permite ejecutar y probar el juego en cualquier navegador (y con Chromium sin
  cabeza en este entorno) sin necesitar un dispositivo/emulador para cada cambio.
- La lógica de juego es JavaScript puro sin DOM (`game/js/sim/`), así que se
  puede simular en Node (`tools/simulate.mjs`) para balancear cientos de
  partidas en segundos. Esto fue clave para ajustar los números (ver §7).
- Una sola base de código. Sin motores externos que descargar (libGDX, Godot,
  Unity) ni assets binarios: los gráficos son vectoriales dibujados en canvas y
  el audio se sintetiza con Web Audio. El APK de depuración pesa ~2,2 MB.

**Alternativas descartadas.**
- *Kotlin nativo con SurfaceView*: no se puede probar aquí (sin emulador) y
  requeriría reescribir la simulación para poder balancearla.
- *libGDX / Godot*: más potencia, pero más peso y dependencia de herramientas
  externas para un juego 2D sencillo.

**Coste asumido.** Un WebView rinde algo menos que código nativo. Se mitiga con:
fondo pre-renderizado una sola vez, ajuste de calidad (partículas, DPR, sombras),
y bucle con paso fijo. Requiere WebView ≥ 99 para `roundRect` (hay polyfill en
`main.js`), en la práctica cualquier Android 8+ actualizado.

## 2. Estructura del proyecto

```
defense-zone/
├── game/                 Juego HTML5 (se empaqueta tal cual como assets del APK)
│   ├── index.html        Pantallas (menú, ajustes, HUD, pausa, fin)
│   ├── css/style.css
│   └── js/
│       ├── config.js     TODOS los números de balance y ajustes por defecto
│       ├── main.js       Controlador: estados, bucle, entrada, eventos → audio/FX
│       ├── sim/          Lógica pura (sin navegador): world.js, ai.js, rng.js
│       ├── render/       renderer.js (entidades), background.js (mapa), effects.js
│       ├── audio/        audio.js (música y efectos sintetizados)
│       ├── ui/           hud.js (cartas/maná/reloj), screens.js (menús)
│       ├── storage.js    Ajustes en localStorage
│       └── platform.js   Puente con Android (salir, vibración)
├── tools/simulate.mjs    Simulación IA vs IA sin pantalla para balance
├── android/              Proyecto Android Studio (Kotlin, AGP 8.7.3, Gradle 8.10.2)
├── docs/                 Capturas de referencia
├── README.md             Cómo ejecutar / compilar
└── handoff.md            Este documento
```

`android/app/build.gradle.kts` apunta `assets.srcDirs("../../game")`, así que
**no hay copia** del juego dentro de `android/`; se empaqueta directamente.

## 3. Reglas de juego (decisiones)

| Tema | Decisión | Por qué |
|---|---|---|
| Mapa | Un solo carril horizontal de 1600×720 unidades; pista transitable entre y=200 y y=620. La cámara muestra todo el mapa (no hay scroll). | El jugador necesita ver dónde está el héroe enemigo para reaccionar; un mapa con scroll complica colocar cartas en móvil. |
| Cámara | Llena el ancho de pantalla; si no cabe el alto, recorta decoración superior/inferior (la pista siempre es visible). | Evita barras negras laterales en móviles 20:9. |
| Héroes | Radio 50 (≈2× cualquier estructura), 12 000 PV, barra de vida grande con texto. El del jugador sale por la izquierda (azul) y va a la derecha; la IA al revés (rojo). Caminan por "vías" distintas (±55 del centro) para cruzarse sin chocar. | Requisito: notablemente más grandes y con barra grande. Las vías evitan un bloqueo mutuo en el centro. |
| Combate del héroe | (1) Las estructuras enemigas son **sólidas**: si una toca su frente, se detiene y la golpea cuerpo a cuerpo. Los **muros sólo se atacan por contacto**. (2) Mientras camina, dispara a la torre enemiga más cercana en alcance (190) o al héroe rival. No se detiene para disparar. | Primera versión: el héroe se paraba ante cualquier estructura a distancia → acampar la base enemiga con torres era imbatible y las partidas duraban 1,3 min. Con esta regla los **muros** son la herramienta de bloqueo y las torres hacen daño mientras el héroe está frenado (sinergia muro+torre, como pedía la descripción). |
| Daño del héroe | 14 por disparo cada 0,4 s contra el héroe rival (35 DPS); ×5 contra estructuras (175 DPS). Regenera 10 PV/s. | Los héroes se cruzan en el centro y con 60 DPS se mataban entre sí antes de que las cartas importaran. Contra estructuras debe ser rápido o el maná (una carta cada ~10 s) bloquea al héroe para siempre. |
| Estructuras propias | El héroe atraviesa las estructuras de su propio bando. | Que un muro propio frene a tu héroe sería frustrante. |
| Zona de despliegue | Cualquier punto de la pista salvo los 300 últimos px pegados a la base enemiga. Las estructuras no pueden solaparse entre sí ni con un héroe. | Antes (200 px) la IA/jugador podían acampar la salida del héroe rival. |
| Tronco | Rueda hacia la base enemiga, hace 60 de daño y **empuja al héroe rival un máximo de 130 px**; después le pasa por encima. Daña estructuras enemigas que atraviesa (150). | La versión inicial arrastraba al héroe durante todo el recorrido (600 px): por 2 de maná reseteaba todo el avance. |
| Alambre de espino | Zona de radio 90 durante 20 s: ralentiza 45 % y hace 6 DPS. | Carta barata de "control" para ganar tiempo a las torres. |
| Cañón | 60 de daño cada 2,4 s con retroceso de 12 px. | El retroceso original (40) impedía físicamente que el héroe llegara a golpearlo (bloqueo infinito); 12 sólo le hace perder algo de tiempo. |
| Fin por tiempo (15:00) | Gana quien haya avanzado más (diferencia >3 %); si empate, quien tenga más % de vida (>2 %); si no, empate. | El usuario no lo especificó; es el desempate más justo con el objetivo de "llegar al otro lado". |
| IA | Usa exactamente las mismas cartas, mano rotativa y maná que el jugador. Piensa cada ~1,2 s: coloca muros delante del héroe rival, torres detrás de los muros, tronco cuando el rival pasa del 55 % del mapa, y ahorra para cañón/arquero cuando ya tiene un muro. Tiene aleatoriedad para no ser predecible. | Reglas simétricas hacen el balance verificable por simulación. Existe un parámetro `difficulty` en `EnemyAI` (no expuesto en ajustes porque no se pidió). |

## 4. Maná y cartas

- Máximo 10, se empieza con 5, recarga **1 maná cada 3 s** ("medianamente
  lenta"; Clash Royale usa 2,8 s). A los 9:00 la recarga se duplica junto con
  todo lo demás.
- Mano de 4 cartas + "siguiente", rotación estilo Clash Royale (la carta jugada
  va al final del mazo de 6).
- Costes de 2 a 6 según lo pedido:

| Carta | Coste | Tipo | Vida | Daño / alcance | Notas |
|---|---|---|---|---|---|
| Tronco | 2 | proyectil | – | 60, empuja 130 px | recorre 420 px |
| Alambre de espino | 3 | zona 20 s | – | 6 DPS, −45 % velocidad | radio 90 |
| Muro | 3 | estructura | 1800 | – | bloquea |
| Torreta | 4 | estructura | 750 | 8 cada 0,5 s (16 DPS), alcance 240 | |
| Torre de arquero | 5 | estructura | 800 | 24 cada 1,2 s (20 DPS), alcance 300 | |
| Cañón | 6 | estructura | 900 | 60 cada 2,4 s (25 DPS) + retroceso, alcance 290 | |

El usuario pidió 4 cartas (torreta, tronco, muro, arquero); se añadieron
**alambre** y **cañón** para cubrir el rango 2–6 de maná con más variedad
táctica. Se pueden quitar del mazo eliminándolas de `DECK_ORDER` en `config.js`.

## 5. Fases de la partida

| Minuto | Evento | Implementación |
|---|---|---|
| 0:00 | Cuenta atrás 3-2-1 y "¡A luchar!" | La simulación no avanza durante la cuenta atrás. |
| 6:00 → 8:00 | Lluvia | Sólo visual/sonora: gotas, oscurecimiento, charcos, relámpagos con trueno, ruido de lluvia. **No afecta al juego** (no se pidió). |
| 9:00 | "¡Velocidad x2!" | `world.speedMult = 2`: velocidad de movimiento, cadencia de disparo (héroes y torres), velocidad de proyectiles y troncos, y recarga de maná. Viñeta roja pulsante y música más rápida. |
| 15:00 | Fin por tiempo | Ver desempate en §3. |

Los tiempos están en `MATCH` (config.js). La barra bajo el reloj tiene marcas
en 6:00 y 9:00.

## 6. Entrada, HUD y pantallas

- **Colocación**: tocar una carta (se resalta) y tocar el mapa, **o** arrastrar
  la carta hasta el mapa. En ambos casos se ve una previsualización verde/roja
  con el alcance. Si no se puede colocar, aparece el motivo (maná, zona, solape).
- **Menú principal** con fondo animado (focos, humo, chispas): Iniciar partida,
  Ajustes, Salir. "Salir" llama a `Android.exitApp()`; en navegador avisa.
- **Ajustes** (accesibles desde el menú y desde la pausa): calidad gráfica
  baja/media/alta (partículas, gotas de lluvia, sombras, tope de DPR, densidad
  de decoración), volumen de música y de efectos (con prueba de sonido al
  soltar). Se guardan en `localStorage`.
- **Pausa** con botón en el HUD, botón "atrás" de Android, tecla Esc, y
  automática al perder el foco/segundo plano. Continuar / Ajustes / Salir al menú.
- **Victoria / derrota / empate**: animación CSS (título que rebota en dorado,
  o cae con desenfoque en rojo), sacudida y explosión del héroe caído, fuegos
  artificiales en victoria, sonido correspondiente. Botones Jugar de nuevo /
  Menú principal.

## 7. Proceso de balance (resumen de lo que se probó)

Herramienta: `node tools/simulate.mjs [partidas] [semilla]` (IA contra IA,
determinista por semilla). Objetivo: partidas de 5–12 min con mezcla de finales
por vida y por llegada, para que lluvia (6:00) y x2 (9:00) importen.

| Iteración | Resultado | Problema / cambio |
|---|---|---|
| 1 | 1,3 min, todo por vida, avance ~0 | Torres demasiado letales y héroe detenido ante cualquier estructura a distancia. |
| 2–3 | 3–5 min, todo por vida | Cañón con retroceso 40 → bloqueo infinito (8 800 de daño en una partida). Tronco arrastraba 600 px. |
| 4 | 4,4 min, 11 de 16 por llegada | Nuevas reglas de combate (§3). Ahora las torres apenas hacían daño. |
| 5–6 | 5–7 min, 50/50 | Vida de muros/torres arriba, alcances abajo, maná a 3 s. |
| 7 | 5,7 min, 27/3 por vida | Al agrandar el héroe (r=50) los héroes se duelaban en el centro (6 000–8 000 de daño entre ellos). |
| **Final** | **8,4 min (4,9–15), 13 vida / 16 llegada / 1 tiempo** | Daño héroe-héroe 60→35 DPS, ×5 contra estructuras, cañón 70→60. |

Nota: la simulación es IA vs IA; un humano juega distinto. Si las partidas
reales resultan largas/cortas, los mandos principales son `HERO.speed`,
`HERO.regenPerSecond`, `MANA.regenPerSecond` y la vida de `wall`.

## 8. Android

- `MainActivity` (ComponentActivity) con `WebView`: JavaScript, DOM storage,
  audio sin gesto adicional, orientación `sensorLandscape`, pantalla completa
  inmersiva, pantalla siempre encendida, `windowLayoutInDisplayCutoutMode=shortEdges`.
- Se sirve con `WebViewAssetLoader` en `https://appassets.androidapp.com/assets/`
  (no `file://`) porque los módulos ES y `localStorage` necesitan un origen https.
- Botón atrás → evento JS `androidback` (pausa / cerrar ajustes / volver / salir).
  `onPause` → evento `androidpause` (pausa la partida).
- `AndroidBridge.exitApp()` expuesto como `window.Android`.
- minSdk 26 (permite icono adaptativo vectorial sin PNGs), targetSdk 35.
- Comprobado: `./gradlew assembleDebug` compila en este entorno (SDK 35,
  build-tools 35). **No se ha probado en un dispositivo ni emulador real**:
  el comportamiento del WebView (rendimiento, audio, barras del sistema) debe
  validarse en un móvil. Firma de release no configurada.

## 9. Ideas / pendientes (no pedidos, apuntados para el futuro)

- Selector de dificultad en ajustes (`EnemyAI.difficulty` ya existe).
- Que la lluvia afecte al juego (p. ej. ralentizar a todos un 10 %).
- Estadísticas / mejor tiempo persistentes.
- Sustituir emojis de las cartas por iconos propios (los emojis dependen de la
  fuente del sistema; en Android salen los de Google).
- Avisos "El enemigo juega X" pueden resultar repetitivos: quitar en `main.js`
  (`case 'place'`).
