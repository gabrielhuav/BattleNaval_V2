package ovh.gabrielhuav.battlenaval_v2

import android.content.Context
import android.graphics.Color
import android.view.DragEvent
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import kotlin.random.Random

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
                setOnLongClickListener { startDrag(it); true }
            }
        }
    }

    var scaleFactor = 1.0f
        private set
    private var cellSize = 100

    init {
        rowCount = 8
        columnCount = 8
        drawBoard()

        setOnDragListener { _, event -> handleDrag(event) }
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

    private fun startDrag(view: View) {
        val cell = view as? Cell ?: return
        val dragData = "${cell.x},${cell.y}"
        val dragShadow = View.DragShadowBuilder(view)

        view.startDragAndDrop(
            null,
            dragShadow,
            dragData,
            0
        )
    }

    private fun handleDrag(event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> return true
            DragEvent.ACTION_DRAG_ENTERED -> return true
            DragEvent.ACTION_DRAG_LOCATION -> return true
            DragEvent.ACTION_DROP -> {
                val dropX = event.x.toInt() / cellSize
                val dropY = event.y.toInt() / cellSize

                if (dropX in 0..6 && dropY in 0..6) {
                    val draggedData = event.localState as String
                    val (draggedX, draggedY) = draggedData.split(",").map { it.toInt() }
                    val draggedCell = cells[draggedY][draggedX]
                    val targetCell = cells[dropY][dropX]

                    moveShip(draggedCell, targetCell)
                }
                return true
            }
            DragEvent.ACTION_DRAG_ENDED -> return true
            else -> return false
        }
    }

    private fun moveShip(draggedCell: Cell, targetCell: Cell) {
        val ship = draggedCell.ship ?: return
        val length = ship.size

        // Check bounds and conflicts
        val startX = targetCell.x
        val startY = targetCell.y

        if (ship.vertical) {
            for (i in startY until startY + length) {
                if (i >= 7 || cells[i][startX].ship != null) return
            }
        } else {
            for (i in startX until startX + length) {
                if (i >= 7 || cells[startY][i].ship != null) return
            }
        }

        // Remove from original position
        if (ship.vertical) {
            for (i in draggedCell.y until draggedCell.y + length) {
                cells[i][draggedCell.x].ship = null
                cells[i][draggedCell.x].invalidate()
            }
        } else {
            for (i in draggedCell.x until draggedCell.x + length) {
                cells[draggedCell.y][i].ship = null
                cells[draggedCell.y][i].invalidate()
            }
        }

        // Place at new position
        if (ship.vertical) {
            for (i in startY until startY + length) {
                cells[i][startX].ship = ship
                cells[i][startX].invalidate()
            }
        } else {
            for (i in startX until startX + length) {
                cells[startY][i].ship = ship
                cells[startY][i].invalidate()
            }
        }
    }

    fun placeShip(ship: Ship, x: Int, y: Int): Boolean {
        val length = ship.size
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

    fun getRandomShipCell(): Cell {
        val shipCells = cells.flatten().filter { it.ship != null && !it.wasShot }
        if (shipCells.isEmpty()) {
            throw IllegalStateException("No hay celdas disponibles de barcos para revelar.")
        }
        return shipCells.random()
    }
}
