package ovh.gabrielhuav.battlenaval_v2.sistemabinario

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R
import kotlin.random.Random

class SwitchesGameFragment : Fragment() {

    // Componentes UI principales
    private lateinit var tvScoreDisplay: TextView
    private lateinit var tvProgress: TextView

    // Componentes Binario a Decimal
    private lateinit var tvBinarioPrompt: TextView
    private lateinit var etBinarioRespuesta: EditText
    private lateinit var btnVerificarBinario: Button
    private lateinit var tvFeedbackBinario: TextView

    // Componentes Decimal a Binario
    private lateinit var tvDecimalPrompt: TextView
    private lateinit var binarySwitchesContainer: LinearLayout
    private lateinit var tvValorBinario: TextView
    private lateinit var btnVerificarDecimal: Button
    private lateinit var tvFeedbackDecimal: TextView

    // Interruptores binarios
    private val switches = arrayOfNulls<Switch>(8)
    private val bitValues = intArrayOf(128, 64, 32, 16, 8, 4, 2, 1)

    // Variables del juego
    private var puntuacionTotal = 0
    private var ejerciciosBinarioRestantes = 5
    private var ejerciciosDecimalRestantes = 5
    private var ejerciciosTotales = 10
    private var ejerciciosCompletados = 0

    // Valores actuales para conversión
    private var valorBinarioActual = ""
    private var valorDecimalActual = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_switches_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar componentes de UI
        tvScoreDisplay = view.findViewById(R.id.tvScoreDisplay)
        tvProgress = view.findViewById(R.id.tvProgress)

        // Inicializar componentes de Binario a Decimal
        tvBinarioPrompt = view.findViewById(R.id.tvBinarioPrompt)
        etBinarioRespuesta = view.findViewById(R.id.etBinarioRespuesta)
        btnVerificarBinario = view.findViewById(R.id.btnVerificarBinario)
        tvFeedbackBinario = view.findViewById(R.id.tvFeedbackBinario)

        // Inicializar componentes de Decimal a Binario
        tvDecimalPrompt = view.findViewById(R.id.tvDecimalPrompt)
        binarySwitchesContainer = view.findViewById(R.id.binarySwitchesContainer)
        tvValorBinario = view.findViewById(R.id.tvValorBinario)
        btnVerificarDecimal = view.findViewById(R.id.btnVerificarDecimal)
        tvFeedbackDecimal = view.findViewById(R.id.tvFeedbackDecimal)

        // Configurar interruptores binarios
        setupSwitches()

        // Configurar listeners de botones
        btnVerificarBinario.setOnClickListener { checkBinarioRespuesta() }
        btnVerificarDecimal.setOnClickListener { checkDecimalRespuesta() }

