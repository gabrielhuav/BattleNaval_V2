package ovh.gabrielhuav.battlenaval_v2.unidadesalmacenamiento

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R
import java.util.*

class StorageUnitsInteractiveFragment : Fragment() {

    // Elementos de la interfaz
    private lateinit var titleTextView: TextView
    private lateinit var instructionsTextView: TextView
    private lateinit var scoreTextView: TextView
    private lateinit var checkButton: Button
    private lateinit var resetButton: Button

    // Cards de instrumentos analógicos
    private lateinit var analogCards: List<CardView>
    private lateinit var analogLabels: List<TextView>
    private lateinit var analogImages: List<ImageView>

    // Cards de instrumentos digitales
    private lateinit var digitalCards: List<CardView>
    private lateinit var digitalLabels: List<TextView>
    private lateinit var digitalImages: List<ImageView>

    // Cards de unidades de medida
    private lateinit var unitCards: List<CardView>
    private lateinit var unitLabels: List<TextView>

    // Estado del juego
    private var selectedAnalogCard: Int = -1
    private var selectedDigitalCard: Int = -1
    private var selectedUnitCard: Int = -1
    private var correctAnswers: Int = 0
    private var totalQuestions: Int = 0

    // Datos del juego
    private val instruments = listOf(
        Triple("Reloj", R.drawable.storage_units_hierarchy, R.drawable.storage_units_hierarchy),
        Triple("Termómetro", R.drawable.storage_units_hierarchy, R.drawable.storage_units_hierarchy),
        Triple("Velocímetro", R.drawable.storage_units_hierarchy, R.drawable.storage_units_hierarchy),
        Triple("Báscula", R.drawable.storage_units_hierarchy, R.drawable.storage_units_hierarchy)
    )

//    // Datos del juego
//    private val instruments = listOf(
//        Triple("Reloj", R.drawable.analog_clock, R.drawable.digital_clock),
//        Triple("Termómetro", R.drawable.analog_thermometer, R.drawable.digital_thermometer),
//        Triple("Velocímetro", R.drawable.analog_speedometer, R.drawable.digital_speedometer),
//        Triple("Báscula", R.drawable.analog_scale, R.drawable.digital_scale)
//    )

    private val units = listOf(
        "Tiempo (segundos, minutos, horas)",
        "Temperatura (grados)",
        "Velocidad (km/h)",
        "Peso (kg)"
    )

    // Mapeo de respuestas correctas: índice de unidad -> índice de instrumento
    private val correctMapping = mapOf(
        0 to 0, // Tiempo -> Reloj
        1 to 1, // Temperatura -> Termómetro
        2 to 2, // Velocidad -> Velocímetro
        3 to 3  // Peso -> Báscula
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_storage_units_interactive, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar vistas
        titleTextView = view.findViewById(R.id.titleTextView)
        instructionsTextView = view.findViewById(R.id.instructionsTextView)
        scoreTextView = view.findViewById(R.id.scoreTextView)
        checkButton = view.findViewById(R.id.checkButton)
        resetButton = view.findViewById(R.id.resetButton)

        // Inicializar cards analógicas
        analogCards = listOf(
            view.findViewById(R.id.analogCard1),
            view.findViewById(R.id.analogCard2),
            view.findViewById(R.id.analogCard3),
            view.findViewById(R.id.analogCard4)
        )

        analogLabels = listOf(
            view.findViewById(R.id.analogLabel1),
            view.findViewById(R.id.analogLabel2),
            view.findViewById(R.id.analogLabel3),
            view.findViewById(R.id.analogLabel4)
        )

        analogImages = listOf(
            view.findViewById(R.id.analogImage1),
            view.findViewById(R.id.analogImage2),
            view.findViewById(R.id.analogImage3),
            view.findViewById(R.id.analogImage4)
        )

        // Inicializar cards digitales
        digitalCards = listOf(
            view.findViewById(R.id.digitalCard1),
            view.findViewById(R.id.digitalCard2),
            view.findViewById(R.id.digitalCard3),
            view.findViewById(R.id.digitalCard4)
        )

        digitalLabels = listOf(
            view.findViewById(R.id.digitalLabel1),
            view.findViewById(R.id.digitalLabel2),
            view.findViewById(R.id.digitalLabel3),
            view.findViewById(R.id.digitalLabel4)
        )

        digitalImages = listOf(
            view.findViewById(R.id.digitalImage1),
            view.findViewById(R.id.digitalImage2),
            view.findViewById(R.id.digitalImage3),
            view.findViewById(R.id.digitalImage4)
        )

        // Inicializar cards de unidades
        unitCards = listOf(
            view.findViewById(R.id.unitCard1),
            view.findViewById(R.id.unitCard2),
            view.findViewById(R.id.unitCard3),
            view.findViewById(R.id.unitCard4)
        )

        unitLabels = listOf(
            view.findViewById(R.id.unitLabel1),
            view.findViewById(R.id.unitLabel2),
            view.findViewById(R.id.unitLabel3),
            view.findViewById(R.id.unitLabel4)
        )

        // Configurar contenido
        setupContent()

        // Configurar listeners
        setupListeners()

        // Actualizar puntuación
        updateScore()
    }

