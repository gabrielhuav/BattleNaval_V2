package ovh.gabrielhuav.battlenaval_v2.sistemabinario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R
import kotlin.random.Random

class DecimalToBinaryFragment : Fragment() {

    private lateinit var tvNumber: TextView
    private lateinit var switchContainer: LinearLayout
    private lateinit var binaryBits: Array<TextView>
    private lateinit var btnGenerate: Button
    private lateinit var btnCheck: Button
    private lateinit var tvFeedback: TextView
    private lateinit var tvExplanation: TextView

    private var currentNumber = 0
    private var maxValue = 15 // Número máximo (4 bits)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_decimal_to_binary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar vistas
        tvNumber = view.findViewById(R.id.tvNumber)
        switchContainer = view.findViewById(R.id.switchContainer)
        btnGenerate = view.findViewById(R.id.btnGenerate)
        btnCheck = view.findViewById(R.id.btnCheck)
        tvFeedback = view.findViewById(R.id.tvFeedback)
        tvExplanation = view.findViewById(R.id.tvExplanation)

        // Crear los "bits" para la conversión (son TextView que cambian al hacer clic)
        createBinaryBits()

        // Generar número inicial
        generateRandomNumber()

        // Configurar listeners
        btnGenerate.setOnClickListener {
            generateRandomNumber()
        }

        btnCheck.setOnClickListener {
            checkAnswer()
        }
    }

    private fun createBinaryBits() {
        // Crear 4 bits (para números hasta 15)
        binaryBits = Array(4) { index ->
            TextView(requireContext()).apply {
                text = "0"
                textSize = 24f
                setPadding(16, 16, 16, 16)
                setBackgroundResource(R.drawable.binary_bit_background)
                setOnClickListener {
                    // Cambiar entre 0 y 1 al hacer clic
                    text = if (text == "0") "1" else "0"

                    // Cambiar color según el valor
                    setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            if (text == "1") R.color.green else R.color.red
                        )
                    )
                }

                // Color inicial (rojo para 0)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            }
        }

        // Añadir etiquetas de valor para cada bit
        val bitValues = arrayOf(8, 4, 2, 1)

        for (i in binaryBits.indices) {
            val bitLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL

                // Añadir el bit (TextView que cambia)
                addView(binaryBits[i])

                // Añadir etiqueta de valor
                addView(TextView(context).apply {
                    text = bitValues[i].toString()
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                })
            }

            // Parámetros para que todos los bits tengan el mismo ancho
            val params = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            params.setMargins(8, 0, 8, 0)
            bitLayout.layoutParams = params

            // Añadir al contenedor
            switchContainer.addView(bitLayout)
        }
    }

    private fun generateRandomNumber() {
        currentNumber = Random.nextInt(maxValue + 1) // 0 a 15
        tvNumber.text = currentNumber.toString()

        // Resetear los bits a 0
        for (bit in binaryBits) {
            bit.text = "0"
            bit.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
        }

        // Limpiar feedback
        tvFeedback.text = ""
        tvExplanation.text = ""
    }

    private fun checkAnswer() {
        // Obtener la respuesta del usuario
        var userAnswer = 0
        val userBinary = StringBuilder()

        for (i in binaryBits.indices) {
            val bitValue = if (binaryBits[i].text == "1") 1 else 0
            userBinary.append(bitValue)

            // Calcular el valor decimal según la posición
            val positionValue = when(i) {
                0 -> 8  // 2^3
                1 -> 4  // 2^2
                2 -> 2  // 2^1
                3 -> 1  // 2^0
                else -> 0
            }

            userAnswer += bitValue * positionValue
        }

        // Verificar si la respuesta es correcta
        if (userAnswer == currentNumber) {
            tvFeedback.text = "¡Correcto! $userBinary = $currentNumber"
            tvFeedback.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))

            // Explicar la conversión
            explainConversion()
        } else {
            tvFeedback.text = "Incorrecto. Intenta de nuevo."
            tvFeedback.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            tvExplanation.text = ""
        }
    }

    private fun explainConversion() {
        val binaryString = StringBuilder()
        val explanation = StringBuilder()

        explanation.append("Explicación:\n\n")

        // Agregar los bits y sus valores
        var hasAddedValue = false

        for (i in binaryBits.indices) {
            val bit = binaryBits[i].text.toString()
            binaryString.append(bit)

            val positionValue = when(i) {
                0 -> 8  // 2^3
                1 -> 4  // 2^2
                2 -> 2  // 2^1
                3 -> 1  // 2^0
                else -> 0
            }

            if (bit == "1") {
                if (hasAddedValue) {
                    explanation.append(" + ")
                }
                explanation.append("$positionValue")
                hasAddedValue = true
            }
        }

        explanation.append(" = $currentNumber")
        explanation.append("\n\nPara convertir $currentNumber a binario:")
        explanation.append("\n1. ¿$currentNumber contiene 8? ")

        if (currentNumber >= 8) {
            explanation.append("Sí, ponemos 1 en posición de 8")
            explanation.append("\n   $currentNumber - 8 = ${currentNumber - 8} (nos queda por convertir)")
        } else {
            explanation.append("No, ponemos 0 en posición de 8")
        }

        var remainder = if (currentNumber >= 8) currentNumber - 8 else currentNumber

        explanation.append("\n2. ¿$remainder contiene 4? ")
        if (remainder >= 4) {
            explanation.append("Sí, ponemos 1 en posición de 4")
            explanation.append("\n   $remainder - 4 = ${remainder - 4} (nos queda por convertir)")
            remainder -= 4
        } else {
            explanation.append("No, ponemos 0 en posición de 4")
        }

        explanation.append("\n3. ¿$remainder contiene 2? ")
        if (remainder >= 2) {
            explanation.append("Sí, ponemos 1 en posición de 2")
            explanation.append("\n   $remainder - 2 = ${remainder - 2} (nos queda por convertir)")
            remainder -= 2
        } else {
            explanation.append("No, ponemos 0 en posición de 2")
        }

        explanation.append("\n4. ¿$remainder contiene 1? ")
        if (remainder >= 1) {
            explanation.append("Sí, ponemos 1 en posición de 1")
        } else {
            explanation.append("No, ponemos 0 en posición de 1")
        }

        tvExplanation.text = explanation.toString()
    }
}