        // Iniciar juego
        updateProgressDisplay()
        generarEjercicioBinario()
        generarEjercicioDecimal()
    }

    private fun setupSwitches() {
        // Limpiar el contenedor
        binarySwitchesContainer.removeAllViews()

        // Crear 8 interruptores para representar valores de 0-255
        for (i in 0 until 8) {
            // Crear layout para cada interruptor
            val switchLayout = LinearLayout(context)
            switchLayout.orientation = LinearLayout.VERTICAL
            switchLayout.gravity = android.view.Gravity.CENTER

            // Crear interruptor
            val switch = Switch(context)
            switch.textOn = "1"
            switch.textOff = "0"
            switch.isChecked = false

            // Crear etiqueta para el valor
            val valueLabel = TextView(context)
            valueLabel.text = bitValues[i].toString()
            valueLabel.textAlignment = View.TEXT_ALIGNMENT_CENTER

            // Añadir vistas al layout
            switchLayout.addView(switch)
            switchLayout.addView(valueLabel)

            // Configurar el layout
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
            params.setMargins(4, 0, 4, 0)
            switchLayout.layoutParams = params

            // Guardar referencia al interruptor
            switches[i] = switch

            // Agregar listener para actualizar la visualización cuando cambie
            switch.setOnCheckedChangeListener { _, _ -> updateBinaryDisplay() }

            // Añadir al contenedor principal
            binarySwitchesContainer.addView(switchLayout)
        }
    }

    private fun updateBinaryDisplay() {
        // Construir la representación binaria desde los interruptores
        val binaryBuilder = StringBuilder()

        for (i in 0 until 8) {
            binaryBuilder.append(if (switches[i]?.isChecked == true) "1" else "0")
        }

        // Actualizar texto con el valor binario
        tvValorBinario.text = binaryBuilder.toString()
    }

    private fun updateProgressDisplay() {
        tvScoreDisplay.text = "Puntuación: $puntuacionTotal / $ejerciciosTotales"
        val ejerciciosRestantes = ejerciciosBinarioRestantes + ejerciciosDecimalRestantes
        tvProgress.text = "Ejercicios restantes: $ejerciciosRestantes de $ejerciciosTotales"
    }

    private fun generarEjercicioBinario() {
        if (ejerciciosBinarioRestantes <= 0) {
            tvBinarioPrompt.text = "¡Todos los ejercicios de Binario a Decimal completados!"
            etBinarioRespuesta.isEnabled = false
            btnVerificarBinario.isEnabled = false
            return
        }

        // Generar número aleatorio y convertirlo a binario
        val decimalValue = Random.nextInt(0, 256)
        var binaryString = Integer.toBinaryString(decimalValue)

        // Asegurar que tenga 8 bits
        while (binaryString.length < 8) {
            binaryString = "0$binaryString"
        }

        // Guardar valor actual y actualizar UI
        valorBinarioActual = binaryString
        tvBinarioPrompt.text = "Convierte este número binario a decimal:\n$binaryString"
        etBinarioRespuesta.text.clear()
        tvFeedbackBinario.text = ""
    }

    private fun generarEjercicioDecimal() {
        if (ejerciciosDecimalRestantes <= 0) {
            tvDecimalPrompt.text = "¡Todos los ejercicios de Decimal a Binario completados!"
            btnVerificarDecimal.isEnabled = false
            for (switch in switches) {
                switch?.isEnabled = false
            }
            return
        }

        // Generar número decimal aleatorio
        valorDecimalActual = Random.nextInt(0, 256)

        // Actualizar UI
        tvDecimalPrompt.text = "Convierte este número decimal a binario:\n$valorDecimalActual"

        // Resetear interruptores
        for (switch in switches) {
            switch?.isChecked = false
        }

        tvFeedbackDecimal.text = ""
        updateBinaryDisplay()
    }

    private fun checkBinarioRespuesta() {
        val respuesta = etBinarioRespuesta.text.toString().trim()

        if (respuesta.isEmpty()) {
            tvFeedbackBinario.text = "Ingresa una respuesta"
            tvFeedbackBinario.setTextColor(Color.RED)
            return
        }

        try {
            val respuestaDecimal = respuesta.toInt()
            val valorCorrecto = Integer.parseInt(valorBinarioActual, 2)

            if (respuestaDecimal == valorCorrecto) {
                // Respuesta correcta
                puntuacionTotal++
                tvFeedbackBinario.text = "¡Correcto! $valorBinarioActual = $valorCorrecto"
                tvFeedbackBinario.setTextColor(Color.GREEN)
            } else {
                // Respuesta incorrecta
                tvFeedbackBinario.text = "Incorrecto. $valorBinarioActual = $valorCorrecto"
                tvFeedbackBinario.setTextColor(Color.RED)
            }

            // Actualizar progreso
            ejerciciosBinarioRestantes--
            ejerciciosCompletados++
            updateProgressDisplay()

            // Programar próximo ejercicio
            btnVerificarBinario.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                btnVerificarBinario.isEnabled = true
                generarEjercicioBinario()

                // Verificar si se completaron todos los ejercicios
                if (ejerciciosBinarioRestantes <= 0 && ejerciciosDecimalRestantes <= 0) {
                    mostrarResultadosFinales()
                }
            }, 1500)

        } catch (e: NumberFormatException) {
            tvFeedbackBinario.text = "Por favor ingresa un número válido"
            tvFeedbackBinario.setTextColor(Color.RED)
        }
    }

    private fun checkDecimalRespuesta() {
        // Obtener respuesta de los interruptores
        val respuestaBinaria = StringBuilder()
        var valorCalculado = 0

        for (i in 0 until 8) {
            if (switches[i]?.isChecked == true) {
                respuestaBinaria.append("1")
                valorCalculado += bitValues[i]
            } else {
                respuestaBinaria.append("0")
            }
        }

        val respuestaString = respuestaBinaria.toString()
        val respuestaCorrecta = Integer.toBinaryString(valorDecimalActual).padStart(8, '0')

        if (valorCalculado == valorDecimalActual) {
            // Respuesta correcta
            puntuacionTotal++
            tvFeedbackDecimal.text = "¡Correcto! $valorDecimalActual = $respuestaString"
            tvFeedbackDecimal.setTextColor(Color.GREEN)
        } else {
            // Respuesta incorrecta
            tvFeedbackDecimal.text = "Incorrecto. $valorDecimalActual = $respuestaCorrecta"
            tvFeedbackDecimal.setTextColor(Color.RED)
        }

        // Actualizar progreso
        ejerciciosDecimalRestantes--
        ejerciciosCompletados++
        updateProgressDisplay()

        // Programar próximo ejercicio
        btnVerificarDecimal.isEnabled = false
        for (switch in switches) {
            switch?.isEnabled = false
        }

        Handler(Looper.getMainLooper()).postDelayed({
            btnVerificarDecimal.isEnabled = true
            for (switch in switches) {
                switch?.isEnabled = true
            }
            generarEjercicioDecimal()

            // Verificar si se completaron todos los ejercicios
            if (ejerciciosBinarioRestantes <= 0 && ejerciciosDecimalRestantes <= 0) {
                mostrarResultadosFinales()
            }
        }, 1500)
    }

    private fun mostrarResultadosFinales() {
        // Calcular porcentaje
        val porcentaje = (puntuacionTotal * 100) / ejerciciosTotales

        // Preparar mensaje según puntuación
        val mensaje = when {
            porcentaje >= 90 -> "¡Excelente! Has dominado las conversiones binarias."
            porcentaje >= 70 -> "¡Muy bien! Tienes un buen manejo de las conversiones."
            porcentaje >= 50 -> "¡Bien hecho! Sigue practicando para mejorar."
            else -> "Sigue practicando las conversiones binarias para mejorar."
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
            ejerciciosBinarioRestantes = 5
            ejerciciosDecimalRestantes = 5
            ejerciciosCompletados = 0

            // Actualizar UI
            updateProgressDisplay()

            // Regenerar ejercicios
            generarEjercicioBinario()
            generarEjercicioDecimal()

            // Habilitar controles
            etBinarioRespuesta.isEnabled = true
            btnVerificarBinario.isEnabled = true
            for (switch in switches) {
                switch?.isEnabled = true
            }
            btnVerificarDecimal.isEnabled = true

            // Cerrar diálogo
            dialog.dismiss()
        }

        dialog.show()
    }
}