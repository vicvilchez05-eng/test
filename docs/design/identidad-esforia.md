# Identidad visual de Esforia (análisis del repo `vicvilchez05-eng/esforia-app`)

Extraído el 2026-09-07 de `src/contexts/palettes.ts`, `src/services/themes.ts`, `src/styles/app.css`,
`src/components/ui/*` y `src/pages/{Home,Finance,Settings}.tsx`. Esta app adopta esta identidad
(HANDOFF D-021). Esforia es la referencia; no se modifica.

## 1. Paletas (tema por defecto "ritmo", morado)

| Clave | Claro | Oscuro | Uso |
|---|---|---|---|
| bg | `#F8F7FD` | degradado 165° `#17122A → #0C0A16` | fondo de pantalla |
| solidBg | `#F8F7FD` | `#17122A` | detrás de barras del sistema |
| surface | `#FFFFFF` | `#181229` | tarjetas (opacas) |
| line | `#E7E3F5` | blanco 8 % | bordes y separadores de 1px |
| ink | `#211C36` | `#F5F3FB` | texto principal |
| inkSoft | `#665F87` | `#A79FC7` | texto secundario, saludos, valores de fila |
| muted | `#9891B4` | `#7A7396` | iconos inactivos de la barra |
| mutedLight | `#C9C4E0` | `#4E4770` | placeholders |
| onAccent | `#FFFFFF` | `#FFFFFF` | texto sobre acento |
| moss (acento) | `#6C5CE7` | `#8B7CF6` | icono activo, botones, anillo |
| mossSoft | `#EBE7FD` | moss 16 % | teja de icono en filas, chips |
| mossText | `#4B3FBF` | `#C9BFFF` | texto de chip, botón outline |
| ember (verde) | `#0FA968` / soft `#DEF7EC` / text `#0B7A4C` | `#34D399` / 16 % / `#7EE8C4` | positivo, ahorro |
| blue | `#2E90D6` / soft `#DFF0FC` | `#7DD3FC` / 16 % | segundo acento, blobs |
| danger | `#D64545` | `#F87171` | errores, gasto |
| track | `#ECE9F8` | blanco 8 % | pista de anillos y switches |
| financeGradient | 135° `#8B5CF6 → #7C6CF0 (45 %) → #5B7FF5` | `#A78BFA → #8B7CF6 → #6E93F7` | hero |
| statusGood/Mid/Low | `#0F9D63` / `#B8862F` / `#BE6B60` (+ soft) | `#34D399` / `#E0B15C` / `#E08C82` | semáforos |
| glass | blanco 55 %, borde blanco 80 %, sombra `0 18 50 rgba(76,58,160,.16)` | blanco 7 %, borde 14 %, sombra negra 45 % | solo onboarding y elementos flotantes |

Los temas (rosa, sakura, lluvia, bosque, nieve, custom) cambian **solo** moss/mossSoft/mossText y
el degradado; greys, estados y categorías no cambian. Un tema = cuatro colores.

## 2. Tipografía (fuentes empaquetadas, no descargadas en runtime)

- **Sora** (`.voice`): títulos de página (22–26 px, peso 500), etiquetas de sección (19 px, 500),
  títulos de tarjeta (15 px), cifra del hero (32 px, 600). Pesos 400–700.
- **Manrope**: cuerpo y todo lo demás. Saludo 13 px inkSoft; fila 14,5 px ink; valor de fila 13 px
  inkSoft; etiqueta de grupo 11,5 px inkSoft MAYÚSCULAS tracking 0,4; etiqueta de hero 11 px
  blanco 85 % MAYÚSCULAS tracking 0,8 peso 600; etiqueta de stat 10 px; etiqueta de barra 9,5 px.
- **IBM Plex Mono** (`.mono`): cifras de dinero y porcentajes (13,5–18 px, peso 500–600).

## 3. Superficies

- **Card**: `surface` opaco, borde 1px `line`, radio 18, padding 16. **Opaco a propósito**: el
  resplandor de fondo vive en los márgenes, nunca tiñe una tarjeta ni resta contraste al texto.
