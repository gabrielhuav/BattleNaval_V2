package ovh.gabrielhuav.battlenaval_v2.codigoascii

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R
import kotlin.random.Random

class AsciiPracticeGameFragment : Fragment() {

    private val TAG = "AsciiPracticeGameFragment"

    // Componentes UI principales
    private lateinit var tvScoreDisplay: TextView
    private lateinit var tvProgress: TextView

    // Componentes ASCII a Decimal
    private lateinit var tvAsciiToDecimalPrompt: TextView
    private lateinit var etAsciiToDecimalRespuesta: EditText
    private lateinit var btnVerificarAsciiToDecimal: Button
    private lateinit var tvFeedbackAsciiToDecimal: TextView

    // Componentes ASCII a Binario
    private lateinit var tvAsciiToBinaryPrompt: TextView
    private lateinit var etAsciiToBinaryRespuesta: EditText
    private lateinit var btnVerificarAsciiToBinary: Button
    private lateinit var tvFeedbackAsciiToBinary: TextView

    // Componentes para la tabla ASCII
    private lateinit var tvAsciiTableTitle: TextView
    private lateinit var btnMostrarTablaAscii: Button
    private lateinit var tableContainer: LinearLayout

    // Variables del juego
    private var puntuacionTotal = 0
    private var ejerciciosAsciiToDecimalRestantes = 5
    private var ejerciciosAsciiToBinaryRestantes = 5
    private var ejerciciosTotales = 10
    private var ejerciciosCompletados = 0

    // Valores actuales para conversión
    private var caracterAsciiActual = ""
    private var valorDecimalActual = 0
    private var valorBinarioActual = ""

    // Lista de caracteres ASCII usados comúnmente (imprimibles y dentro del rango 32-126)
    private val commonAsciiChars = (32..126).map { it.toChar() }.toList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ascii_practice_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar componentes de UI
        tvScoreDisplay = view.findViewById(R.id.tvScoreDisplay)
        tvProgress = view.findViewById(R.id.tvProgress)

        // Inicializar componentes de ASCII a Decimal
        tvAsciiToDecimalPrompt = view.findViewById(R.id.tvAsciiToDecimalPrompt)
        etAsciiToDecimalRespuesta = view.findViewById(R.id.etAsciiToDecimalRespuesta)
        btnVerificarAsciiToDecimal = view.findViewById(R.id.btnVerificarAsciiToDecimal)
        tvFeedbackAsciiToDecimal = view.findViewById(R.id.tvFeedbackAsciiToDecimal)

        // Inicializar componentes de ASCII a Binario
        tvAsciiToBinaryPrompt = view.findViewById(R.id.tvAsciiToBinaryPrompt)
        etAsciiToBinaryRespuesta = view.findViewById(R.id.etAsciiToBinaryRespuesta)
        btnVerificarAsciiToBinary = view.findViewById(R.id.btnVerificarAsciiToBinary)
        tvFeedbackAsciiToBinary = view.findViewById(R.id.tvFeedbackAsciiToBinary)

        // Inicializar componentes para la tabla ASCII
        tvAsciiTableTitle = view.findViewById(R.id.tvAsciiTableTitle)
        btnMostrarTablaAscii = view.findViewById(R.id.btnMostrarTablaAscii)
        tableContainer = view.findViewById(R.id.tableContainer)

        // Configurar que el EditText solo acepte números para decimal
        etAsciiToDecimalRespuesta.inputType = InputType.TYPE_CLASS_NUMBER

        // Configurar listeners de botones
        btnVerificarAsciiToDecimal.setOnClickListener { checkAsciiToDecimalRespuesta() }
        btnVerificarAsciiToBinary.setOnClickListener { checkAsciiToBinaryRespuesta() }
        btnMostrarTablaAscii.setOnClickListener { mostrarTablaAscii() }

