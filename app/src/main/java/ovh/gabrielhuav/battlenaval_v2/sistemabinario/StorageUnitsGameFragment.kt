package ovh.gabrielhuav.battlenaval_v2.unidadesalmacenamiento

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R
import kotlin.math.abs
import kotlin.math.round
import kotlin.random.Random

/**
 * Fragmento que implementa un juego educativo para evaluar el conocimiento
 * sobre unidades de medida de almacenamiento.
 */
class StorageUnitsGameFragment : Fragment() {

    // Componentes UI principales
    private lateinit var tvScoreDisplay: TextView
    private lateinit var tvProgress: TextView
    private lateinit var progressBar: ProgressBar

    // Sección de conversión de unidades
    private lateinit var conversionCard: LinearLayout
    private lateinit var tvConversionQuestion: TextView
    private lateinit var etConversionAnswer: EditText
    private lateinit var btnCheckConversion: Button
    private lateinit var tvFeedbackConversion: TextView
    private lateinit var unitsRadioGroup: RadioGroup
    private lateinit var rbBytes: RadioButton
    private lateinit var rbKilobytes: RadioButton
    private lateinit var rbMegabytes: RadioButton
    private lateinit var rbGigabytes: RadioButton

    // Sección de relacionar columnas
    private lateinit var matchingCard: LinearLayout
    private lateinit var leftContainer: LinearLayout
    private lateinit var rightContainer: LinearLayout
    private lateinit var btnCheckMatching: Button
    private lateinit var tvFeedbackMatching: TextView

    // Sección de problemas prácticos
    private lateinit var practicalCard: LinearLayout
    private lateinit var tvPracticalQuestion: TextView
    private lateinit var etPracticalAnswer: EditText
    private lateinit var btnCheckPractical: Button
    private lateinit var tvFeedbackPractical: TextView

    // Controles generales
    private lateinit var btnNextQuestion: Button

    // Variables del juego
    private var currentScore = 0
    private var totalQuestions = 10
    private var questionsRemaining = 10
    private var questionsCompleted = 0
    private var currentQuestionType = QuestionType.CONVERSION

    // Variables para el juego de relacionar
    private var selectedLeftItemIndex = -1
    private var selectedRightItemIndex = -1
    private var matchedPairs = mutableMapOf<Int, Int>()
    private var leftItems = mutableListOf<MatchItem>()
    private var rightItems = mutableListOf<MatchItem>()

    // Tipo de pregunta actual
    private enum class QuestionType {
        CONVERSION, MATCHING, PRACTICAL
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_storage_units_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar componentes de UI
        initializeViews(view)

        // Configurar listeners
        setupListeners()

        // Iniciar juego
        updateProgressDisplay()
        generateQuestion()
    }

    private fun initializeViews(view: View) {
        // Indicadores de progreso
        tvScoreDisplay = view.findViewById(R.id.tvScoreDisplay)
        tvProgress = view.findViewById(R.id.tvProgress)
        progressBar = view.findViewById(R.id.progressBar)

        // Sección de conversión
        conversionCard = view.findViewById(R.id.conversionCard)
        tvConversionQuestion = view.findViewById(R.id.tvConversionQuestion)
        etConversionAnswer = view.findViewById(R.id.etConversionAnswer)
        btnCheckConversion = view.findViewById(R.id.btnCheckConversion)
        tvFeedbackConversion = view.findViewById(R.id.tvFeedbackConversion)
        unitsRadioGroup = view.findViewById(R.id.unitsRadioGroup)
        rbBytes = view.findViewById(R.id.rbBytes)
        rbKilobytes = view.findViewById(R.id.rbKilobytes)
        rbMegabytes = view.findViewById(R.id.rbMegabytes)
        rbGigabytes = view.findViewById(R.id.rbGigabytes)

        // Sección de relacionar columnas
        matchingCard = view.findViewById(R.id.matchingCard)
        leftContainer = view.findViewById(R.id.leftContainer)
        rightContainer = view.findViewById(R.id.rightContainer)
        btnCheckMatching = view.findViewById(R.id.btnCheckMatching)
        tvFeedbackMatching = view.findViewById(R.id.tvFeedbackMatching)

        // Sección de problemas prácticos
        practicalCard = view.findViewById(R.id.practicalCard)
        tvPracticalQuestion = view.findViewById(R.id.tvPracticalQuestion)
        etPracticalAnswer = view.findViewById(R.id.etPracticalAnswer)
        btnCheckPractical = view.findViewById(R.id.btnCheckPractical)
        tvFeedbackPractical = view.findViewById(R.id.tvFeedbackPractical)

        // Controles generales
        btnNextQuestion = view.findViewById(R.id.btnNextQuestion)

        // Configurar tipo de entrada numérica para respuestas
        etConversionAnswer.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        etPracticalAnswer.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    }

