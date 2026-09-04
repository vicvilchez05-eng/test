package com.esforal.gamelauncher.domain.model

/**
 * Como se reparte la pantalla principal.
 *
 * Son dos disposiciones de los mismos elementos, no dos aplicaciones: las dos
 * usan la misma fila de paginas, la misma barra de rendimiento y los mismos
 * botones. Lo que cambia es donde va cada cosa y que tamano tienen las fichas.
 *
 * Se guarda por nombre y no por ordinal, igual que el tema: reordenar el enum
 * no debe cambiar la disposicion que el usuario habia elegido.
 */
enum class HomeLayout {

    /**
     * La original. La pagina del juego ocupa la pantalla entera, el cromo
     * flota arriba y las pestanas son pequenas, abajo.
     *
     * El fondo que el usuario eligio es el protagonista absoluto.
     */
    PAGE,

    /**
     * Estilo consola de salon. Fila de fichas grandes en el centro con la
     * seleccionada destacada, y todo el cromo -ajustes, rendimiento y perfil-
     * recogido en una barra inferior.
     *
     * Aqui el protagonista es el catalogo: se ve de un vistazo lo que hay y
     * cuesta menos saltar entre juegos, a cambio de que el fondo elegido pase
     * a ser ambiente en lugar de contenido.
     */
    CONSOLE,

    /**
     * El inicio de Hexpad PC en modo "Rejilla", traido tal cual: un carrusel de
     * cartas verticales con la elegida en el centro, mas grande y encendida, y
     * el fondo del juego a pantalla completa por detras.
     *
     * Frente a [CONSOLE], que reparte las portadas en cuadrados de la misma
     * fila, aqui hay una sola carta protagonista y las vecinas solo asoman. Se
     * ve menos catalogo de un vistazo y se reconoce antes el juego elegido.
     */
    CAROUSEL,

    /**
     * El inicio de Hexpad PC en modo "Consola", traido tal cual: una franja de
     * iconos grandes en la parte alta con los juegos primero y las funciones
     * despues, el elegido mas grande con su nombre debajo, y una zona baja
     * pequena con la ficha y lo ultimo jugado.
     *
     * Se diferencia de [CONSOLE] en donde vive el cromo: alli en una barra
     * inferior, y aqui como iconos mas al final de la propia franja, que es lo
     * que hace que la pantalla entera se recorra con un solo gesto lateral.
     */
    STRIP,
}

