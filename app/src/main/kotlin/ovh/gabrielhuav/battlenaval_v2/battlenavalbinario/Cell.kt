package ovh.gabrielhuav.battlenaval_v2.battlenavalbinario

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
    var text: String? = null // Nuevo: Texto para mostrar en la celda
    private val paint = Paint()
    private val textPaint = Paint()

    init {
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE // Fondo blanco por defecto

        textPaint.apply {
            color = Color.BLACK
            textSize = 40f
            textAlign = Paint.Align.CENTER
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = 100 // Tamaño fijo para cada celda
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Dibuja el fondo
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Dibuja el borde
        val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.BLACK
            strokeWidth = 2f
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), borderPaint)

        // Dibuja el barco en el tablero (jugador o enemigo)
        if (ship != null && !wasShot) {
            paint.color = ship!!.color // Usa el color del barco
            canvas.drawRect(5f, 5f, width.toFloat() - 5f, height.toFloat() - 5f, paint)
            paint.color = Color.WHITE // Restaura el color original
        }


        // Si fue disparada, dibuja el estado
        if (wasShot) {
            val symbol = if (ship != null) "1" else "0" // Muestra 1 si hay barco, 0 si no
            canvas.drawText(
                symbol,
                width / 2f,
                height / 2f - (textPaint.descent() + textPaint.ascent()) / 2,
                textPaint
            )
        }

        // Dibuja el texto personalizado si existe
        text?.let {
            canvas.drawText(
                it,
                width / 2f,
                height / 2f - (textPaint.descent() + textPaint.ascent()) / 2,
                textPaint
            )
        }
    }

    fun shoot(): Boolean {
        wasShot = true
        invalidate() // Redibuja la celda

        return ship?.let {
            it.hit()
            if (!it.isAlive()) {
                board.ships-- // Reduce la cantidad de barcos si este ya no está vivo
            }
            true // Devuelve true si se le atinó a un barco
        } ?: false // Devuelve false si no hay barco
    }

    /**
     * Cambia el color de fondo de la celda.
     */
    override fun setBackgroundColor(newColor: Int) {
        paint.color = newColor
        invalidate() // Redibuja la celda
    }
}
