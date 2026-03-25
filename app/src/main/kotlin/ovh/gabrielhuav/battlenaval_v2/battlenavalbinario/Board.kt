package ovh.gabrielhuav.battlenaval_v2.battlenavalbinario

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.MotionEvent
import android.widget.GridLayout
import android.widget.TextView

class Board(
    context: Context,
    val enemy: Boolean = false,
    private val onCellClick: (Cell) -> Unit
) : GridLayout(context) {

    var ships = 3
    val cells = Array(7) { y ->
        Array(7) { x ->
            Cell(context, x, y, this).apply {
                setOnClickListener { onCellClick(this) }
            }
        }
    }

    var scaleFactor = 1.0f
        private set
    private var cellSize = 100

    private var rowLabels = List(7) { y -> ('A' + y).toString() } // Etiquetas por defecto

    init {
        rowCount = 8
        columnCount = 8
        drawBoard()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }

    private fun drawBoard() {
        removeAllViews()

        // Encabezado vacío
        addView(TextView(context).apply {
            gravity = Gravity.CENTER
            text = ""
            setBackgroundColor(Color.LTGRAY)
        })

        // Encabezados de columnas
        for (i in 1..7) {
            addView(TextView(context).apply {
                text = i.toString()
                gravity = Gravity.CENTER
                setBackgroundColor(Color.YELLOW)
                setTextColor(Color.BLACK)
                layoutParams = LayoutParams(cellSize, cellSize)
                textSize = (cellSize * 0.2f).coerceAtLeast(12f)
            })
        }

        // Encabezados de filas y celdas
        for (y in 0 until 7) {
            // Encabezados de fila
            addView(TextView(context).apply {
                text = rowLabels[y]
                gravity = Gravity.CENTER
                setBackgroundColor(Color.YELLOW)
                setTextColor(Color.BLACK)
                layoutParams = LayoutParams(cellSize, cellSize)
                textSize = (cellSize * 0.2f).coerceAtLeast(12f)
            })

            // Celdas
            for (x in 0 until 7) {
                val cell = cells[y][x]
                cell.layoutParams = LayoutParams(cellSize, cellSize)
                addView(cell)
            }
        }
    }

    fun placeShip(ship: Ship, x: Int, y: Int): Boolean {
        val length = ship.type
        if (ship.vertical) {
            for (i in y until y + length) {
                if (i >= 7 || cells[i][x].ship != null) return false
            }
        } else {
            for (i in x until x + length) {
                if (i >= 7 || cells[y][i].ship != null) return false
            }
        }
        if (ship.vertical) {
            for (i in y until y + length) {
                cells[i][x].ship = ship
                cells[i][x].invalidate() // Fuerza redibujado de la celda
            }
        } else {
            for (i in x until x + length) {
                cells[y][i].ship = ship
                cells[y][i].invalidate() // Fuerza redibujado de la celda
            }
        }
        return true
    }

    fun getCell(x: Int, y: Int): Cell = cells[y][x]

    fun adjustCellSize(newScaleFactor: Float) {
        scaleFactor = newScaleFactor
        cellSize = (100 * scaleFactor).toInt().coerceAtLeast(20)
        drawBoard()
    }

    fun getShipCoordinates(): List<Pair<String, String>> {
        val coordinates = mutableListOf<Pair<String, String>>()

        for (y in cells.indices) {
            for (x in cells[y].indices) {
                val cell = cells[y][x]
                if (cell.ship != null) {
                    val letter = ('A' + y).toString() // Convierte el índice de fila a letra
                    val number = (x + 1).toString() // Convierte el índice de columna a número
                    coordinates.add(Pair(letter, number))
                }
            }
        }

        return coordinates
    }

    fun clearShips() {
        for (y in cells.indices) {
            for (x in cells[y].indices) {
                val cell = cells[y][x]
                cell.ship = null
                cell.invalidate() // Fuerza el redibujado de la celda
            }
        }
        ships = 0 // Reinicia el contador de barcos
    }

    /**
     * Actualiza las etiquetas de las filas del tablero.
     */
    fun setRowLabels(newLabels: List<String>) {
        if (newLabels.size == 7) {
            rowLabels = newLabels
            drawBoard() // Redibuja el tablero con las nuevas etiquetas
        } else {
            throw IllegalArgumentException("Se requieren exactamente 7 etiquetas para las filas.")
        }
    }

    /**
     * Método para forzar el redibujado del tablero y todas sus celdas.
     */
    fun redrawBoard() {
        for (y in cells.indices) {
            for (x in cells[y].indices) {
                cells[y][x].invalidate() // Fuerza el redibujado de cada celda
            }
        }
        invalidate() // Fuerza el redibujado del contenedor del tablero
    }
}
