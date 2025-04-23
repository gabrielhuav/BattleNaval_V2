package ovh.gabrielhuav.battlenaval_v2.unidadesalmacenamiento

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R

class StorageUnitsRelationshipFragment : Fragment() {

    // Vistas para la sección expandible de ejemplos
    private lateinit var examplesCardView: CardView
    private lateinit var examplesHeaderLayout: LinearLayout
    private lateinit var examplesHeaderText: TextView
    private lateinit var examplesArrowImage: ImageView
    private lateinit var examplesContent: TextView

    // Vistas para la sección expandible de comparación
    private lateinit var expandableCardView: CardView
    private lateinit var expandableHeaderLayout: LinearLayout
    private lateinit var expandableHeaderText: TextView
    private lateinit var expandArrowImage: ImageView
    private lateinit var expandableContent: TextView

    // Vistas para la sección interactiva
    private lateinit var matchingLayout: LinearLayout
    private lateinit var matchContainer1: LinearLayout
    private lateinit var matchContainer2: LinearLayout
    private lateinit var feedbackText: TextView
    private lateinit var checkButton: Button
    private lateinit var resetButton: Button

    // Estados de expansión
    private var isExamplesExpanded = false
    private var isComparisonExpanded = false

    // Estado de relaciones seleccionadas
    private var selectedItemIndex = -1
    private var selectedUnitIndex = -1
    private var matchedPairs = mutableMapOf<Int, Int>()
    private var selectedItem: CardView? = null
    private var selectedUnit: CardView? = null

    // Datos para el emparejamiento
    private val digitalItems = listOf(
        "Una página de texto",
        "Una fotografía de calidad media",
        "Una canción de 3 minutos",
        "Un video de 10 minutos en HD",
        "Un libro electrónico completo"
    )

    private val storageUnits = listOf(
        "2-5 KB",
        "2-5 MB",
        "3-5 MB",
        "200-500 MB",
        "1-2 MB"
    )

