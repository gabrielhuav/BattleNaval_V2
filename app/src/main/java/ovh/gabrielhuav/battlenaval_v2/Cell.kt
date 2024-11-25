package ovh.gabrielhuav.battlenaval_v2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class Cell(
    context: Context,
    val x: Int,
    val y: Int,
    private val board: Board
) : View(context) {
    var ship: Ship? = null
    var wasShot: Boolean = false
    private val paint = Paint()
    private val borderPaint = Paint()

    init {
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE // Fondo blanco por defecto

        borderPaint.style = Paint.Style.STROKE
        borderPaint.color = Color.BLACK // Borde negro por defecto
        borderPaint.strokeWidth = 2f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = 100 // Tamaño fijo para cada celda
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Si la celda tiene un barco, usa su color
        ship?.let {
            if (!board.enemy) {
                paint.color = it.color
            }
        }

        // Dibuja el fondo
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Dibuja el borde
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), borderPaint)

        // Si fue disparada, dibuja el estado
        if (wasShot) {
            paint.color = if (ship != null) Color.RED else Color.BLUE
            canvas.drawCircle(width / 2f, height / 2f, width / 4f, paint)
        }
    }

    fun shoot(): Boolean {
        wasShot = true
        invalidate()

        return ship?.let {
            it.hit()
            if (!it.isAlive()) {
                board.ships--
            }
            true
        } ?: false
    }
}
