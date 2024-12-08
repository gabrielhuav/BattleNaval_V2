package ovh.gabrielhuav.battlenaval_v2

import android.graphics.Color

data class Ship(
    val size: Int, // Tamaño del barco
    val vertical: Boolean,
    val color: Int = Color.TRANSPARENT // Por defecto, los barcos enemigos son transparentes
) {
    private var health: Int = size

    fun hit() {
        if (health > 0) health--
    }

    fun isAlive(): Boolean = health > 0
}