    private fun setupListeners() {
        // Botón para verificar respuesta de conversión
        btnCheckConversion.setOnClickListener {
            checkConversionAnswer()
        }

        // Botón para verificar emparejamientos
        btnCheckMatching.setOnClickListener {
            checkMatchingAnswer()
        }

        // Botón para verificar respuesta de caso práctico
        btnCheckPractical.setOnClickListener {
            checkPracticalAnswer()
        }

        // Botón para siguiente pregunta
        btnNextQuestion.setOnClickListener {
            nextQuestion()
        }
    }

    private fun updateProgressDisplay() {
        tvScoreDisplay.text = "Puntuación: $currentScore / $totalQuestions"
        tvProgress.text = "Ejercicios restantes: $questionsRemaining de $totalQuestions"

        // Actualizar barra de progreso
        val progress = ((totalQuestions - questionsRemaining) * 100) / totalQuestions
        progressBar.progress = progress
    }

    private fun generateQuestion() {
        // Ocultar todos los tipos de preguntas
        conversionCard.visibility = View.GONE
        matchingCard.visibility = View.GONE
        practicalCard.visibility = View.GONE

        // Determinar qué tipo de pregunta generar basado en la secuencia y algo de aleatoriedad
        currentQuestionType = when {
            questionsCompleted % 3 == 0 -> QuestionType.CONVERSION
            questionsCompleted % 3 == 1 -> QuestionType.MATCHING
            else -> QuestionType.PRACTICAL
        }

        // Generar la pregunta apropiada
        when (currentQuestionType) {
            QuestionType.CONVERSION -> generateConversionQuestion()
            QuestionType.MATCHING -> generateMatchingQuestion()
            QuestionType.PRACTICAL -> generatePracticalQuestion()
        }

        // Desactivar el botón de siguiente pregunta
        btnNextQuestion.isEnabled = false
        btnNextQuestion.visibility = View.GONE
    }

    private fun generateConversionQuestion() {
        // Mostrar la tarjeta de conversión
        conversionCard.visibility = View.VISIBLE

        // Limpiar campos anteriores
        etConversionAnswer.text.clear()
        tvFeedbackConversion.text = ""

        // Habilitar el botón de verificación
        btnCheckConversion.isEnabled = true

        // Configurar la dificultad según el progreso
        val difficulty = when {
            questionsCompleted < 5 -> 1
            questionsCompleted < 10 -> 2
            else -> 3
        }

        // Generar pregunta basada en la dificultad
        val (value, fromUnit, toUnit, correctAnswer) = when (difficulty) {
            1 -> generateEasyConversionQuestion()
            2 -> generateMediumConversionQuestion()
            else -> generateHardConversionQuestion()
        }

        // Construir y mostrar la pregunta
        tvConversionQuestion.text = "Convierte $value $fromUnit a $toUnit:"

        // Guardar datos para la verificación
        etConversionAnswer.tag = correctAnswer

        // Preseleccionar la unidad adecuada
        val radioButtons = (0 until unitsRadioGroup.childCount)
            .map { unitsRadioGroup.getChildAt(it) }
            .filterIsInstance<RadioButton>()

        for (rb in radioButtons) {
            rb.isChecked = rb.text.toString().contains(toUnit, ignoreCase = true)
        }
    }

    private fun generateEasyConversionQuestion(): ConversionQuestion {
        // Conversiones simples entre unidades adyacentes (KB a MB, B a KB) con números redondos
        val questions = listOf(
            ConversionQuestion(1024, "bytes", "kilobytes", 1.0),
            ConversionQuestion(2048, "bytes", "kilobytes", 2.0),
            ConversionQuestion(5, "kilobytes", "bytes", 5120.0),
            ConversionQuestion(10, "kilobytes", "bytes", 10240.0),
            ConversionQuestion(1024, "kilobytes", "megabytes", 1.0),
            ConversionQuestion(2048, "kilobytes", "megabytes", 2.0),
            ConversionQuestion(5, "megabytes", "kilobytes", 5120.0)
        )
        return questions.random()
    }