    // Respuestas correctas (índice de digitalItems -> índice de storageUnits)
    private val correctMatches = mapOf(
        0 to 0, // Página de texto -> 2-5 KB
        1 to 1, // Fotografía -> 2-5 MB
        2 to 2, // Canción -> 3-5 MB
        3 to 3, // Video -> 200-500 MB
        4 to 4  // Libro electrónico -> 1-2 MB
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_storage_units_relationship, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val contentTextView = view.findViewById<TextView>(R.id.contentTextView)
        val comparisonTableLayout = view.findViewById<TableLayout>(R.id.comparisonTableLayout)
        val illustrationImageView = view.findViewById<ImageView>(R.id.illustrationImageView)

        // Inicializar las vistas de la sección expandible de ejemplos
        examplesCardView = view.findViewById(R.id.examplesCardView)
        examplesHeaderLayout = view.findViewById(R.id.examplesHeaderLayout)
        examplesHeaderText = view.findViewById(R.id.examplesHeaderText)
        examplesArrowImage = view.findViewById(R.id.examplesArrowImage)
        examplesContent = view.findViewById(R.id.examplesContent)

        // Inicializar las vistas de la sección expandible de comparación
        expandableCardView = view.findViewById(R.id.expandableCardView)
        expandableHeaderLayout = view.findViewById(R.id.expandableHeaderLayout)
        expandableHeaderText = view.findViewById(R.id.expandableHeaderText)
        expandArrowImage = view.findViewById(R.id.expandArrowImage)
        expandableContent = view.findViewById(R.id.expandableContent)

        // Inicializar vistas para la sección interactiva
        matchingLayout = view.findViewById(R.id.matchingLayout)
        matchContainer1 = view.findViewById(R.id.matchContainer1)
        matchContainer2 = view.findViewById(R.id.matchContainer2)
        feedbackText = view.findViewById(R.id.feedbackText)
        checkButton = view.findViewById(R.id.checkButton)
        resetButton = view.findViewById(R.id.resetButton)

        titleTextView.text = "El Mundo Real y Digital: ¿Cómo se Relacionan?"

        contentTextView.text = """
            Nuestro mundo físico (el que podemos tocar) y el mundo digital (el de las computadoras) están conectados. Muchas cosas del mundo real pueden convertirse en información digital que guardamos en dispositivos.
            
            Algunos ejemplos de cómo convertimos cosas reales a digitales:
            
            • Una foto impresa → Una imagen digital (JPG, PNG)
            • Una carta escrita → Un documento de texto (PDF, DOC)
            • Un disco de música → Archivos de audio (MP3, WAV)
            • Un libro → Un libro electrónico (PDF, EPUB)
            • Un video en cinta → Un archivo de video (MP4, AVI)
            
            Al convertir algo físico a digital, lo transformamos en información que puede guardarse como bits y bytes.
        """.trimIndent()

        // Configurar texto para la sección de ejemplos expandible
        examplesHeaderText.text = "¿CUÁNTO ESPACIO OCUPAN LAS COSAS QUE CONVERTIMOS?"
        examplesContent.text = """
            Piensa en estas comparaciones:
            
            • Una página de texto: 2 KB (kilobytes) aproximadamente
                → Podrías guardar 500 páginas en 1 MB
            
            • Una fotografía de calidad media: 2 MB (megabytes) aproximadamente
                → En 1 GB cabrían unas 500 fotos
            
            • Una canción de 3 minutos: 3-5 MB aproximadamente
                → Un álbum completo: 50-70 MB
            
            • Un video de 10 minutos en HD: 200-500 MB aproximadamente
                → Una película de 2 horas: 2-4 GB
            
            • Todos tus libros de texto de un año escolar (digitalizados): 
                → Aproximadamente 100-200 MB
            
            ¡Es increíble! Una biblioteca pequeña con 1,000 libros podría caber en una memoria USB de 8 GB. El mundo digital nos permite almacenar mucha información en espacios muy pequeños.
        """.trimIndent()

        // Configurar el texto de la sección expandible de comparación
        expandableHeaderText.text = "¿CÓMO SE COMPARAN DIFERENTES UNIDADES DE ALMACENAMIENTO?"
        expandableContent.text = """
            Para entender mejor la relación entre unidades de almacenamiento, observa estas equivalencias:
            
            • 1 Kilobyte (KB) = 1,024 Bytes
            • 1 Megabyte (MB) = 1,024 KB = 1,048,576 Bytes
            • 1 Gigabyte (GB) = 1,024 MB = 1,073,741,824 Bytes
            • 1 Terabyte (TB) = 1,024 GB = 1,099,511,627,776 Bytes
            
            Para visualizarlo mejor:
            
            • Si un Byte fuera un grano de arroz, un Kilobyte sería un tazón de arroz.
            • Un Megabyte sería un saco de arroz.
            • Un Gigabyte sería un camión lleno de arroz.
            • Un Terabyte sería un campo de arroz entero.
            
            Estas comparaciones nos ayudan a entender la enorme diferencia de escala entre cada unidad y por qué usamos diferentes unidades para diferentes tipos de archivos.
        """.trimIndent()

        // Crear tabla de comparación
        createComparisonTable(comparisonTableLayout)

        // Establecer imagen ilustrativa
        illustrationImageView.setImageResource(R.drawable.analog_vs_digital)

        // Configurar el comportamiento expandible
        setupExpandableSections()

        // Configurar la sección interactiva
        setupMatchingActivity()

        // Configurar botones
        checkButton.setOnClickListener {
            checkMatches()
        }

        resetButton.setOnClickListener {
            resetMatching()
        }
    }

    private fun setupExpandableSections() {
        // Inicialmente, ocultar los contenidos expandibles
        examplesContent.visibility = View.GONE
        expandableContent.visibility = View.GONE

        // Configurar listeners para expandir/contraer
        examplesHeaderLayout.setOnClickListener {
            toggleExamplesExpand()
        }

        expandableHeaderLayout.setOnClickListener {
            toggleComparisonExpand()
        }
    }

    private fun toggleExamplesExpand() {
        isExamplesExpanded = !isExamplesExpanded

        // Rotar la flecha según el estado
        val rotationAngle = if (isExamplesExpanded) 180f else 0f
        val arrowAnimator = ValueAnimator.ofFloat(examplesArrowImage.rotation, rotationAngle)
        arrowAnimator.interpolator = AccelerateDecelerateInterpolator()
        arrowAnimator.duration = 300
        arrowAnimator.addUpdateListener { animation ->
            examplesArrowImage.rotation = animation.animatedValue as Float
        }
        arrowAnimator.start()

        // Mostrar u ocultar el contenido con animación
        if (isExamplesExpanded) {
            examplesContent.visibility = View.VISIBLE
            examplesContent.alpha = 0f

            // Animar la transparencia para una transición suave
            val alphaAnimator = ValueAnimator.ofFloat(0f, 1f)
            alphaAnimator.duration = 300
            alphaAnimator.addUpdateListener { animation ->
                examplesContent.alpha = animation.animatedValue as Float
            }
            alphaAnimator.start()
        } else {
            // Animar la transparencia hacia 0 y luego ocultar
            val alphaAnimator = ValueAnimator.ofFloat(1f, 0f)
            alphaAnimator.duration = 300
            alphaAnimator.addUpdateListener { animation ->
                examplesContent.alpha = animation.animatedValue as Float
                if (animation.animatedFraction == 1.0f) {
                    examplesContent.visibility = View.GONE
                }
            }
            alphaAnimator.start()
        }
    }

