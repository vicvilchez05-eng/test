# Juegos hechos desde el celular

Tres juegos en HTML5, cada uno en un solo archivo. Se abren en cualquier navegador,
en el celular o en la PC, sin instalar nada.

## ESFIGHTERS · `esfighters/index.html`

Juego de peleas 2D en pixel art.

- **4 luchadores**: John Camacho (boxeador), Víctor Vilchez (taekwondo), Marta Sol (capoeira) y Bruno Castro (peso pesado).
  Cada uno tiene velocidad, fuerza y alcance distintos.
- **4 ataques por personaje**: 2 puños y 2 patadas, con nombre propio. Si atacas agachado das golpes bajos.
- **Bloqueo, salto y agacharse.** El bloqueo de pie no para los barridos, el bloqueo agachado no para los golpes en salto
  y agacharse esquiva la patada alta.
- **2 escenarios**: Azotea nocturna y Muelle al atardecer.
- **Menú de selección de personaje**, selección de escenario y pantalla VS con la lista de ataques.
- **1 jugador contra la CPU** o **2 jugadores** en el mismo teclado. Gana quien se lleve 2 rondas de 60 segundos.

Controles en el celular: cruceta y botones en pantalla.
Teclado J1: `A` `D` mover, `W` saltar, `S` agacharse, `J` `K` puños, `U` `I` patadas, `L` bloqueo, `Intro` start.
Teclado J2: flechas, teclado numérico `1` `2` puños, `4` `5` patadas, `0` bloqueo.

Los sprites, los escenarios y los sonidos se generan por código en un canvas de 320 × 180 escalado sin suavizado.

### APK para Android (uso personal)

La carpeta `android/` contiene dos apps mínimas que muestran cada juego a pantalla completa, sin internet:
`app` (ESFIGHTERS, 2D) y `app3d` (ESFIGHTERS 3D). Cada vez que cambia un juego o la app, GitHub Actions
compila los APK y los deja en la sección **Releases** del repositorio, etiqueta `apk-latest`,
como `ESFIGHTERS.apk` y `ESFIGHTERS-3D.apk`.

Para instalarlo: descarga el archivo en el celular, ábrelo y acepta "instalar apps de origen desconocido"
la primera vez. Va firmado con una clave de depuración, suficiente para uso personal; no sirve para Google Play.

Para compilarlo en una PC con Android Studio: abre la carpeta `android/` y ejecuta `gradle assembleDebug`.

## ESFIGHTERS 3D · `esfighters3d/index.html`

Juego de peleas en 3D con Three.js. John Camacho, un soldado con pistola, contra Víctor Vilchez, un ninja con katana,
en una calle de ciudad de noche con lluvia ligera, letreros de neón y charcos.

- **2 personajes** con modelos 3D animados por código: el soldado tiene un disparo a distancia que se esquiva agachándose;
  el ninja tiene más alcance con la katana y salta más alto.
- **4 ataques por personaje** (2 de puño y 2 de patada), golpes agachados, bloqueo, salto y agacharse.
- **Un escenario**: ciudad bajo la lluvia, con cámara que sigue la pelea, cámara lenta en el KO y golpes con impacto.
- **Menús**: portada con los dos luchadores, selección de personaje, pantalla VS con la lista de ataques y resultado.
- **1 jugador contra la CPU** o **2 jugadores** en el mismo teclado. Mismos controles que ESFIGHTERS.

La librería Three.js se incluye en la carpeta (`three.min.js`, licencia MIT) para que funcione sin internet.

## Serpiente LCD · `index.html`

El clásico de la serpiente con estética de consola de bolsillo. Desliza el dedo o usa la cruceta.
Cada manzana vale 10 puntos y acelera la serpiente. El récord se guarda en el dispositivo.