    private fun generateMediumConversionQuestion(): ConversionQuestion {
        // Conversiones más simples con números redondos
        val questions = listOf(
            ConversionQuestion(5, "megabytes", "bytes", 5242880.0),
            ConversionQuestion(2, "megabytes", "bytes", 2097152.0),
            ConversionQuestion(1, "gigabytes", "megabytes", 1024.0),
            ConversionQuestion(2, "gigabytes", "megabytes", 2048.0),
            ConversionQuestion(4, "megabytes", "gigabytes", 0.00390625)
        )
        return questions.random()
    }

    private fun generateHardConversionQuestion(): ConversionQuestion {
        // Conversiones simplificadas pero que siguen requiriendo comprensión del tema
        val questions = listOf(
            ConversionQuestion(0.5, "gigabytes", "megabytes", 512.0),
            ConversionQuestion(2000, "kilobytes", "megabytes", 1.953125),
            ConversionQuestion(1, "terabyte", "gigabytes", 1024.0),
            ConversionQuestion(0.25, "terabytes", "gigabytes", 256.0),
            ConversionQuestion(1024, "bytes", "kilobytes", 1.0)
        )
        return questions.random()
    }

    private fun generateMatchingQuestion() {
        // Mostrar la tarjeta de relacionar
        matchingCard.visibility = View.VISIBLE

        // Limpiar feedback
        tvFeedbackMatching.text = ""

        // Limpiar selecciones previas
        selectedLeftItemIndex = -1
        selectedRightItemIndex = -1
        matchedPairs.clear()

        // Limpiar contenedores
        leftContainer.removeAllViews()
        rightContainer.removeAllViews()

        // Diferentes conjuntos de preguntas para variar
        val questionSets = listOf(
            // Conjunto 1: Relacionar tipos de medios con su tamaño típico
            Pair(
                listOf(
                    MatchItem("Una página de texto", 0),
                    MatchItem("Una foto de WhatsApp", 1),
                    MatchItem("Una canción MP3", 2),
                    MatchItem("Un video de 10 min en HD", 3),
                    MatchItem("Una película en HD", 4)
                ),
                listOf(
                    MatchItem("2-5 KB", 0),
                    MatchItem("100-300 KB", 1),
                    MatchItem("3-5 MB", 2),
                    MatchItem("150-300 MB", 3),
                    MatchItem("2-4 GB", 4)
                )
            ),

            // Conjunto 2: Relacionar dispositivos con su capacidad típica
            Pair(
                listOf(
                    MatchItem("Mensaje de texto", 0),
                    MatchItem("Memoria USB básica", 1),
                    MatchItem("SSD de laptop estándar", 2),
                    MatchItem("Disco duro externo común", 3),
                    MatchItem("Servidor de almacenamiento", 4)
                ),
                listOf(
                    MatchItem("1-2 KB", 0),
                    MatchItem("16-64 GB", 1),
                    MatchItem("256-512 GB", 2),
                    MatchItem("1-2 TB", 3),
                    MatchItem("8-16 TB", 4)
                )
            ),

            // Conjunto 3: Relacionar magnitudes con lo que miden
            Pair(
                listOf(
                    MatchItem("Tamaño de información", 0),
                    MatchItem("Distancia", 1),
                    MatchItem("Peso", 2),
                    MatchItem("Tiempo", 3),
                    MatchItem("Volumen", 4)
                ),
                listOf(
                    MatchItem("Bytes, KB, MB, GB", 0),
                    MatchItem("mm, cm, m, km", 1),
                    MatchItem("mg, g, kg, ton", 2),
                    MatchItem("seg, min, hora, día", 3),
                    MatchItem("ml, l, galón", 4)
                )
            ),

            // Conjunto 4: Equivalencias entre unidades
            Pair(
                listOf(
                    MatchItem("1 Kilobyte", 0),
                    MatchItem("1 Megabyte", 1),
                    MatchItem("1 Gigabyte", 2),
                    MatchItem("1 Terabyte", 3)
                ),
                listOf(
                    MatchItem("1024 bytes", 0),
                    MatchItem("1024 KB", 1),
                    MatchItem("1024 MB", 2),
                    MatchItem("1024 GB", 3)
                )
            )
        )

        // Seleccionar un conjunto aleatorio
        val selectedSet = questionSets.random()
        leftItems = selectedSet.first.toMutableList()
        rightItems = selectedSet.second.toMutableList()

        // Opcional: barajar el orden de las listas para mayor dificultad
        leftItems.shuffle()
        rightItems.shuffle()

        // Crear las tarjetas visuales para cada elemento
        for ((index, item) in leftItems.withIndex()) {
            leftContainer.addView(createMatchCard(item.text, index, true))
        }

        for ((index, item) in rightItems.withIndex()) {
            rightContainer.addView(createMatchCard(item.text, index, false))
        }
    }

