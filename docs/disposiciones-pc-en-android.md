# Las dos disposiciones de Hexpad PC, llevadas a Hexpad Android

Adaptación de las dos pantallas de inicio del launcher de escritorio —Rejilla y
Consola— a la app de móvil, como **dos disposiciones nuevas** que se suman a las
dos que la app ya tenía.

Última actualización: 4 de septiembre de 2026.

## 1. Qué se ha hecho, en una frase

Se han portado `ui/home/CarouselScreen.kt` y `ui/console/ConsoleScreen.kt` del
proyecto de escritorio a `presentation/launcher/` del proyecto Android,
respetando su estructura y su comportamiento, y reescalando sus medidas al alto
de un móvil en horizontal.

**No se ha tocado ninguna de las dos disposiciones que la app ya tenía.**
`PAGE` y `CONSOLE` siguen exactamente igual; las nuevas son dos ramas más.

## 2. El nombre, que se presta a confusión

El escritorio llama a sus dos inicios **Rejilla** y **Consola**. El móvil ya
tenía una disposición llamada `CONSOLE`, que es distinta de la del escritorio.
Para que no haya dos "Consola" en la misma pantalla de ajustes:

| Escritorio | Enum en Android | Ajustes (ES) | Ajustes (EN) |
|---|---|---|---|
| Rejilla | `HomeLayout.CAROUSEL` | Rejilla | Carousel |
| Consola | `HomeLayout.STRIP` | Franja | Strip |

Las dos llevan en su texto de ayuda "Viene de Hexpad para Windows", que es lo
que de verdad las distingue de las otras dos.

## 3. Ficheros

### Nuevos

| Fichero | Qué es |
|---|---|
| `presentation/launcher/CarouselLayout.kt` | La Rejilla del escritorio |
| `presentation/launcher/StripLayout.kt` | La Consola del escritorio |
| `presentation/launcher/SelectionDrag.kt` | Modificador compartido: arrastrar de lado cambia de juego |
| `res/values/strings_pc_layouts.xml` | Textos nuevos, en inglés |
| `res/values-es/strings_pc_layouts.xml` | Los mismos, en castellano |

Los textos van en un fichero de recursos aparte a propósito: Android fusiona
todos los XML de una carpeta de valores, así que la función se añade sin tocar
un `strings.xml` que ya está traducido y revisado, y se retira borrando dos
ficheros.

### Modificados

| Fichero | Cambio |
|---|---|
| `domain/model/HomeLayout.kt` | Dos valores más en el enum, con su documentación |
| `presentation/launcher/LauncherScreen.kt` | Dos ramas más en el `when` que reparte |
| `presentation/settings/SettingsContent.kt` | Dos tarjetas más en la sección de disposición |
| `app/build.gradle.kts` | `versionCode` 21 → 22 y `versionName` 0.21 → 0.22 |

La subida de versión no es un extra: es la regla del propio proyecto (§3.bis del
handoff). Dos APK con la misma versión son indistinguibles una vez instalados, y
eso ya costó una vuelta entera.

Los tres se entregan **completos**, no como parche: se copian encima de los del
proyecto. Están tomados de la copia del proyecto que hay en Drive y verificados
byte a byte contra ella antes de modificarlos.

Nada de esto cambia la persistencia. La disposición ya se guardaba **por nombre
y no por posición**, así que ampliar el enum no le altera la elección a nadie que
tenga la app instalada.

## 4. El problema real de la adaptación: el alto

El escritorio asume una ventana de 1100×660 como mínimo. Un móvil en horizontal
da unos **411 dp de alto**, y **309 dp** si el usuario ha subido el tamaño de
pantalla en los ajustes del sistema. Las medidas del escritorio no caben:
sus cartas piden entre 300 y 470 dp de alto, que es más que la pantalla entera.

Se ha seguido la regla que el propio proyecto Android ya se aplica (§3.bis y
§3.quater de su handoff): **repartir el alto explícitamente y decidir qué cede**,
porque en una columna que desborda el que se queda sin sitio es el último hijo, y
el último suele ser justo el botón de jugar.

En las dos disposiciones nuevas:

- **Ceden las portadas.** En el carrusel, entre 132 y 300 dp de alto; en la
  franja, iconos entre 64 y 132 dp de lado.
- **No ceden nunca los botones.** Sin "Jugar" la pantalla no sirve para nada.
- **Se retira lo prescindible antes de aplastar nada.** Por debajo de 340 dp
  útiles desaparecen los datos de la partida del carrusel; por debajo de 320,
  el panel de "Recientes" de la franja. En los dos casos lo retirado está
  también en el perfil.

Cuentas concretas: a 411 dp de alto salen cartas de 261 dp y iconos de 132 dp;
a 309 dp, cartas de 159 dp e iconos de 91 dp. Se sigue leyendo qué juego es.

## 5. Lo que cambia respecto al escritorio, y por qué

Estas no son licencias: son sitios donde copiar literalmente habría dado una
pantalla peor o directamente rota.

**La navegación.** En el escritorio vive en una barra lateral que en el móvil no
existe. En el carrusel, los tres mandos —biblioteca, perfil y ajustes— van en la
cabecera, entre la marca y el reloj, que es la única banda ya ocupada y no cuesta
alto. En la franja van donde el escritorio los pone: como iconos más al final de
la propia franja, detrás de los juegos.

**El halo de la carta elegida.** El escritorio lo dibuja con `Modifier.blur`.
En Android eso **solo pinta desde API 31**, y el mínimo de la app es API 26: en
un móvil con Android 8 a 11 la carta elegida se habría quedado sin halo. Se usa
en su lugar el halo de anillos concéntricos que ya tiene el launcher
(`GlassPanel(glowing = true)`), que se ve igual en todas las versiones. El
proyecto Android ya tenía escrito por qué no usa sombras del sistema para esto.

