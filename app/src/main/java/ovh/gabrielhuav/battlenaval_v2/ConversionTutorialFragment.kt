package ovh.gabrielhuav.battlenaval_v2

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import kotlin.random.Random

class ConversionTutorialFragment : DialogFragment() {

    private lateinit var tvExampleContent: TextView
    private lateinit var tvTutorialTitle: TextView
    private lateinit var btnContinue: Button
    private var countDownTimer: CountDownTimer? = null
    private var timeRemaining = 10 // Tiempo en segundos que debe permanecer visible el tutorial

    companion object {
        private const val ARG_TYPE = "tutorial_type"

        // Tipos de tutorial
        const val TYPE_ASCII = 0
        const val TYPE_BINARY = 1

        fun newInstance(type: Int): ConversionTutorialFragment {
            val fragment = ConversionTutorialFragment()
            val args = Bundle()
            args.putInt(ARG_TYPE, type)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Usar estilo de diálogo fullscreen
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_conversion_tutorial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvExampleContent = view.findViewById(R.id.tvExampleContent)
        tvTutorialTitle = view.findViewById(R.id.tvTutorialTitle)
        btnContinue = view.findViewById(R.id.btnContinue)

        // Actualizar el texto del botón con el temporizador
        updateButtonText()

        // Determinar el tipo de tutorial y generar un ejemplo acorde
        val tutorialType = arguments?.getInt(ARG_TYPE) ?: TYPE_BINARY
        generateExample(tutorialType)

        // Iniciar el temporizador
        startCountdown()

        // Configurar el botón de continuar
        btnContinue.setOnClickListener {
            countDownTimer?.cancel()
            dismiss()
        }

        // Deshabilitar el botón al principio
        btnContinue.isEnabled = false
    }

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(timeRemaining * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = (millisUntilFinished / 1000).toInt()
                updateButtonText()
            }

            override fun onFinish() {
                timeRemaining = 0
                btnContinue.isEnabled = true
                btnContinue.text = "¡Entendido! Volver al Juego"
            }
        }.start()
    }

    private fun updateButtonText() {
        if (timeRemaining > 0) {
            btnContinue.text = "Espera $timeRemaining segundos..."
        } else {
            btnContinue.text = "¡Entendido! Volver al Juego"
            btnContinue.isEnabled = true
        }
    }

    private fun generateExample(type: Int) {
        when (type) {
            TYPE_ASCII -> {
                // Generar ejemplo de conversión ASCII
                tvTutorialTitle.text = "Conversión de Binario a ASCII (Letra)"

                // Elegir una letra aleatoria que NO sea A-G para que no coincida con el juego
                val randomLetter = ('H'..'Z').random()
                val asciiValue = randomLetter.code
                val binaryValue = Integer.toBinaryString(asciiValue).padStart(8, '0')

                val example = """
                    Ejemplo: Convertir el código binario "$binaryValue" a una letra.
                    
                    Paso 1: Calcular el valor decimal de cada posición binaria
                    $binaryValue
                    ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓
                    ${if (binaryValue[0] == '1') "128" else "0"} + ${if (binaryValue[1] == '1') "64" else "0"} + ${if (binaryValue[2] == '1') "32" else "0"} + ${if (binaryValue[3] == '1') "16" else "0"} + ${if (binaryValue[4] == '1') "8" else "0"} + ${if (binaryValue[5] == '1') "4" else "0"} + ${if (binaryValue[6] == '1') "2" else "0"} + ${if (binaryValue[7] == '1') "1" else "0"}
                    
                    Paso 2: Sumar todos los valores
                    = $asciiValue
                    
                    Paso 3: Buscar qué carácter ASCII corresponde al valor $asciiValue
                    = '$randomLetter'
                    
                    Por lo tanto, $binaryValue en binario representa la letra '$randomLetter' en ASCII.
                """.trimIndent()

                tvExampleContent.text = example
            }
            TYPE_BINARY -> {
                // Generar ejemplo de conversión binaria a decimal
                tvTutorialTitle.text = "Conversión de Binario a Decimal (Número)"

                // Elegir un número aleatorio que NO sea 1-7 para que no coincida con el juego
                val randomNum = (8..15).random()
                val binaryValue = Integer.toBinaryString(randomNum).padStart(4, '0')

                val example = """
                    Ejemplo: Convertir el código binario "$binaryValue" a un número decimal.
                    
                    Paso 1: Identificar el valor de cada posición binaria
                    $binaryValue
                    ↓ ↓ ↓ ↓
                    ${if (binaryValue[0] == '1') "8" else "0"} + ${if (binaryValue[1] == '1') "4" else "0"} + ${if (binaryValue[2] == '1') "2" else "0"} + ${if (binaryValue[3] == '1') "1" else "0"}
                    
                    Paso 2: Sumar todos los valores
                    = $randomNum
                    
                    Por lo tanto, $binaryValue en binario representa el número $randomNum en decimal.
                """.trimIndent()

                tvExampleContent.text = example
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}