    private fun createMatchCard(text: String, index: Int, isLeft: Boolean): CardView {
        // Crear tarjeta para cada elemento
        val card = CardView(requireContext()).apply {
            radius = resources.getDimension(R.dimen.card_corner_radius)
            cardElevation = resources.getDimension(R.dimen.card_elevation)
            useCompatPadding = true

            // Guardar datos para identificar la tarjeta
            tag = if (isLeft) "L$index" else "R$index"

            // Configurar la apariencia
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            this.layoutParams = layoutParams

            // Al hacer clic, seleccionar esta tarjeta
            setOnClickListener {
                if (isLeft) {
                    selectLeftItem(index, this)
                } else {
                    selectRightItem(index, this)
                }
            }
        }

        // Agregar el texto dentro de la tarjeta
        val textView = TextView(requireContext()).apply {
            this.text = text
            setPadding(24, 16, 24, 16)
            textSize = 16f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        card.addView(textView)
        return card
    }

    private fun selectLeftItem(index: Int, card: CardView) {
        // Ignorar si ya está emparejado
        if (matchedPairs.containsKey(index)) {
            return
        }

        // Deseleccionar la anterior selección
        if (selectedLeftItemIndex != -1) {
            val previousCard = findCardByTag("L$selectedLeftItemIndex", leftContainer)
            previousCard?.setCardBackgroundColor(Color.WHITE)
        }

        // Seleccionar esta
        selectedLeftItemIndex = index
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200))

