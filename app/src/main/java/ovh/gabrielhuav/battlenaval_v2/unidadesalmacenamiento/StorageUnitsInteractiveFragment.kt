package ovh.gabrielhuav.battlenaval_v2.unidadesalmacenamiento

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.gridlayout.widget.GridLayout
import ovh.gabrielhuav.battlenaval_v2.R

class StorageUnitsInteractiveFragment : Fragment() {

    // Elementos de la interfaz
    private lateinit var titleTextView: TextView
    private lateinit var instructionsTextView: TextView
    private lateinit var scoreTextView: TextView
    private lateinit var itemsContainer: GridLayout
    private lateinit var unitsContainer: GridLayout
    private lateinit var resetButton: Button

    // Estado del juego
    private var selectedItemCard: CardView? = null
    private var selectedUnitCard: CardView? = null
    private var score = 0
    private var totalMatched = 0
    private val maxItems = 10

    // Datos del juego - Relaciones correctas elemento-unidad
    private val correctMatches = mapOf(
        "Una letra" to "Bytes (B)",
        "Un emoji" to "Bytes (B)",
        "Un mensaje de texto" to "Kilobytes (KB)",
        "Un libro electrónico" to "Megabytes (MB)",
        "Una foto de perfil" to "Kilobytes (KB)",
        "Una canción" to "Megabytes (MB)",
        "Un episodio de serie" to "Gigabytes (GB)",
        "Un juego para celular" to "Megabytes (MB)",
        "Una película en HD" to "Gigabytes (GB)",
        "Todas tus fotos y videos" to "Gigabytes (GB)"
    )

    // Lista de elementos digitales para clasificar
    private val digitalItems = listOf(
        "Una letra",
        "Un emoji",
        "Un mensaje de texto",
        "Un libro electrónico",
        "Una foto de perfil",
        "Una canción",
        "Un episodio de serie",
        "Un juego para celular",
        "Una película en HD",
        "Todas tus fotos y videos"
    )

    // Lista de unidades de almacenamiento
    private val storageUnits = listOf(
        "Bytes (B)",
        "Kilobytes (KB)",
        "Megabytes (MB)",
        "Gigabytes (GB)",
        "Terabytes (TB)"
    )

    // Mapa para rastrear los elementos ya emparejados
    private val matchedItems = mutableMapOf<String, String>()

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
        itemsContainer = view.findViewById(R.id.itemsContainer)
        unitsContainer = view.findViewById(R.id.unitsContainer)
        resetButton = view.findViewById(R.id.resetButton)

        // Configurar contenido
        titleTextView.text = "¿Cuánto Espacio Ocupa?"
        instructionsTextView.text = """
            ¡Vamos a jugar! ¿Sabes cuánto espacio ocupa cada cosa en el mundo digital?
            
            Instrucciones:
            1. Toca un elemento de arriba
            2. Luego toca la unidad que crees que mejor lo representa abajo
            3. Si aciertas, ¡ganarás un punto!
            4. Intenta relacionar todos los elementos correctamente
        """.trimIndent()

        // Crear tarjetas para elementos digitales
        createItemCards()

        // Crear tarjetas para unidades de almacenamiento
        createUnitCards()

        // Configurar botón de reinicio
        resetButton.setOnClickListener {
            resetGame()
        }

