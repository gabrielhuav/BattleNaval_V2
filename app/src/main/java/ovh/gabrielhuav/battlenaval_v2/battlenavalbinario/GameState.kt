package ovh.gabrielhuav.battlenaval_v2.battlenavalbinario

data class GameState(
    val playerShips: List<ShipData>,
    val enemyShips: List<ShipData>,
    val revealedCoordinates: List<Pair<Int, Int>>,
    val currentRevealedCoordinate: Pair<Int, Int>?,
    val serverCorrectAnswers: Int,
    val serverIncorrectAnswers: Int,
    val clientCorrectAnswers: Int,
    val clientIncorrectAnswers: Int
)

data class ShipData(
    val size: Int,
    val x: Int,
    val y: Int,
    val vertical: Boolean
)

// Extraer datos de los barcos en el tablero
fun getShipData(board: Board): List<ShipData> {
    return board.getShipCoordinates().map { (row, col) ->
        val x = col.toInt() - 1
        val y = row[0] - 'A'
        val ship = board.getCell(x, y).ship
        ShipData(
            size = ship?.type ?: 0,
            x = x,
            y = y,
            vertical = ship?.vertical ?: true
        )
    }
}