        // Intentar crear un emparejamiento si hay una selección en el lado derecho
        tryCreateMatch()
    }

    private fun selectRightItem(index: Int, card: CardView) {
        // Ignorar si ya está emparejado
        if (matchedPairs.containsValue(index)) {
            return
        }

        // Deseleccionar la anterior selección
        if (selectedRightItemIndex != -1) {
            val previousCard = findCardByTag("R$selectedRightItemIndex", rightContainer)
            previousCard?.setCardBackgroundColor(Color.WHITE)
        }

        // Seleccionar esta
        selectedRightItemIndex = index
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200))

        // Intentar crear un emparejamiento si hay una selección en el lado izquierdo
        tryCreateMatch()
    }

    private fun tryCreateMatch() {
        // Si hay selecciones en ambos lados, crear un emparejamiento temporal
        if (selectedLeftItemIndex != -1 && selectedRightItemIndex != -1) {
            // Actualizar feedback con la selección actual
            tvFeedbackMatching.text = "Relacionando \"${leftItems[selectedLeftItemIndex].text}\" con \"${rightItems[selectedRightItemIndex].text}\""

            // Activar el botón de verificar
            btnCheckMatching.isEnabled = true
        }
    }

    private fun findCardByTag(tag: String, container: ViewGroup): CardView? {
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is CardView && child.tag == tag) {
                return child
            }
        }
        return null
    }

    private fun generatePracticalQuestion() {
        // Mostrar la tarjeta de casos prácticos
        practicalCard.visibility = View.VISIBLE

        // Limpiar campos
        etPracticalAnswer.text.clear()
        tvFeedbackPractical.text = ""

        // Habilitar botón de verificación
        btnCheckPractical.isEnabled = true

        // Diferentes preguntas de casos prácticos (simplificadas)
        val questions = listOf(
            PracticalQuestion(
                "Una canción en formato MP3 ocupa aproximadamente 4 MB. ¿Cuántas canciones puedes almacenar en 16 MB?",
                4.0
            ),
            PracticalQuestion(
                "Un video de 1 minuto ocupa aproximadamente 20 MB. ¿Cuántos minutos de video puedes almacenar en 100 MB?",
                5.0
            ),
            PracticalQuestion(
                "Una foto ocupa aproximadamente 2 MB. Si tienes 10 fotos, ¿cuánto espacio ocuparán en MB?",
                20.0
            ),
            PracticalQuestion(
                "Un archivo PDF ocupa aproximadamente 5 MB. ¿Cuántos archivos puedes almacenar en 50 MB?",
                10.0
            ),
            PracticalQuestion(
                "Un juego sencillo ocupa aproximadamente 100 MB. Si tienes 1 GB de espacio libre, ¿cuántos juegos puedes instalar?",
                10.0
            ),
            PracticalQuestion(
                "Si una página de texto ocupa aproximadamente 2 KB, ¿cuántas páginas puedes almacenar en 10 KB?",
                5.0
            ),
            PracticalQuestion(
                "Un documento de texto ocupa 3 MB. Si tienes 5 documentos iguales, ¿cuánto espacio ocupan en total en MB?",
                15.0
            ),
            PracticalQuestion(
                "Si 1 KB equivale a 1024 bytes, ¿cuántos KB son 2048 bytes?",
                2.0
            ),
            PracticalQuestion(
                "Si 1 MB equivale a 1024 KB, ¿cuántos MB son 2048 KB?",
                2.0
            ),
            PracticalQuestion(
                "Si 1 GB equivale a 1024 MB, ¿cuántos GB son 2048 MB?",
                2.0
            )
        )

        // Seleccionar una pregunta aleatoria
        val selectedQuestion = questions.random()
        tvPracticalQuestion.text = selectedQuestion.question

        // Guardar respuesta correcta para verificación
        etPracticalAnswer.tag = selectedQuestion.answer
    }

    private fun checkConversionAnswer() {
        val answerText = etConversionAnswer.text.toString().trim()

        if (answerText.isEmpty()) {
            tvFeedbackConversion.text = "Por favor, ingresa un valor"
            tvFeedbackConversion.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            return
        }

        try {
            val userAnswer = answerText.toDouble()
            val correctAnswer = etConversionAnswer.tag as Double

            // Verificar con un margen de error del 1% para números grandes
            val isCorrect = if (correctAnswer > 1000) {
                val percentError = abs((userAnswer - correctAnswer) / correctAnswer) * 100
                percentError <= 1
            } else {
                // Para números pequeños, comparar con un margen fijo
                abs(userAnswer - correctAnswer) < 0.1
            }

            if (isCorrect) {
                handleCorrectAnswer(tvFeedbackConversion)
            } else {
                tvFeedbackConversion.text = "Incorrecto. La respuesta correcta es: $correctAnswer"
                tvFeedbackConversion.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            }

            // Desactivar el botón para evitar múltiples intentos
            btnCheckConversion.isEnabled = false

            // Mostrar botón para continuar
            showNextButton()

        } catch (e: NumberFormatException) {
            tvFeedbackConversion.text = "Por favor, ingresa un número válido"
            tvFeedbackConversion.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
        }
    }

    private fun checkMatchingAnswer() {
        // Verificar si las selecciones son correctas (los índices deben coincidir)
        val leftItem = leftItems[selectedLeftItemIndex]
        val rightItem = rightItems[selectedRightItemIndex]

        val isCorrect = leftItem.matchIndex == rightItem.matchIndex

        if (isCorrect) {
            // Marcar como emparejado correctamente
            matchedPairs[selectedLeftItemIndex] = selectedRightItemIndex

            // Cambiar color a verde para indicar emparejamiento correcto
            val leftCard = findCardByTag("L$selectedLeftItemIndex", leftContainer)
            val rightCard = findCardByTag("R$selectedRightItemIndex", rightContainer)

            leftCard?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
            rightCard?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))

            // Desactivar estas tarjetas
            leftCard?.isClickable = false
            rightCard?.isClickable = false

            // Actualizar feedback
            tvFeedbackMatching.text = "¡Correcto! Has emparejado correctamente"
            tvFeedbackMatching.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))

            // Actualizar puntuación
            currentScore++
            updateProgressDisplay()

            // Resetear selecciones actuales
            selectedLeftItemIndex = -1
            selectedRightItemIndex = -1

            // Verificar si se completaron todos los emparejamientos
            if (matchedPairs.size == leftItems.size) {
                // Todas las parejas están completas - mostrar botón para continuar
                showNextButton()
            }
        } else {
            // Emparejamiento incorrecto
            tvFeedbackMatching.text = "Incorrecto. Intenta otra combinación"
            tvFeedbackMatching.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))

            // Resetear colores a blanco
            val leftCard = findCardByTag("L$selectedLeftItemIndex", leftContainer)
            val rightCard = findCardByTag("R$selectedRightItemIndex", rightContainer)

            leftCard?.setCardBackgroundColor(Color.WHITE)
            rightCard?.setCardBackgroundColor(Color.WHITE)

            // Resetear selecciones
            selectedLeftItemIndex = -1
            selectedRightItemIndex = -1
        }

        // Desactivar botón de verificar hasta nueva selección
        btnCheckMatching.isEnabled = false
    }

    private fun checkPracticalAnswer() {
        val answerText = etPracticalAnswer.text.toString().trim()

        if (answerText.isEmpty()) {
            tvFeedbackPractical.text = "Por favor, ingresa un valor"
            tvFeedbackPractical.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            return
        }

        try {
            val userAnswer = answerText.toDouble()
            val correctAnswer = etPracticalAnswer.tag as Double

            // Verificar con un margen de error más amplio (10%) para estos problemas prácticos simplificados
            val percentError = abs((userAnswer - correctAnswer) / correctAnswer) * 100
            val isCorrect = percentError <= 10

            if (isCorrect) {
                handleCorrectAnswer(tvFeedbackPractical)
            } else {
                tvFeedbackPractical.text = "Incorrecto. La respuesta correcta es aproximadamente: $correctAnswer"
                tvFeedbackPractical.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            }

            // Desactivar el botón para evitar múltiples intentos
            btnCheckPractical.isEnabled = false

            // Mostrar botón para continuar
            showNextButton()

        } catch (e: NumberFormatException) {
            tvFeedbackPractical.text = "Por favor, ingresa un número válido"
            tvFeedbackPractical.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
        }
    }

    private fun handleCorrectAnswer(feedbackTextView: TextView) {
        // Incrementar puntuación
        currentScore++

        // Mostrar feedback positivo
        feedbackTextView.text = "¡Correcto!"
        feedbackTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))

        // Actualizar progreso
        updateProgressDisplay()
    }

    private fun showNextButton() {
        // Actualizar progreso
        questionsRemaining--
        questionsCompleted++
        updateProgressDisplay()

        // Mostrar botón para siguiente pregunta
        btnNextQuestion.isEnabled = true
        btnNextQuestion.visibility = View.VISIBLE

        // Si ya no quedan preguntas, mostrar resultados finales
        if (questionsRemaining <= 0) {
            showFinalResults()
        }
    }

    private fun nextQuestion() {
        if (questionsRemaining > 0) {
            // Generar siguiente pregunta
            generateQuestion()
        } else {
            // Mostrar resultados finales
            showFinalResults()
        }
    }

    private fun showFinalResults() {
        // Calcular porcentaje
        val percentScore = (currentScore * 100) / totalQuestions

        // Preparar mensaje según puntuación
        val message = when {
            percentScore >= 90 -> "¡Excelente! Dominas las unidades de almacenamiento"
            percentScore >= 75 -> "¡Muy bien! Tienes buen conocimiento de las unidades de almacenamiento"
            percentScore >= 60 -> "¡Bien! Has aprendido lo básico sobre unidades de almacenamiento"
            else -> "Has completado el ejercicio. Sigue practicando para mejorar"
        }

        // Crear diálogo con resultados
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_results, null)

        val tvResultadoFinal = dialogView.findViewById<TextView>(R.id.tvResultadoFinal)
        val tvMensajeFinal = dialogView.findViewById<TextView>(R.id.tvMensajeFinal)
        val btnReiniciar = dialogView.findViewById<Button>(R.id.btnReiniciar)

        tvResultadoFinal.text = "Puntuación final: $currentScore de $totalQuestions ($percentScore%)"
        tvMensajeFinal.text = message

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnReiniciar.setOnClickListener {
            // Reiniciar juego
            currentScore = 0
            questionsRemaining = totalQuestions
            questionsCompleted = 0

            // Actualizar UI
            updateProgressDisplay()

            // Generar nueva pregunta
            generateQuestion()

            // Cerrar diálogo
            dialog.dismiss()
        }

        dialog.show()
    }
}

/**
 * Clase para preguntas de conversión de unidades
 */
data class ConversionQuestion(
    val value: Number, // Valor a convertir (puede ser Int o Double)
    val fromUnit: String, // Unidad de origen
    val toUnit: String, // Unidad de destino
    val answer: Double // Respuesta correcta
)

/**
 * Clase para preguntas de relacionar elementos
 */
data class EquivalenceQuestion(
    val question: String, // Pregunta o instrucción
    val options: List<String>, // Opciones a mostrar
    val correctIndex: Int // Índice de la opción correcta
)

/**
 * Clase para elementos de emparejamiento
 */
data class MatchItem(
    val text: String, // Texto a mostrar
    val matchIndex: Int // Índice para emparejamiento correcto
)

/**
 * Clase para preguntas de casos prácticos
 */
data class PracticalQuestion(
    val question: String, // Pregunta del caso práctico
    val answer: Double // Respuesta correcta
)