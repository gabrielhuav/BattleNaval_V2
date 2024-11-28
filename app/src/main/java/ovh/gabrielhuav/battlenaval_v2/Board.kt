package ovh.gabrielhuav.battlenaval_v2

import android.content.Context
import android.graphics.Color
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
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

    var scaleFactor = 1.0f
        private set
    private var cellSize = 100

    // Detector de gestos para pellizcar
    private val scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = scaleFactor * detector.scaleFactor
            adjustCellSize(scale.coerceIn(0.1f, 2.0f)) // Limitar el zoom entre 0.1x y 2.0x
            return true
        }
    })

    init {
        rowCount = 8
        columnCount = 8
        drawBoard()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return scaleGestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
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
            addView(TextView(context).apply {
                text = ('A' + y).toString()
                gravity = Gravity.CENTER
                setBackgroundColor(Color.YELLOW)
                setTextColor(Color.BLACK)
                layoutParams = LayoutParams(cellSize, cellSize)
                textSize = (cellSize * 0.2f).coerceAtLeast(12f)
            })

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

    fun adjustCellSize(newScaleFactor: Float) {
        scaleFactor = newScaleFactor
        cellSize = (100 * scaleFactor).toInt().coerceAtLeast(20)
        drawBoard()
    }
}
