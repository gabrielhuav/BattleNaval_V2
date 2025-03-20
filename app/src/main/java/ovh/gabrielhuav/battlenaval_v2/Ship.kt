package ovh.gabrielhuav.battlenaval_v2

import android.graphics.Color

class Ship(
    val type: Int, // Tamaño del barco
    val vertical: Boolean,
    val color: Int = Color.MAGENTA // Por defecto, los barcos enemigos son transparentes
) {
    private var health: Int = type

    fun hit() {
        health--
    }

    fun isAlive(): Boolean = health > 0
}