**La rueda del ratón es un arrastre.** Las dos filas del escritorio tienen el
desplazamiento desactivado —el centro lo decide la selección, no el dedo— y la
rueda mueve la selección de una en una con un respiro entre pasos. Se ha
mantenido el desplazamiento desactivado y el equivalente táctil es
`SelectionDrag.kt`: arrastrar de lado cambia de juego, 64 dp por juego.

**Las estanterías de la franja son dos y no seis.** El escritorio reparte en
Todos, Recientes, Favoritos y una por género. En el móvil no hay favoritos, y los
géneros salían de la ficha de internet, que esta app no pide a propósito. Quedan
Todos y Recientes, y Recientes solo aparece cuando hay acceso al uso y algo que
enseñar: una pestaña permanentemente vacía es peor que no tenerla.

**Los datos de la partida son otros.** El escritorio enseña tienda de
procedencia y veredicto frente a los requisitos del juego. En el móvil no hay
tiendas —el juego es una app instalada— ni requisitos que comparar. En su lugar:
última vez, tiempo jugado y si sigue instalado. Cuando falta el acceso al uso se
dice "Sin acceso al uso" y **no un cero**, que se leería como "no has jugado".

**"Mi PC" es la barra de rendimiento.** El escritorio tiene una sección propia
con nota del equipo; el móvil tiene la barra que ya existe, y va arriba. No se ha
hecho un panel de cifras nuevo: sería repetirla con otros números.

**"Mantenimiento" no se porta.** No existe en el móvil, y no es esta la tarea
para inventarlo.

## 6. Qué está verificado y qué no

**Verificado:**

- Los ficheros modificados coinciden byte a byte con los del Drive antes de
  aplicarles el cambio.
- **Compila de verdad.** Se reconstruyó el proyecto entero en este contenedor
  —SDK de Android 37.0, build-tools 37.0.0, AGP 9.3.2, Gradle 9.5.0, JDK 21— y
  `:app:assembleDebug` termina en BUILD SUCCESSFUL. El APK resultante pesa 20 MB,
  declara `com.esforal.gamelauncher` 0.22 (versionCode 22), minSdk 26 y
  targetSdk 37, y su firma verifica con el esquema v2.
- **Las piezas nuevas están dentro del APK**: `CarouselLayoutKt`, `StripLayoutKt`
  y `SelectionDragKt` aparecen en los dex, y las cadenas nuevas —incluidas
  «Rejilla», «Franja» y «Viene de Hexpad para Windows»— están en la tabla de
  recursos en los dos idiomas.
- **Lógica**: 15 comprobaciones sobre las reglas escritas —filtro de recientes,
  reparto por estantería y paso por arrastre— compiladas y ejecutadas en verde.

**Tres fallos reales que salieron al verificar, y están corregidos:**

1. El gesto de arrastre pasaba la selección como clave de `pointerInput`, así que
   al cambiar de juego el bloque se reiniciaba y **cancelaba el gesto en curso**.
   Se sacó a `SelectionDrag.kt` con clave fija y el índice en una variable local
   del gesto.
2. Las cadenas `duration_hours_minutes` y `duration_minutes` **ya existían** en
   el proyecto y se duplicaron; el empaquetado de recursos falló. Ahora se usan
   `formatPlayTime` y `formatLastPlayed`, que el launcher ya tenía en
   `presentation/common/Formatters.kt`.
3. Dentro de la `Column`, el receptor de `BoxWithConstraints` queda tapado y
   `maxHeight` no se ve. El alto libre se captura ahora antes de entrar.

**No verificado, y hay que hacerlo antes de dar esto por bueno:**

- **No se ha visto en pantalla.** Todo el reparto de alto está calculado, no
  mirado. Hay que probarlo a 411 dp y, sobre todo, **a 309 dp**
  (`adb shell wm density 560` sobre 2400×1080), que es donde el proyecto ya se
  rompió una vez.
- **El gesto de arrastre no se ha probado con un dedo.** El paso de 64 dp es una
  estimación razonable, no una medida.
- **Mando**: las fichas son `clickable`, así que la cruceta mueve el foco y A
  activa, y los gatillos L1/R1 siguen rotando por la lista desde la raíz. No se
  ha comprobado con un mando de verdad, igual que el resto de la app.
- **El APK va firmado con la clave de debug**, porque el keystore no está en
  Drive a propósito. No se actualiza sobre una instalación firmada con la clave
  real: hay que desinstalar primero, y eso borra los juegos y los logros (§6.quinquies
  del handoff).

## 7. Cómo llevarlo al proyecto

El proyecto real vive en Drive, en `Hexpad/Proyecto Android`. Copiando desde la
raíz de este repositorio:

```
hexpad-android/app/src/main/java/com/esforal/gamelauncher/...  ->  app/src/main/java/com/esforal/gamelauncher/...
hexpad-android/app/src/main/res/...                            ->  app/src/main/res/...
```

Los cinco ficheros nuevos se añaden y los tres modificados se sobrescriben.
Después, `./gradlew assembleDebug` y a mirar la pantalla.

Si algo no encaja, el primer sospechoso es la firma de algún componente
compartido: este código se escribió leyendo `Controls.kt`, `Surfaces.kt`,
`Focus.kt`, `GameTabs.kt`, `ConsoleLayout.kt` y `LauncherScreen.kt` de la copia
del Drive, no compilando contra ellos.
