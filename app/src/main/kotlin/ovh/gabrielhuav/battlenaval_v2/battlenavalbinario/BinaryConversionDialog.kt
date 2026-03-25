package ovh.gabrielhuav.battlenaval_v2.battlenavalbinario

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.random.Random

/**
 * Diálogo para mostrar el desafío de conversión binaria
 */
class BinaryConversionDialog(private val context: Context) {

    private var lastBinaryX: String? = null
    private var lastBinaryY: String? = null
    private var lastX: Int? = null
    private var lastY: Int? = null

    init {
        Log.d("BinaryConversionDialog", "Inicializando BinaryConversionDialog")
        // Asegurarnos de que las variables están vacías al inicio
        resetCoordinates()
    }

    private fun resetCoordinates() {
        lastBinaryX = null
        lastBinaryY = null
        lastX = null
        lastY = null
        Log.d("BinaryConversionDialog", "Coordenadas reseteadas")
    }

    /**
     * Muestra el diálogo con el desafío de conversión
     * @param x Coordenada x del barco revelado (0-6)
     * @param y Coordenada y del barco revelado (0-6)
     * @param onCorrectConversion Callback cuando la conversión es correcta o incorrecta
     * @param reuseLastCoordinate Si se debe reutilizar la última coordenada mostrada
     */
    fun showConversionChallenge(
        x: Int?,
        y: Int?,
        onCorrectConversion: (Int, Int, Boolean) -> Unit,
        reuseLastCoordinate: Boolean = false
    ) {
        // Si no se solicita reutilizar y se proporcionan nuevas coordenadas, actualizamos
        if (!reuseLastCoordinate && x != null && y != null) {
            lastX = x
            lastY = y
            // Resetear también los valores binarios para que se generen nuevos
            lastBinaryX = null
            lastBinaryY = null
        }

        // Usar las coordenadas actuales o las últimas almacenadas
        val coordX = lastX ?: x ?: (0..6).random()
        val coordY = lastY ?: y ?: (0..6).random()

        // Si las coordenadas son nuevas (no almacenadas antes), actualizar
        if (lastX == null || lastY == null) {
            lastX = coordX
            lastY = coordY
        }

        Log.d("BinaryConversionDialog", "Usando coordenadas: ($coordX, $coordY) => ${('A' + coordY)}${coordX+1}")

        // Convertir coordenadas a caracteres/números para la interfaz
        // IMPORTANTE: Invertimos las coordenadas aquí para que coincidan con la orientación del tablero del juego
        // La letra (A-G) ahora será la fila y el número (1-7) será la columna
        val charY = ('A' + coordY)  // Ahora Y es la letra (antes era coordY+1 como número)
        val numX = coordX + 1        // Ahora X es el número (antes era ('A' + coordX) como letra)

        // Convertir a binario (o reutilizar valores previos)
        // IMPORTANTE: Invertimos también las conversiones
        val binaryY = lastBinaryY ?: BinaryConversionHelper.charToBinary(charY)  // Coordenada vertical como ASCII
        val binaryX = lastBinaryX ?: BinaryConversionHelper.numberToBinary(numX) // Coordenada horizontal como número

        // Guardar valores binarios
        lastBinaryX = binaryX
        lastBinaryY = binaryY

        // Crear el diálogo
        val builder = AlertDialog.Builder(context)
        builder.setTitle("¡Desafío de Conversión Binaria!")

        // Crear el layout del diálogo
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(30, 30, 30, 30)

        // Texto explicativo (modificado para ocultar las coordenadas reales)
        val textView = TextView(context)
        textView.text = "Coordenada binaria revelada:\n" +
                "Vertical (ASCII): $binaryY - Letras de A a G\n" +
                "Horizontal (Decimal): $binaryX - Números del 1 al 7\n\n" +
                "Convierte estos valores para poder disparar en esta ubicación."
        layout.addView(textView)

        // Campo para entrada horizontal (carácter)
        val editTextX = EditText(context)
        editTextX.hint = "Coordenada horizontal (A-G)"
        editTextX.inputType = InputType.TYPE_CLASS_TEXT
        layout.addView(editTextX)

        // Campo para entrada vertical (número)
        val editTextY = EditText(context)
        editTextY.hint = "Coordenada vertical (1-7)"
        editTextY.inputType = InputType.TYPE_CLASS_NUMBER
        layout.addView(editTextY)

        // Añadir información de depuración (solo durante desarrollo)
        /*
        val debugText = TextView(context)
        debugText.text = "DEBUG - Coordenada real: ${charY}${numX}"
        debugText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
        layout.addView(debugText)
        */

        builder.setView(layout)

        // Botones del diálogo
        builder.setPositiveButton("Verificar") { dialog, _ ->
            val inputX = editTextX.text.toString().uppercase()
            val inputY = editTextY.text.toString()

            if (inputX.isEmpty() || inputY.isEmpty()) {
                Toast.makeText(context, "Por favor, completa ambos campos", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                // Mostrar el mismo desafío nuevamente
                showConversionChallenge(coordX, coordY, onCorrectConversion, true)
                return@setPositiveButton
            }

            if (inputX.length != 1 || inputX[0] !in 'A'..'G') {
                Toast.makeText(context, "La coordenada horizontal debe ser una letra entre A y G", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                showConversionChallenge(coordX, coordY, onCorrectConversion, true)
                return@setPositiveButton
            }

            val numericY = try {
                inputY.toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "La coordenada vertical debe ser un número entre 1 y 7", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                showConversionChallenge(coordX, coordY, onCorrectConversion, true)
                return@setPositiveButton
            }

            if (numericY !in 1..7) {
                Toast.makeText(context, "La coordenada vertical debe estar entre 1 y 7", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                showConversionChallenge(coordX, coordY, onCorrectConversion, true)
                return@setPositiveButton
            }

            // Verificar conversión
            // IMPORTANTE: También invertimos la verificación
            val isCorrect = BinaryConversionHelper.checkConversion(
                binaryY,  // Vertical (ASCII)
                binaryX,  // Horizontal (número)
                inputX[0], // El usuario sigue ingresando letra para X
                numericY   // El usuario sigue ingresando número para Y
            )

            if (isCorrect) {
                Toast.makeText(context, "¡Conversión correcta! Disparando en la ubicación revelada.", Toast.LENGTH_SHORT).show()
                resetCoordinates() // Resetear coordenadas después de conversión correcta
                onCorrectConversion(coordX, coordY, true) // Pasar true para indicar acierto
                dialog.dismiss()
            } else {
                // Cuando falla, calcular coordenadas para un disparo incorrecto
                val incorrectX = inputX[0] - 'A'
                val incorrectY = numericY - 1

                if (incorrectX in 0..6 && incorrectY in 0..6) {
                    // La coordenada ingresada es válida para el tablero

                    // Notificar el error de conversión pero permitir el disparo en una coordenada errónea
                    Toast.makeText(context, "Conversión incorrecta. Disparando en la coordenada ingresada.", Toast.LENGTH_SHORT).show()

                    dialog.dismiss()

                    // Mostrar el fragment de tutorial
                    // Elegir aleatoriamente entre tutorial de ASCII o binario
                    val tutorialType = if (Random.nextBoolean())
                        ConversionTutorialFragment.TYPE_ASCII
                    else
                        ConversionTutorialFragment.TYPE_BINARY

                    val tutorialFragment = ConversionTutorialFragment.newInstance(tutorialType)

                    // Si context es una actividad con FragmentManager
                    if (context is AppCompatActivity) {
                        tutorialFragment.show((context as AppCompatActivity).supportFragmentManager, "tutorial")
                    }

                    // Disparo fallido en la coordenada incorrecta que ingresó
                    onCorrectConversion(incorrectY, incorrectX, false) // Pasar false para indicar error
                } else {
                    // Coordenada fuera de rango
                    Toast.makeText(context, "Coordenada fuera de rango. Inténtalo nuevamente.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()

                    // Mostrar el mismo desafío nuevamente
                    showConversionChallenge(coordX, coordY, onCorrectConversion, true)
                }
            }
        }

        builder.setNegativeButton("Cancelar", null)

        builder.show()
    }
}