# Serpiente LCD

Un juego de la serpiente en un solo archivo HTML, con estética de consola de bolsillo
con pantalla LCD. Funciona en el celular y en la PC, sin instalar nada.

## Cómo jugar

Abre `index.html` en cualquier navegador.

- **Celular**: desliza el dedo sobre la pantalla verde o usa la cruceta. `A` juega o pausa, `B` reinicia.
- **PC**: flechas o `W` `A` `S` `D` para moverte, `espacio` para jugar o pausar, `P` pausa, `R` reinicia.

Cada manzana vale 10 puntos y hace que la serpiente vaya un poco más rápido.
Chocar con la pared o con tu propio cuerpo termina la partida.
El récord se guarda en el navegador del dispositivo.

## Detalles

- Tablero de 20 × 20 celdas dibujado en `<canvas>`, con los "píxeles apagados" de una LCD de verdad.
- Sin dependencias: HTML, CSS y JavaScript en un archivo. Las tipografías vienen de Google Fonts y
  tienen respaldo si no cargan.
- Controles por teclado, gestos táctiles y botones en pantalla, con vibración en celulares que la soportan.
