// PracticeFragment.kt
package ovh.gabrielhuav.battlenaval_v2.sistemabinario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R
import java.util.Random

class PracticeFragment : Fragment() {

    private lateinit var tvDecimalQuestion: TextView
    private lateinit var tvDifficultyLevel: TextView
    private lateinit var tvCorrectCounter: TextView
    private lateinit var binaryInputContainer: LinearLayout
    private lateinit var btnSubmit: Button
    private lateinit var btnNextProblem: Button
    private lateinit var tvFeedback: TextView

    private var currentLevel = 1
    private val maxLevel = 5
    private var currentDecimalValue = 0
    private var correctAnswers = 0
    private var totalProblems = 0
    private lateinit var bits: Array<ToggleButton?>
    private val random = Random()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar layout
        val view = inflater.inflate(R.layout.fragment_practice, container, false)

        // Inicializar vistas
        tvDecimalQuestion = view.findViewById(R.id.tvDecimalQuestion)
        tvDifficultyLevel = view.findViewById(R.id.tvDifficultyLevel)
        tvCorrectCounter = view.findViewById(R.id.tvCorrectCounter)
        binaryInputContainer = view.findViewById(R.id.binaryInputContainer)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        btnNextProblem = view.findViewById(R.id.btnNextProblem)
        tvFeedback = view.findViewById(R.id.tvFeedback)

        // Configurar toggles de entrada binaria
        setupBinaryInputs()

        // Configurar listeners de botones
        btnSubmit.setOnClickListener { checkAnswer() }
        btnNextProblem.setOnClickListener { generateNewProblem() }

        // Inicializar primer problema
        updateLevelDisplay()
        generateNewProblem()
        updateScoreDisplay()

        return view
    }

    private fun setupBinaryInputs() {
        // Crear 8 toggle buttons para entrada binaria
        bits = arrayOfNulls(8)

        for (i in 0 until 8) {
            val toggleButton = ToggleButton(context)
            toggleButton.textOn = "1"
            toggleButton.textOff = "0"
            toggleButton.isChecked = false

            // Crear layout para bit
            val bitLayout = LinearLayout(context)
            bitLayout.orientation = LinearLayout.VERTICAL
            bitLayout.addView(toggleButton)

            // Agregar etiqueta de valor posicional
            val tvValue = TextView(context)
            tvValue.text = (1 shl (7 - i)).toString() // 128, 64, 32, etc.
            tvValue.textAlignment = View.TEXT_ALIGNMENT_CENTER
            bitLayout.addView(tvValue)

            // Agregar parámetros de layout
            val params = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
            )
            params.setMargins(4, 0, 4, 0)
            bitLayout.layoutParams = params

            // Agregar al contenedor
            binaryInputContainer.addView(bitLayout)

            // Almacenar referencia al toggle button
            bits[i] = toggleButton
        }
    }

    private fun generateNewProblem() {
        // Limpiar retroalimentación anterior
        tvFeedback.text = ""
        resetBinaryInput()

        // Generar problema basado en nivel actual
        val maxValue = getMaxValueForLevel(currentLevel)
        currentDecimalValue = random.nextInt(maxValue) + 1
        tvDecimalQuestion.text = currentDecimalValue.toString()

        // Habilitar botón de envío para nuevo problema
        btnSubmit.isEnabled = true
    }

    private fun getMaxValueForLevel(level: Int): Int {
        return when (level) {
            1 -> 15     // 4-bit numbers (max 15)
            2 -> 31     // 5-bit numbers (max 31)
            3 -> 63     // 6-bit numbers (max 63)
            4 -> 127    // 7-bit numbers (max 127)
            5 -> 255    // 8-bit numbers (max 255)
            else -> 15  // Default to level 1
        }
    }

    private fun checkAnswer() {
        totalProblems++

        // Obtener entrada binaria del usuario
        var userAnswer = 0
        for (i in 0 until 8) {
            if (bits[i]?.isChecked == true) {
                userAnswer += (1 shl (7 - i))
            }
        }

        // Verificar si es correcta
        val isCorrect = (userAnswer == currentDecimalValue)

        if (isCorrect) {
            correctAnswers++
            tvFeedback.text = "¡Correcto! 🎉"

            // Verificar si debe subir de nivel (cada 3 respuestas correctas)
            if (correctAnswers % 3 == 0 && currentLevel < maxLevel) {
                currentLevel++
                updateLevelDisplay()
                Toast.makeText(context, "¡Subiste al nivel $currentLevel!", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Calcular representación binaria correcta para retroalimentación
            var correctBinary = Integer.toBinaryString(currentDecimalValue)
            while (correctBinary.length < 8) {
                correctBinary = "0$correctBinary"
            }

            tvFeedback.text = "Incorrecto. La respuesta correcta es: $correctBinary"
        }

        updateScoreDisplay()

        // Deshabilitar botón de envío hasta siguiente problema
        btnSubmit.isEnabled = false
    }

    private fun resetBinaryInput() {
        for (bit in bits) {
            bit?.isChecked = false
        }
    }

    private fun updateLevelDisplay() {
        tvDifficultyLevel.text = "Nivel: $currentLevel de $maxLevel"
    }

    private fun updateScoreDisplay() {
        tvCorrectCounter.text = "Aciertos: $correctAnswers de $totalProblems"
    }
}