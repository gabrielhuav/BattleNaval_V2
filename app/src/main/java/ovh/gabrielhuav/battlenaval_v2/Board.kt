package ovh.gabrielhuav.battlenaval_v2

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView

class Board(
    context: Context,
    val enemy: Boolean = false,
    private val onCellClick: (Cell) -> Unit
) : GridLayout(context) {
    var ships = 3
    private val cells = Array(7) { y ->
        Array(7) { x ->
            Cell(context, x, y, this).apply {
                setOnClickListener { onCellClick(this) }
            }
        }
    }

    init {
        rowCount = 8 // 7 filas más la fila de encabezado
        columnCount = 8 // 7 columnas más la columna de encabezado

        // Fondo transparente para evitar el negro
        setBackgroundColor(Color.TRANSPARENT)

        // Crear encabezados y celdas
        createHeadersAndCells()
    }

    private fun createHeadersAndCells() {
        // Encabezados de columnas (1-7)
        addView(TextView(context)) // Celda vacía en la esquina superior izquierda
        for (i in 1..7) {
            addView(TextView(context).apply {
                text = i.toString()
                gravity = Gravity.CENTER
                setBackgroundColor(Color.YELLOW)
                setTextColor(Color.BLACK)
                layoutParams = LayoutParams().apply {
                    width = 100
                    height = 100
                }
            })
        }

        // Filas con encabezados (A-G) y celdas
        for (y in 0 until 7) {
            // Encabezado de fila (A-G)
            addView(TextView(context).apply {
                text = ('A' + y).toString()
                gravity = Gravity.CENTER
                setBackgroundColor(Color.YELLOW)
                setTextColor(Color.BLACK)
                layoutParams = LayoutParams().apply {
                    width = 100
                    height = 100
                }
            })

            // Celdas del tablero
            for (x in 0 until 7) {
                addView(cells[y][x], LayoutParams().apply {
                    width = 100
                    height = 100
                })
            }
        }
    }

    fun placeShip(ship: Ship, x: Int, y: Int): Boolean {
        val length = ship.type

        // Verifica si el barco cabe en las celdas y no hay barcos que lo bloqueen
        if (ship.vertical) {
            for (i in y until y + length) {
                if (i >= 7 || cells[i][x].ship != null) return false
            }
        } else {
            for (i in x until x + length) {
                if (i >= 7 || cells[y][i].ship != null) return false
            }
        }

        // Coloca el barco en las celdas
        if (ship.vertical) {
            for (i in y until y + length) {
                cells[i][x].ship = ship
                cells[i][x].invalidate() // Redibuja la celda
            }
        } else {
            for (i in x until x + length) {
                cells[y][i].ship = ship
                cells[y][i].invalidate() // Redibuja la celda
            }
        }

        return true
    }

    fun getCell(x: Int, y: Int): Cell = cells[y][x]
}
