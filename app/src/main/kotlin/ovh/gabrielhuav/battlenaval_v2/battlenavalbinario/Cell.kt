package ovh.gabrielhuav.battlenaval_v2.battlenavalbinario

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
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
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF() // Para dibujar rectángulos con bordes redondeados
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

        val w = width.toFloat()
        val h = height.toFloat()

        // 1. Dibuja el fondo (Agua de mar profunda)
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#112240") // Azul oscuro
        canvas.drawRect(0f, 0f, w, h, paint)

        // 2. Dibuja las líneas de la cuadrícula (como un radar)
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#233554") // Azul más claro para el borde
        paint.strokeWidth = 3f
        canvas.drawRect(0f, 0f, w, h, paint)

// 3. ALTERNATIVA 1: DISEÑO "INTERCEPTOR" (ESTRUCTURADO Y ANGULAR)
        if (ship != null && (!board.enemy || wasShot)) {

            // Colores base estilo Bandera de México (tonos tácticos/oscuros para el radar)
            val baseColor = when(ship!!.type) {
                4 -> Color.parseColor("#2E7D32") // Verde táctico (Portaaviones)
                3 -> Color.parseColor("#E0E0E0") // Blanco humo (Submarino)
                2 -> Color.parseColor("#C62828") // Rojo táctico (Destructor)
                else -> Color.parseColor("#78909C")
            }

            // Color de los detalles (contraste ajustado)
            val detailColor = when (ship!!.type) {
                4 -> Color.parseColor("#A5D6A7") // Verde claro
                3 -> Color.parseColor("#37474F") // Gris oscuro sobre blanco
                2 -> Color.parseColor("#FFCDD2") // Rojo claro
                else -> Color.parseColor("#90A4AE")
            }

            paint.style = Paint.Style.FILL
            paint.color = if (wasShot && board.enemy) Color.parseColor("#212121") else baseColor

            val paddingH = w * 0.10f // Margen horizontal ligero
            val paddingV = h * 0.05f // Margen vertical mínimo

            // --- DIBUJO DEL CASCO ANGULAR ---
            // Usamos Path para dibujar un trapezoide que parece un segmento de casco metálico
            val hullPath = Path()
            if (ship!!.vertical) {
                // Forma vertical (más ancha arriba, ligeramente más estrecha abajo)
                hullPath.moveTo(paddingH * 1.5f, paddingV)
                hullPath.lineTo(w - paddingH * 1.5f, paddingV)
                hullPath.lineTo(w - paddingH, h - paddingV)
                hullPath.lineTo(paddingH, h - paddingV)
                hullPath.close()
            } else {
                // Forma horizontal (más alta a la izquierda, más baja a la derecha)
                hullPath.moveTo(paddingH, paddingV * 1.5f)
                hullPath.lineTo(w - paddingH, paddingV * 1.5f)
                hullPath.lineTo(w - paddingH, h - paddingV)
                hullPath.lineTo(paddingH, h - paddingV)
                hullPath.close()
            }
            canvas.drawPath(hullPath, paint)

            // --- DIBUJO DE DETALLES ESTRUCTURALES ---
            paint.style = Paint.Style.STROKE
            paint.color = detailColor
            paint.strokeWidth = 3f

            // 1. Quilla central texturizada (da volumen)
            if (ship!!.vertical) {
                canvas.drawLine(w/2f, paddingV, w/2f, h - paddingV, paint)
                // Pequeñas muescas horizontales en la quilla
                canvas.drawLine(w/2f - 10f, h*0.3f, w/2f + 10f, h*0.3f, paint)
                canvas.drawLine(w/2f - 10f, h*0.7f, w/2f + 10f, h*0.7f, paint)
            } else {
                canvas.drawLine(paddingH, h/2f, w - paddingH, h/2f, paint)
                // Pequeñas muescas verticales en la quilla
                canvas.drawLine(w*0.3f, h/2f - 10f, w*0.3f, h/2f + 10f, paint)
                canvas.drawLine(w*0.7f, h/2f - 10f, w*0.7f, h/2f + 10f, paint)
            }

            // 2. Detalles específicos por tipo (adaptados al nuevo diseño)
            when (ship!!.type) {
                4 -> { // Portaaviones (Verde): Pista de aterrizaje simplificada
                    paint.strokeWidth = 2f
                    if (ship!!.vertical) {
                        canvas.drawLine(w*0.3f, paddingV*4, w*0.3f, h - paddingV*4, paint)
                        canvas.drawLine(w*0.7f, paddingV*4, w*0.7f, h - paddingV*4, paint)
                    } else {
                        canvas.drawLine(paddingH*4, h*0.3f, w - paddingH*4, h*0.3f, paint)
                        canvas.drawLine(paddingH*4, h*0.7f, w - paddingH*4, h*0.7f, paint)
                    }
                }
                3 -> { // Submarino (Blanco): Dos escotillas más pequeñas
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(w*0.35f, h/2f, w * 0.08f, paint)
                    canvas.drawCircle(w*0.65f, h/2f, w * 0.08f, paint)
                }
                2 -> { // Destructor (Rojo): Antena/Cañón en ángulo
                    paint.strokeWidth = 4f
                    if (ship!!.vertical) {
                        canvas.drawLine(w/2f, paddingV*2, w/2f + 15f, paddingV*2 + 15f, paint)
                    } else {
                        canvas.drawLine(w - paddingH*2, h/2f, w - paddingH*2 - 15f, h/2f - 15f, paint)
                    }
                }
            }
        }

        // 4. Dibuja el estado del disparo (Hit o Agua)
        if (wasShot) {
            if (ship != null) {
                // ACERTASTE (HIT) - Dibuja una explosión / Mira roja
                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#FF3D00") // Naranja/Rojo fuego
                canvas.drawCircle(w / 2f, h / 2f, w * 0.35f, paint)

                // Núcleo amarillo de la explosión
                paint.color = Color.parseColor("#FFEA00")
                canvas.drawCircle(w / 2f, h / 2f, w * 0.15f, paint)
            } else {
                // FALLASTE (AGUA) - Dibuja una marca de "Splash" blanca/azul claro
                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#80D8FF") // Celeste agua
                canvas.drawCircle(w / 2f, h / 2f, w * 0.2f, paint)

                // Aro exterior del agua
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                canvas.drawCircle(w / 2f, h / 2f, w * 0.3f, paint)
            }
        }

        // 5. Opcional: Dibuja texto si estás usándolo para algún debug o reto binario
        text?.let {
            paint.style = Paint.Style.FILL
            paint.color = Color.WHITE
            paint.textSize = w * 0.4f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(
                it,
                w / 2f,
                h / 2f - (paint.descent() + paint.ascent()) / 2,
                paint
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