    private fun toggleComparisonExpand() {
        isComparisonExpanded = !isComparisonExpanded

        // Rotar la flecha según el estado
        val rotationAngle = if (isComparisonExpanded) 180f else 0f
        val arrowAnimator = ValueAnimator.ofFloat(expandArrowImage.rotation, rotationAngle)
        arrowAnimator.interpolator = AccelerateDecelerateInterpolator()
        arrowAnimator.duration = 300
        arrowAnimator.addUpdateListener { animation ->
            expandArrowImage.rotation = animation.animatedValue as Float
        }
        arrowAnimator.start()

        // Mostrar u ocultar el contenido con animación
        if (isComparisonExpanded) {
            expandableContent.visibility = View.VISIBLE
            expandableContent.alpha = 0f

            // Animar la transparencia para una transición suave
            val alphaAnimator = ValueAnimator.ofFloat(0f, 1f)
            alphaAnimator.duration = 300
            alphaAnimator.addUpdateListener { animation ->
                expandableContent.alpha = animation.animatedValue as Float
            }
            alphaAnimator.start()
        } else {
            // Animar la transparencia hacia 0 y luego ocultar
            val alphaAnimator = ValueAnimator.ofFloat(1f, 0f)
            alphaAnimator.duration = 300
            alphaAnimator.addUpdateListener { animation ->
                expandableContent.alpha = animation.animatedValue as Float
                if (animation.animatedFraction == 1.0f) {
                    expandableContent.visibility = View.GONE
                }
            }
            alphaAnimator.start()
        }
    }

    private fun setupMatchingActivity() {
        // Limpiar los contenedores
        matchContainer1.removeAllViews()
        matchContainer2.removeAllViews()

        // Crear tarjetas para elementos digitales
        for ((index, item) in digitalItems.withIndex()) {
            val card = createMatchCard(item, index)
            matchContainer1.addView(card)

            // Configurar clic para seleccionar
            card.setOnClickListener {
                selectItem(card, index)
            }
        }

        // Crear tarjetas para unidades de almacenamiento
        for ((index, unit) in storageUnits.withIndex()) {
            val card = createMatchCard(unit, index)
            matchContainer2.addView(card)

            // Configurar clic para seleccionar
            card.setOnClickListener {
                selectUnit(card, index)
            }
        }
    }

    private fun createMatchCard(text: String, tag: Int): CardView {
        val card = CardView(requireContext())
        card.radius = resources.getDimension(R.dimen.card_corner_radius)
        card.cardElevation = resources.getDimension(R.dimen.card_elevation)
        card.useCompatPadding = true
        card.tag = tag

        // Parámetros para que todas las tarjetas sean del mismo ancho
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(8, 8, 8, 8)
        card.layoutParams = params

        val textView = TextView(requireContext())
        textView.text = text
        textView.setPadding(24, 16, 24, 16)
        textView.textSize = 16f
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        card.addView(textView)
        return card
    }