    private fun setupContent() {
        titleTextView.text = "Relaciona Unidades con Instrumentos"

        instructionsTextView.text = """
            Relaciona cada unidad de medida con su correspondiente instrumento analógico y digital:
            
            1. Selecciona una tarjeta de unidad de medida (izquierda)
            2. Selecciona su instrumento analógico correspondiente (centro)
            3. Selecciona su instrumento digital correspondiente (derecha)
            4. Presiona "Verificar" para comprobar tu respuesta
            
            ¡Intenta relacionar correctamente todas las unidades!
        """.trimIndent()

        // Barajar instrumentos y unidades (manteniendo su relación)
        val shuffledIndices = (0..3).toList().shuffled()

        // Configurar tarjetas de instrumentos analógicos
        for (i in 0..3) {
            val instrumentIndex = shuffledIndices[i]
            val instrument = instruments[instrumentIndex]

            analogLabels[i].text = "${instrument.first} Analógico"
            analogImages[i].setImageResource(instrument.second)

            // Guardar el índice original como tag
            analogCards[i].tag = instrumentIndex
        }

        // Configurar tarjetas de instrumentos digitales
        for (i in 0..3) {
            val instrumentIndex = shuffledIndices[i]
            val instrument = instruments[instrumentIndex]

            digitalLabels[i].text = "${instrument.first} Digital"
            digitalImages[i].setImageResource(instrument.third)

            // Guardar el índice original como tag
            digitalCards[i].tag = instrumentIndex
        }

        // Configurar tarjetas de unidades (también barajadas)
        val shuffledUnitIndices = (0..3).toList().shuffled()
        for (i in 0..3) {
            val unitIndex = shuffledUnitIndices[i]
            unitLabels[i].text = units[unitIndex]

            // Guardar el índice original como tag
            unitCards[i].tag = unitIndex
        }
    }

    private fun setupListeners() {
        // Configurar listeners para tarjetas de unidades
        for (i in unitCards.indices) {
            unitCards[i].setOnClickListener {
                selectUnitCard(i)
            }
        }

        // Configurar listeners para tarjetas analógicas
        for (i in analogCards.indices) {
            analogCards[i].setOnClickListener {
                selectAnalogCard(i)
            }
        }

        // Configurar listeners para tarjetas digitales
        for (i in digitalCards.indices) {
            digitalCards[i].setOnClickListener {
                selectDigitalCard(i)
            }
        }

        // Configurar botones
        checkButton.setOnClickListener {
            checkAnswer()
        }

        resetButton.setOnClickListener {
            resetSelections()
        }
    }