- **Group** (ajustes): `surface`, borde `line`, radio 16, sin padding; filas de 50 px mínimo con
  padding 12×14, teja de icono 28×28 radio 9 fondo `mossSoft`, etiqueta 14,5 ink, valor 13
  inkSoft a la derecha, chevron; separador 1px `line` con inset 46. Encima, `GroupLabel` en
  mayúsculas 11,5 inkSoft con margen 8 abajo y 4 a la izquierda. Entre grupos 22 px.
- **Hero**: `financeGradient`, radio 22, padding 20×18, sombra `0 14 30 moss 24 %`, `overflow
  hidden` (decoración de tema pegada al borde inferior). Dentro: etiqueta MAYÚSCULAS blanca 85 %,
  cifra Sora 32 blanca, fila de **stat pills** (blanco 16 %, radio 14, padding 10×12; la
  destacada blanco 30 % + borde blanco 35 %).
- **Chip**: fondo `mossSoft`, texto `mossText`, 11 px, radio 20, padding 3×9.
- **Botón outline**: radio 11, borde `moss` 33 %, texto `mossText` 13 px, padding 12 vertical.
- **Botón circular**: 34–38 px, `surface` + borde `line` (o blanco 15 % sobre hero).
- **Switch**: 48×28, encendido `financeGradient` + sombra moss 28 %, apagado `track` + borde.
- **Ring**: anillo SVG, trazo 8–9, `moss` sobre `track` (blanco 28 % sobre hero).
- **GlassPanel**: `glass` + borde + sombra, `backdrop-filter: blur(16px) saturate(140%)`, solo en
  pocos elementos (onboarding). Nunca a pantalla completa.

## 4. Fondo ambiental (`AmbientBackground` + `.ambient-blob`)

- Tres blobs **en los márgenes**, no bajo el texto: `a` 240 px en (-60, -70); `b` 200 px a
  30 % de alto pegado a la derecha (-70); `c` 220 px abajo (-80) al 20 % de ancho.
- `filter: blur(46px)`, animaciones solo transform/opacity: `a` 26 s (+26,+34, escala 1→1,12,
  opacidad ,55→,8); `b` 32 s (−30,−26, 1,05→1, ,5→,75); `c` 29 s (+22,−30, 1→1,14, ,45→,7).
  `ease-in-out infinite`, ida y vuelta.
- Colores por pantalla (**tone**) y una **fuerza** global de la capa: home `[blue, moss, moss]`
  0,9 · finance `[moss, moss, blue]` 1 · goals `[moss, blue, moss]` 0,95 · progress
  `[blue, blue, moss]` 0,7 · profile `[moss, blue, ember]` 0,6 · settings `[moss, blue, moss]` 0,4.
- Pausado en pestañas no visibles; desactivado con "reducir movimiento".
- Decoraciones de tema (pétalos, lluvia, luciérnagas, nieve) en una capa hermana; fuera de alcance
  aquí salvo que Vic lo pida.

## 5. Estructura de pantalla

- Padding: `22 + statusBar` arriba, 18 lados, 28 abajo. El inset lo paga cada pantalla, no el
  contenedor, para que el fondo llegue hasta el borde superior.
- Cabecera **alineada a la izquierda**: línea de saludo o fecha (13, inkSoft) y `h1` Sora
  22–26 peso 500. Nada de títulos centrados en versalitas.
- Botones flotantes de perfil y ajustes arriba a la derecha (34 px, circulares, `surface`+`line`).
- Orden típico: título → hero → tarjeta resumen → `SectionLabel` (Sora 19, márgenes 22/10) →
  tarjetas/grupos.
- **Barra de pestañas**: `surface` con borde superior `line`, padding 10/4/12 + insets. Ítems en
  columna: icono lucide 19 px (trazo 2,3 activo / 1,8 inactivo), etiqueta 9,5 px; activo `moss`,
  inactivo `muted`; altura mínima 48. Sin indicador. (En esta app la barra sigue siendo flotante
  y se encoge con el scroll por exigencia del brief; solo adopta este estilo.)
- Iconos: **lucide** (línea fina). En Compose, `Icons.Outlined` inactivo / `Icons.Filled` activo.
- Feedback táctil: escala 0,98 + opacidad 0,9 al pulsar; sin ripple rectangular.
- Marca: teja morada con degradado `#8B5CF6 → #6C5CE7`, radio 22 %, "E" blanca. Splash morado.