    private fun selectItem(card: CardView, index: Int) {
        // Si este elemento ya está emparejado, no hacer nada
        if (matchedPairs.containsKey(index)) {
            return
        }

        // Deseleccionar la tarjeta anterior si existe
        selectedItem?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))

        // Seleccionar esta tarjeta
        selectedItem = card
        selectedItemIndex = index
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200))

        // Si ya hay una unidad seleccionada, crear relación temporal
        if (selectedUnitIndex != -1) {
            // Añadir relación temporal
            feedbackText.text = "Seleccionados: ${digitalItems[selectedItemIndex]} ↔ ${storageUnits[selectedUnitIndex]}"
        } else {
            feedbackText.text = "Seleccionado: ${digitalItems[selectedItemIndex]} - Ahora selecciona una unidad de almacenamiento"
        }
    }

    private fun selectUnit(card: CardView, index: Int) {
        // Si esta unidad ya está emparejada, no hacer nada
        if (matchedPairs.containsValue(index)) {
            return
        }

        // Deseleccionar la tarjeta anterior si existe
        selectedUnit?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))

        // Seleccionar esta tarjeta
        selectedUnit = card
        selectedUnitIndex = index
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200))

        // Si ya hay un elemento seleccionado, crear relación temporal
        if (selectedItemIndex != -1) {
            // Añadir relación temporal
            feedbackText.text = "Seleccionados: ${digitalItems[selectedItemIndex]} ↔ ${storageUnits[selectedUnitIndex]}"
        } else {
            feedbackText.text = "Seleccionado: ${storageUnits[selectedUnitIndex]} - Ahora selecciona un elemento digital"
        }
    }

    private fun checkMatches() {
        // Verificar que hay un elemento y una unidad seleccionados
        if (selectedItemIndex == -1 || selectedUnitIndex == -1) {
            feedbackText.text = "Debes seleccionar un elemento digital y una unidad de almacenamiento"
            return
        }

        // Verificar si la relación es correcta
        val isCorrect = correctMatches[selectedItemIndex] == selectedUnitIndex

        if (isCorrect) {
            // Marcar ambas tarjetas como emparejadas correctamente
            selectedItem?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
            selectedUnit?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))

            // Añadir a parejas emparejadas
            matchedPairs[selectedItemIndex] = selectedUnitIndex

            // Actualizar feedback
            feedbackText.text = "¡Correcto! ${digitalItems[selectedItemIndex]} ocupa aproximadamente ${storageUnits[selectedUnitIndex]}"

            // Desactivar las tarjetas emparejadas
            selectedItem?.isClickable = false
            selectedUnit?.isClickable = false

            // Reiniciar selección actual
            selectedItemIndex = -1
            selectedUnitIndex = -1
            selectedItem = null
            selectedUnit = null

            // Verificar si se completaron todos los emparejamientos
            if (matchedPairs.size == digitalItems.size) {
                feedbackText.text = "¡Felicidades! Has emparejado correctamente todos los elementos"
                Toast.makeText(context, "¡Actividad completada correctamente!", Toast.LENGTH_LONG).show()
            }
        } else {
            // Marcar temporalmente en rojo
            selectedItem?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
            selectedUnit?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))

            // Actualizar feedback
            feedbackText.text = "Incorrecto. Intenta de nuevo"

            // Programar reinicio de colores después de un tiempo
            selectedItem?.postDelayed({
                // Solo cambiar color si la tarjeta no está emparejada
                if (selectedItemIndex != -1 && !matchedPairs.containsKey(selectedItemIndex)) {
                    selectedItem?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
                if (selectedUnitIndex != -1 && !matchedPairs.containsValue(selectedUnitIndex)) {
                    selectedUnit?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                }

                // Reiniciar selección
                selectedItemIndex = -1
                selectedUnitIndex = -1
                selectedItem = null
                selectedUnit = null
            }, 1000)
        }
    }

    private fun resetMatching() {
        // Reiniciar todas las variables de estado
        selectedItemIndex = -1
        selectedUnitIndex = -1
        selectedItem = null
        selectedUnit = null
        matchedPairs.clear()

        // Limpiar feedback
        feedbackText.text = "Selecciona un elemento digital y una unidad de almacenamiento para emparejarlos"

        // Reiniciar todas las tarjetas
        setupMatchingActivity()
    }

    private fun createComparisonTable(tableLayout: TableLayout) {
        // Crear encabezados
        val headerRow = TableRow(requireContext())
        listOf("Objeto Físico", "Versión Digital", "Tamaño Digital Aprox.").forEach { header ->
            val textView = TextView(requireContext()).apply {
                text = header
                setPadding(8, 8, 8, 8)
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_700))
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                textSize = 16f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            headerRow.addView(textView)
        }
        tableLayout.addView(headerRow)

        // Datos de comparación
        val comparisonData = listOf(
            arrayOf("Libreta de notas (100 páginas)", "Documento de texto", "200 KB"),
            arrayOf("Álbum de fotos (100 imágenes)", "Carpeta con imágenes JPG", "200-300 MB"),
            arrayOf("Enciclopedia (20 tomos)", "Wikipedia (parcial)", "10 GB"),
            arrayOf("Colección de 100 CDs de música", "Biblioteca de MP3", "30-50 GB"),
            arrayOf("DVD con película", "Archivo MP4", "2-4 GB"),
            arrayOf("Tu mochila con libros de texto", "Carpeta con PDFs", "100-200 MB")
        )

        // Agregar filas de datos
        comparisonData.forEach { rowData ->
            val row = TableRow(requireContext())

            rowData.forEach { cellData ->
                row.addView(createTableCell(cellData))
            }

            tableLayout.addView(row)
        }
    }

    private fun createTableCell(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            setPadding(12, 8, 12, 8)
            textSize = 14f
        }
    }
}