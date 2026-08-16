package ovh.gabrielhuav.battlenaval_v2.battlenavalbinario

/** Lado del tablero. El juego usa una cuadrícula 7×7: columnas 1–7, filas A–G. */
const val BOARD_SIZE = 7

/** Barcos con los que arranca cada flota. */
const val DEFAULT_FLEET_SIZE = 3

/**
 * Reglas del tablero: colocación de barcos, disparos y conteo de flota.
 *
 * Es código puro, sin `android.*`, para que se pueda probar en la JVM. La vista
 * [Board] y sus celdas [Cell] delegan aquí y se quedan solo con el dibujo. Esa
 * separación es también lo que permitirá dibujar el tablero con Compose sin
 * reescribir las reglas: ver `docs/MIGRACION-COMPOSE.md`.
 *
 * Índices en base 0: `x` es la columna y `y` la fila.
 */
class BoardModel(val size: Int = BOARD_SIZE) {

    private val ships = Array(size) { arrayOfNulls<Ship>(size) }
    private val shots = Array(size) { BooleanArray(size) }

    /** Barcos a flote. Llega a 0 cuando la flota queda hundida. */
    var remainingShips: Int = DEFAULT_FLEET_SIZE

    private fun isInside(x: Int, y: Int) = x in 0 until size && y in 0 until size

    fun shipAt(x: Int, y: Int): Ship? = if (isInside(x, y)) ships[y][x] else null

    fun setShipAt(x: Int, y: Int, ship: Ship?) {
        if (isInside(x, y)) ships[y][x] = ship
    }

    fun wasShot(x: Int, y: Int): Boolean = isInside(x, y) && shots[y][x]

    fun setShot(x: Int, y: Int, value: Boolean) {
        if (isInside(x, y)) shots[y][x] = value
    }

    /**
     * Indica si [ship] cabe en (`x`, `y`) sin salirse ni encimarse con otro.
     * El barco ocupa celdas hacia abajo si es vertical, y hacia la derecha si no.
     */
    fun canPlace(ship: Ship, x: Int, y: Int): Boolean {
        if (!isInside(x, y)) return false
        val length = ship.type
        return if (ship.vertical) {
            (y until y + length).all { it < size && ships[it][x] == null }
        } else {
            (x until x + length).all { it < size && ships[y][it] == null }
        }
    }

    /** Coloca [ship] si cabe. Devuelve `false` y no modifica nada si no cabe. */
    fun placeShip(ship: Ship, x: Int, y: Int): Boolean {
        if (!canPlace(ship, x, y)) return false
        val length = ship.type
        if (ship.vertical) {
            for (i in y until y + length) ships[i][x] = ship
        } else {
            for (i in x until x + length) ships[y][i] = ship
        }
        return true
    }

    /**
     * Dispara a (`x`, `y`). Marca la celda, aplica el daño y descuenta la flota
     * cuando el barco se hunde. Devuelve `true` si había barco.
     *
     * No protege contra disparar dos veces la misma celda; quien llama lo evita
     * comprobando [wasShot] antes, igual que hacía la versión con vistas.
     */
    fun shoot(x: Int, y: Int): Boolean {
        if (!isInside(x, y)) return false
        setShot(x, y, true)
        val ship = ships[y][x] ?: return false
        ship.hit()
        if (!ship.isAlive()) remainingShips--
        return true
    }

    /** Quita todos los barcos y deja la flota en cero, como hacía `Board.clearShips()`. */
    fun clearShips() {
        for (y in 0 until size) {
            for (x in 0 until size) ships[y][x] = null
        }
        remainingShips = 0
    }

    /**
     * Celdas ocupadas por algún barco, como pares (`x`, `y`) en base 0.
     *
     * No uses `buildList` aquí: dentro de su lambda `size` resuelve al de la
     * lista en construcción, no al del tablero, y el recorrido queda vacío.
     */
    fun occupiedCells(): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until size) {
            for (x in 0 until size) {
                if (ships[y][x] != null) result.add(x to y)
            }
        }
        return result
    }

    /**
     * Celdas ocupadas en notación del juego: la fila como letra y la columna
     * como número. La celda (0, 0) es `("A", "1")`.
     */
    fun occupiedCoordinates(): List<Pair<String, String>> =
        occupiedCells().map { (x, y) -> ('A' + y).toString() to (x + 1).toString() }
}