        // Iniciar juego
        updateProgressDisplay()
        generarEjercicioAsciiToDecimal()
        generarEjercicioAsciiToBinary()
        crearTablaAsciiResumen()
    }

    private fun updateProgressDisplay() {
        tvScoreDisplay.text = "Puntuación: $puntuacionTotal / $ejerciciosTotales"
        val ejerciciosRestantes = ejerciciosAsciiToDecimalRestantes + ejerciciosAsciiToBinaryRestantes
        tvProgress.text = "Ejercicios restantes: $ejerciciosRestantes de $ejerciciosTotales"
    }

    private fun generarEjercicioAsciiToDecimal() {
        if (ejerciciosAsciiToDecimalRestantes <= 0) {
            tvAsciiToDecimalPrompt.text = "¡Todos los ejercicios de ASCII a Decimal completados!"
            etAsciiToDecimalRespuesta.isEnabled = false
            btnVerificarAsciiToDecimal.isEnabled = false
            return
        }

        // Seleccionar un carácter ASCII aleatorio de la lista de comunes
        caracterAsciiActual = commonAsciiChars[Random.nextInt(commonAsciiChars.size)].toString()

        // Obtener su valor decimal
        valorDecimalActual = caracterAsciiActual[0].code

        // Actualizar UI
        val displayChar = if (caracterAsciiActual == " ") "espacio" else caracterAsciiActual
        tvAsciiToDecimalPrompt.text = "Convierte este carácter ASCII a decimal:\n\"$displayChar\""
        etAsciiToDecimalRespuesta.text.clear()
        tvFeedbackAsciiToDecimal.text = ""
    }

    private fun generarEjercicioAsciiToBinary() {
        if (ejerciciosAsciiToBinaryRestantes <= 0) {
            tvAsciiToBinaryPrompt.text = "¡Todos los ejercicios de ASCII a Binario completados!"
            etAsciiToBinaryRespuesta.isEnabled = false
            btnVerificarAsciiToBinary.isEnabled = false
            return
        }

        // Seleccionar un carácter ASCII aleatorio de la lista de comunes
        // pero diferente al actual para evitar duplicar el ejercicio
        var nuevoCaracter: String
        do {
            nuevoCaracter = commonAsciiChars[Random.nextInt(commonAsciiChars.size)].toString()
        } while (nuevoCaracter == caracterAsciiActual && commonAsciiChars.size > 1)

        val charCode = nuevoCaracter[0].code
        valorBinarioActual = Integer.toBinaryString(charCode).padStart(8, '0')

        // Actualizar UI
        val displayChar = if (nuevoCaracter == " ") "espacio" else nuevoCaracter
        tvAsciiToBinaryPrompt.text = "Convierte este carácter ASCII a binario (8 bits):\n\"$displayChar\""
        etAsciiToBinaryRespuesta.text.clear()
        tvFeedbackAsciiToBinary.text = ""
    }

    private fun checkAsciiToDecimalRespuesta() {
        val respuesta = etAsciiToDecimalRespuesta.text.toString().trim()

        if (respuesta.isEmpty()) {
            tvFeedbackAsciiToDecimal.text = "Ingresa una respuesta"
            tvFeedbackAsciiToDecimal.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            return
        }

        try {
            val respuestaDecimal = respuesta.toInt()

            if (respuestaDecimal == valorDecimalActual) {
                // Respuesta correcta
                puntuacionTotal++
                tvFeedbackAsciiToDecimal.text = "¡Correcto! \"$caracterAsciiActual\" = $valorDecimalActual"
                tvFeedbackAsciiToDecimal.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
            } else {
                // Respuesta incorrecta
                tvFeedbackAsciiToDecimal.text = "Incorrecto. \"$caracterAsciiActual\" = $valorDecimalActual"
                tvFeedbackAsciiToDecimal.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            }

            // Actualizar progreso
            ejerciciosAsciiToDecimalRestantes--
            ejerciciosCompletados++
            updateProgressDisplay()

            // Programar próximo ejercicio
            btnVerificarAsciiToDecimal.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                btnVerificarAsciiToDecimal.isEnabled = true
                generarEjercicioAsciiToDecimal()

                // Verificar si se completaron todos los ejercicios
                if (ejerciciosAsciiToDecimalRestantes <= 0 && ejerciciosAsciiToBinaryRestantes <= 0) {
                    mostrarResultadosFinales()
                }
            }, 1500)

        } catch (e: NumberFormatException) {
            tvFeedbackAsciiToDecimal.text = "Por favor ingresa un número válido"
            tvFeedbackAsciiToDecimal.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
        }
    }

    private fun checkAsciiToBinaryRespuesta() {
        val respuesta = etAsciiToBinaryRespuesta.text.toString().trim()

        if (respuesta.isEmpty()) {
            tvFeedbackAsciiToBinary.text = "Ingresa una respuesta"
            tvFeedbackAsciiToBinary.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            return
        }

        // Limpiar y normalizar la respuesta (eliminar espacios y asegurar 8 bits)
        val respuestaLimpia = respuesta.replace(" ", "")

        // Verificar que solo contiene 0s y 1s
        if (!respuestaLimpia.matches(Regex("[01]+"))) {
            tvFeedbackAsciiToBinary.text = "La respuesta debe contener solo 0s y 1s"
            tvFeedbackAsciiToBinary.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            return
        }

        // Ignorar ceros a la izquierda en la comparación, pero verificar la equivalencia
        val respuestaDecimal = try {
            Integer.parseInt(respuestaLimpia, 2)
        } catch (e: NumberFormatException) {
            -1 // Valor imposible si hay error
        }

        val respuestaCorrecta = Integer.parseInt(valorBinarioActual, 2)

        if (respuestaDecimal == respuestaCorrecta) {
            // Respuesta correcta
            puntuacionTotal++
            tvFeedbackAsciiToBinary.text = "¡Correcto! El binario de \"${tvAsciiToBinaryPrompt.text.split("\n")[1].replace("\"", "")}\" es $valorBinarioActual"
            tvFeedbackAsciiToBinary.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
        } else {
            // Respuesta incorrecta
            tvFeedbackAsciiToBinary.text = "Incorrecto. El binario correcto es $valorBinarioActual"
            tvFeedbackAsciiToBinary.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
        }

        // Actualizar progreso
        ejerciciosAsciiToBinaryRestantes--
        ejerciciosCompletados++
        updateProgressDisplay()

        // Programar próximo ejercicio
        btnVerificarAsciiToBinary.isEnabled = false
        Handler(Looper.getMainLooper()).postDelayed({
            btnVerificarAsciiToBinary.isEnabled = true
            generarEjercicioAsciiToBinary()

            // Verificar si se completaron todos los ejercicios
            if (ejerciciosAsciiToDecimalRestantes <= 0 && ejerciciosAsciiToBinaryRestantes <= 0) {
                mostrarResultadosFinales()
            }
        }, 1500)
    }

    private fun crearTablaAsciiResumen() {
        // Inicialmente ocultamos la tabla
        tableContainer.visibility = View.GONE
    }

    private fun mostrarTablaAscii() {
        if (tableContainer.visibility == View.VISIBLE) {
            // Ocultar la tabla
            tableContainer.visibility = View.GONE
            btnMostrarTablaAscii.text = "Mostrar Tabla ASCII"
            Log.d(TAG, "Tabla ASCII ocultada")
        } else {
            // Mostrar la tabla
            tableContainer.visibility = View.VISIBLE
            btnMostrarTablaAscii.text = "Ocultar Tabla ASCII"
            Log.d(TAG, "Tabla ASCII mostrada")

            // Si no hay fragmento de tabla ASCII, creamos uno
            val fragmentContainer = view?.findViewById<FrameLayout>(R.id.asciiTableFragmentContainer)
            if (fragmentContainer != null) {
                // Verificar si el fragmento ya existe
                var fragment = childFragmentManager.findFragmentById(R.id.asciiTableFragmentContainer) as? AsciiTableFragment
                if (fragment == null) {
                    Log.d(TAG, "Creando nuevo AsciiTableFragment")
                    fragment = AsciiTableFragment()
                    val transaction = childFragmentManager.beginTransaction()
                    transaction.replace(R.id.asciiTableFragmentContainer, fragment)
                    transaction.commitNow() // Usamos commitNow para asegurar que el fragmento se agregue inmediatamente
                    Log.d(TAG, "AsciiTableFragment agregado con commitNow")
                } else {
                    Log.d(TAG, "AsciiTableFragment ya existe")
                }

                // Hacer scroll hacia la tabla para mostrarla al usuario
                fragmentContainer.post {
                    tableContainer.requestFocus()
                    val scrollView = view?.parent as? androidx.core.widget.NestedScrollView
                    scrollView?.smoothScrollTo(0, tableContainer.top)
                    // Forzar un desplazamiento adicional al final para asegurar que todo sea visible
                    scrollView?.post {
                        scrollView.fullScroll(View.FOCUS_DOWN)
                    }
                    Log.d(TAG, "Scroll realizado hacia la tabla")
                }
            } else {
                Log.e(TAG, "fragmentContainer es null")
            }
        }
    }

    private fun mostrarResultadosFinales() {
        // Calcular porcentaje
        val porcentaje = (puntuacionTotal * 100) / ejerciciosTotales

        // Preparar mensaje según puntuación
        val mensaje = when {
            porcentaje >= 90 -> "¡Excelente! Has dominado las conversiones ASCII."
            porcentaje >= 70 -> "¡Muy bien! Tienes un buen manejo de las conversiones ASCII."
            porcentaje >= 50 -> "¡Bien hecho! Sigue practicando para mejorar."
            else -> "Sigue practicando las conversiones ASCII para mejorar."
        }

        // Mostrar diálogo con resultados
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_results, null)

        val tvResultadoFinal = dialogView.findViewById<TextView>(R.id.tvResultadoFinal)
        val tvMensajeFinal = dialogView.findViewById<TextView>(R.id.tvMensajeFinal)
        val btnReiniciar = dialogView.findViewById<Button>(R.id.btnReiniciar)

        tvResultadoFinal.text = "Puntuación final: $puntuacionTotal de $ejerciciosTotales ($porcentaje%)"
        tvMensajeFinal.text = mensaje

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnReiniciar.setOnClickListener {
            // Reiniciar juego
            puntuacionTotal = 0
            ejerciciosAsciiToDecimalRestantes = 5
            ejerciciosAsciiToBinaryRestantes = 5
            ejerciciosCompletados = 0

            // Actualizar UI
            updateProgressDisplay()

            // Regenerar ejercicios
            generarEjercicioAsciiToDecimal()
            generarEjercicioAsciiToBinary()

            // Habilitar controles
            etAsciiToDecimalRespuesta.isEnabled = true
            btnVerificarAsciiToDecimal.isEnabled = true
            etAsciiToBinaryRespuesta.isEnabled = true
            btnVerificarAsciiToBinary.isEnabled = true

            // Cerrar diálogo
            dialog.dismiss()
        }

        dialog.show()
    }
}