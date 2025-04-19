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
            tvExplanation.text = "" // No mostrar explicación si es correcto
        } else {
            tvFeedback.text = "Incorrecto. Intenta de nuevo."
            tvFeedback.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            // Mostrar explicación solo si es incorrecto
            explainConversion()
        }
    }

    private fun explainConversion() {
        val explanation = StringBuilder()
        explanation.append("Cómo convertir $currentNumber a binario:\n\n")

        // Convertir usando el método de potencias de 2
        var remainder = currentNumber
        val binaryResult = arrayOf(0, 0, 0, 0) // Para 8, 4, 2, 1

        // Verificar cada potencia de 2
        if (remainder >= 8) {
            binaryResult[0] = 1
            remainder -= 8
            explanation.append("• $currentNumber contiene 8 (2³), ponemos 1 en la posición de 8\n")
            explanation.append("  $currentNumber - 8 = $remainder (nos queda por convertir)\n")
        } else {
            explanation.append("• $currentNumber no contiene 8 (2³), ponemos 0 en la posición de 8\n")
        }

        if (remainder >= 4) {
            binaryResult[1] = 1
            remainder -= 4
            explanation.append("• $remainder contiene 4 (2²), ponemos 1 en la posición de 4\n")
            explanation.append("  $remainder - 4 = ${remainder - 4} (nos queda por convertir)\n")
        } else {
            explanation.append("• $remainder no contiene 4 (2²), ponemos 0 en la posición de 4\n")
        }

        if (remainder >= 2) {
            binaryResult[2] = 1
            remainder -= 2
            explanation.append("• $remainder contiene 2 (2¹), ponemos 1 en la posición de 2\n")
            explanation.append("  $remainder - 2 = ${remainder - 2} (nos queda por convertir)\n")
        } else {
            explanation.append("• $remainder no contiene 2 (2¹), ponemos 0 en la posición de 2\n")
        }

        if (remainder >= 1) {
            binaryResult[3] = 1
            explanation.append("• $remainder contiene 1 (2⁰), ponemos 1 en la posición de 1\n")
        } else {
            explanation.append("• $remainder no contiene 1 (2⁰), ponemos 0 en la posición de 1\n")
        }

        explanation.append("\nResultado binario: ${binaryResult.joinToString("")}")
        tvExplanation.text = explanation.toString()
    }
}