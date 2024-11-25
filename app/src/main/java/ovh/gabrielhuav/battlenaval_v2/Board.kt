package ovh.gabrielhuav.battlenaval_v2

import android.content.Context
import android.graphics.Color
import android.view.Gravity
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
        rowCount = 8 // Incluye encabezados
        columnCount = 8

        // Encabezados
        addView(TextView(context)) // Celda vacía en la esquina superior izquierda
        for (i in 1..7) {
            addView(TextView(context).apply {
                text = i.toString()
                gravity = Gravity.CENTER
                setBackgroundColor(Color.YELLOW)
                setTextColor(Color.BLACK)
                layoutParams = LayoutParams(100, 100)
            })
        }

        for (y in 0 until 7) {
            addView(TextView(context).apply {
                text = ('A' + y).toString()
                gravity = Gravity.CENTER
                setBackgroundColor(Color.YELLOW)
                setTextColor(Color.BLACK)
                layoutParams = LayoutParams(100, 100)
            })

            for (x in 0 until 7) {
                addView(cells[y][x], LayoutParams(100, 100))
            }
        }
    }

    fun placeShip(ship: Ship, x: Int, y: Int): Boolean {
        val length = ship.type

        // Verifica si el barco cabe
        if (ship.vertical) {
            for (i in y until y + length) {
                if (i >= 7 || cells[i][x].ship != null) return false
            }
        } else {
            for (i in x until x + length) {
                if (i >= 7 || cells[y][i].ship != null) return false
            }
        }

        // Coloca el barco
        if (ship.vertical) {
            for (i in y until y + length) {
                cells[i][x].ship = ship
                cells[i][x].invalidate()
            }
        } else {
            for (i in x until x + length) {
                cells[y][i].ship = ship
                cells[y][i].invalidate()
            }
        }

        return true
    }

    fun getCell(x: Int, y: Int): Cell = cells[y][x]
}
