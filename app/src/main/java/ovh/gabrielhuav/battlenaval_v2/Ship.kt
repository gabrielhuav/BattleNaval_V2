package ovh.gabrielhuav.battlenaval_v2

import android.graphics.Color

class Ship(
    val type: Int, // 4 para barco de 4 casillas, 3 para el de 3, 2 para el de 2
    var vertical: Boolean = true
) {
    private var health: Int = type
    val color: Int = when (type) {
        4 -> Color.GREEN // Barco más grande (4x1) en verde
        3 -> Color.GRAY  // Barco mediano (3x1) en gris
        2 -> Color.RED   // Barco más pequeño (2x1) en rojo
        else -> Color.WHITE
    }

    fun hit() {
        health--
    }

    fun isAlive(): Boolean = health > 0
}
