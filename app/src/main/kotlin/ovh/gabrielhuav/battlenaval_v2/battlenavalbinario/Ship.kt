package ovh.gabrielhuav.battlenaval_v2.battlenavalbinario

/** Equivale a `android.graphics.Color.MAGENTA`, escrito como literal para que
 *  esta clase no dependa de Android y se pueda probar en la JVM. */
private const val MAGENTA = 0xFFFF00FF.toInt()

/**
 * Un barco de la flota. Sin dependencias de Android a propósito: las reglas del
 * juego viven en código puro para que [BoardModel] y sus tests funcionen sin
 * emulador.
 *
 * @param type longitud del barco en celdas; también identifica su tipo de dibujo.
 * @param vertical orientación con la que se colocó.
 * @param color color base; solo lo usa la capa de dibujo.
 */
class Ship(
    val type: Int,
    val vertical: Boolean,
    val color: Int = MAGENTA
) {
    private var health: Int = type

    fun hit() {
        health--
    }

    fun isAlive(): Boolean = health > 0
}