        // Actualizar puntuación inicial
        updateScore()
    }

    private fun createItemCards() {
        // Limpiar el contenedor
        itemsContainer.removeAllViews()

        // Para cada elemento digital...
        for (item in digitalItems) {
            // Si este elemento ya está emparejado, omitirlo
            if (matchedItems.containsKey(item)) continue

            // Crear una nueva tarjeta
            val card = CardView(requireContext()).apply {
                // Configurar apariencia
                radius = resources.getDimension(R.dimen.card_corner_radius)
                cardElevation = resources.getDimension(R.dimen.card_elevation)
                setCardBackgroundColor(Color.WHITE)

                // Configurar parámetros de layout
                val params = GridLayout.LayoutParams()
                params.width = 0
                params.height = GridLayout.LayoutParams.WRAP_CONTENT
                params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                params.setMargins(8, 8, 8, 8)
                layoutParams = params

                // Hacer la tarjeta clickeable
                isClickable = true
                isFocusable = true

                // Añadir efecto de ripple al hacer clic
                foreground = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ripple_effect
                )

                // Configurar listener
                setOnClickListener {
                    selectItemCard(this, item)
                }

                // Guardar el item como tag para referencia
                tag = item
            }

            // Añadir TextView dentro de la tarjeta
            val textView = TextView(requireContext()).apply {
                text = item
                textSize = 16f
                setTextColor(Color.BLACK)
                setPadding(24, 24, 24, 24)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }

            // Añadir el TextView a la tarjeta
            card.addView(textView)

            // Añadir la tarjeta al contenedor
            itemsContainer.addView(card)
        }
    }

    private fun createUnitCards() {
        // Limpiar el contenedor
        unitsContainer.removeAllViews()

        // Para cada unidad de almacenamiento...
        for (unit in storageUnits) {
            // Crear una nueva tarjeta
            val card = CardView(requireContext()).apply {
                // Configurar apariencia
                radius = resources.getDimension(R.dimen.card_corner_radius)
                cardElevation = resources.getDimension(R.dimen.card_elevation)
                setCardBackgroundColor(Color.WHITE)

                // Configurar parámetros de layout
                val params = GridLayout.LayoutParams()
                params.width = 0
                params.height = GridLayout.LayoutParams.WRAP_CONTENT
                params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                params.setMargins(8, 8, 8, 8)
                layoutParams = params

                // Hacer la tarjeta clickeable
                isClickable = true
                isFocusable = true

                // Añadir efecto de ripple al hacer clic
                foreground = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ripple_effect
                )

                // Configurar listener
                setOnClickListener {
                    selectUnitCard(this, unit)
                }

                // Guardar la unidad como tag para referencia
                tag = unit
            }

            // Añadir TextView dentro de la tarjeta
            val textView = TextView(requireContext()).apply {
                text = unit
                textSize = 16f
                setTextColor(Color.BLACK)
                setPadding(24, 24, 24, 24)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }

            // Añadir el TextView a la tarjeta
            card.addView(textView)

            // Añadir la tarjeta al contenedor
            unitsContainer.addView(card)
        }
    }

    private fun selectItemCard(card: CardView, item: String) {
        // Si ya había una tarjeta seleccionada, resetearla
        selectedItemCard?.setCardBackgroundColor(Color.WHITE)

        // Seleccionar la nueva tarjeta
        selectedItemCard = card
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200))

        // Si también hay una unidad seleccionada, verificar el match
        if (selectedUnitCard != null) {
            val unit = selectedUnitCard?.tag as String
            checkMatch(item, unit)
        }
    }

    private fun selectUnitCard(card: CardView, unit: String) {
        // Si ya había una tarjeta seleccionada, resetearla
        selectedUnitCard?.setCardBackgroundColor(Color.WHITE)

        // Seleccionar la nueva tarjeta
        selectedUnitCard = card
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200))

        // Si también hay un elemento seleccionado, verificar el match
        if (selectedItemCard != null) {
            val item = selectedItemCard?.tag as String
            checkMatch(item, unit)
        }
    }

    private fun checkMatch(item: String, unit: String) {
        // Verificar si el match es correcto según nuestro mapa
        val isCorrect = correctMatches[item] == unit

        if (isCorrect) {
            // Incrementar puntuación
            score++
            totalMatched++

            // Añadir a los items emparejados
            matchedItems[item] = unit

            // Mostrar feedback positivo
            Toast.makeText(
                requireContext(),
                "¡Correcto! $item ocupa aproximadamente $unit",
                Toast.LENGTH_SHORT
            ).show()

            // Marcar visualmente como correcto
            selectedItemCard?.setCardBackgroundColor(Color.GREEN)
            selectedUnitCard?.setCardBackgroundColor(Color.GREEN)

            // Desactivar estas tarjetas para futuros clicks
            selectedItemCard?.isClickable = false
            selectedUnitCard?.isClickable = false

            // Actualizar la puntuación mostrada
            updateScore()

            // Limpiar selecciones
            selectedItemCard = null
            selectedUnitCard = null

            // Recrear las tarjetas para quitar las ya emparejadas
            createItemCards()

            // Verificar si se completaron todos los elementos
            if (totalMatched >= maxItems) {
                // ¡Victoria!
                Toast.makeText(
                    requireContext(),
                    "¡Felicidades! Has completado correctamente todas las relaciones",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            // Feedback negativo
            Toast.makeText(
                requireContext(),
                "Incorrecto. Intenta de nuevo",
                Toast.LENGTH_SHORT
            ).show()

            // Marcar visualmente como incorrecto
            selectedItemCard?.setCardBackgroundColor(Color.RED)
            selectedUnitCard?.setCardBackgroundColor(Color.RED)

            // Esperar un momento y luego resetear el color
            selectedItemCard?.postDelayed({
                selectedItemCard?.setCardBackgroundColor(Color.WHITE)
                selectedUnitCard?.setCardBackgroundColor(Color.WHITE)

                // Limpiar selecciones
                selectedItemCard = null
                selectedUnitCard = null
            }, 1000)
        }
    }

    private fun updateScore() {
        scoreTextView.text = "Puntuación: $score / $maxItems"
    }

    private fun resetGame() {
        // Reiniciar variables de estado
        score = 0
        totalMatched = 0
        matchedItems.clear()

        // Limpiar selecciones
        selectedItemCard = null
        selectedUnitCard = null

        // Recrear tarjetas
        createItemCards()
        createUnitCards()

        // Actualizar puntuación
        updateScore()

        // Feedback
        Toast.makeText(requireContext(), "Juego reiniciado", Toast.LENGTH_SHORT).show()
    }
}