    private fun selectUnitCard(index: Int) {
        // Deseleccionar la tarjeta anterior
        if (selectedUnitCard != -1) {
            unitCards[selectedUnitCard].setCardBackgroundColor(Color.WHITE)
        }

        // Seleccionar la nueva tarjeta
        selectedUnitCard = index
        unitCards[index].setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200))
    }

    private fun selectAnalogCard(index: Int) {
        // Deseleccionar la tarjeta anterior
        if (selectedAnalogCard != -1) {
            analogCards[selectedAnalogCard].setCardBackgroundColor(Color.WHITE)
        }

        // Seleccionar la nueva tarjeta
        selectedAnalogCard = index
        analogCards[index].setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200))
    }

    private fun selectDigitalCard(index: Int) {
        // Deseleccionar la tarjeta anterior
        if (selectedDigitalCard != -1) {
            digitalCards[selectedDigitalCard].setCardBackgroundColor(Color.WHITE)
        }

        // Seleccionar la nueva tarjeta
        selectedDigitalCard = index
        digitalCards[index].setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200))
    }

    private fun checkAnswer() {
        // Verificar que se hayan seleccionado todas las tarjetas necesarias
        if (selectedUnitCard == -1 || selectedAnalogCard == -1 || selectedDigitalCard == -1) {
            Toast.makeText(context, "Debes seleccionar una unidad, un instrumento analógico y uno digital", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener los índices originales de las tarjetas seleccionadas
        val unitIndex = unitCards[selectedUnitCard].tag as Int
        val analogInstrumentIndex = analogCards[selectedAnalogCard].tag as Int
        val digitalInstrumentIndex = digitalCards[selectedDigitalCard].tag as Int

        // Verificar si la respuesta es correcta
        val isCorrect = correctMapping[unitIndex] == analogInstrumentIndex &&
                correctMapping[unitIndex] == digitalInstrumentIndex

        // Incrementar el contador de preguntas
        totalQuestions++

        if (isCorrect) {
            // Incrementar puntuación
            correctAnswers++

            // Mostrar mensaje de éxito
            Toast.makeText(context, "¡Correcto! Has relacionado correctamente.", Toast.LENGTH_SHORT).show()

            // Desactivar las tarjetas usadas
            unitCards[selectedUnitCard].isEnabled = false
            analogCards[selectedAnalogCard].isEnabled = false
            digitalCards[selectedDigitalCard].isEnabled = false

            // Cambiar el color a verde para indicar acierto
            unitCards[selectedUnitCard].setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
            analogCards[selectedAnalogCard].setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
            digitalCards[selectedDigitalCard].setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))

            // Resetear selección actual
            selectedUnitCard = -1
            selectedAnalogCard = -1
            selectedDigitalCard = -1

            // Verificar si se completaron todas las relaciones
            if (correctAnswers == 4) {
                Toast.makeText(context, "¡Felicidades! Has relacionado correctamente todas las unidades.", Toast.LENGTH_LONG).show()
                checkButton.isEnabled = false
            }
        } else {
            // Mostrar mensaje de error
            Toast.makeText(context, "Incorrecto. Intenta otra combinación.", Toast.LENGTH_SHORT).show()

            // Resetear selecciones para intentar de nuevo
            resetSelections()
        }

        // Actualizar puntuación
        updateScore()
    }

    private fun resetSelections() {
        // Deseleccionar todas las tarjetas
        if (selectedUnitCard != -1) {
            unitCards[selectedUnitCard].setCardBackgroundColor(Color.WHITE)
            selectedUnitCard = -1
        }

        if (selectedAnalogCard != -1) {
            analogCards[selectedAnalogCard].setCardBackgroundColor(Color.WHITE)
            selectedAnalogCard = -1
        }

        if (selectedDigitalCard != -1) {
            digitalCards[selectedDigitalCard].setCardBackgroundColor(Color.WHITE)
            selectedDigitalCard = -1
        }
    }

    private fun updateScore() {
        scoreTextView.text = "Puntuación: $correctAnswers de $totalQuestions"
    }
}