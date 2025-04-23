package ovh.gabrielhuav.battlenaval_v2.unidadesalmacenamiento

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R

class StorageUnitsTypesFragment : Fragment() {

    // Vistas para la sección expandible
    private lateinit var expandableCardView: CardView
    private lateinit var expandableHeaderLayout: LinearLayout
    private lateinit var expandableHeaderText: TextView
    private lateinit var expandArrowImage: ImageView
    private lateinit var expandableContent: TextView

    // Estado de expansión
    private var isExpanded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_storage_units_types, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val contentTextView = view.findViewById<TextView>(R.id.contentTextView)
        val unitsTableLayout = view.findViewById<TableLayout>(R.id.unitsTableLayout)
        val illustrationImageView = view.findViewById<ImageView>(R.id.illustrationImageView)

        // Inicializar las vistas expandibles
        expandableCardView = view.findViewById(R.id.expandableCardView)
        expandableHeaderLayout = view.findViewById(R.id.expandableHeaderLayout)
        expandableHeaderText = view.findViewById(R.id.expandableHeaderText)
        expandArrowImage = view.findViewById(R.id.expandArrowImage)
        expandableContent = view.findViewById(R.id.expandableContent)

        titleTextView.text = "Conociendo las Unidades de Almacenamiento"

        contentTextView.text = """
            Las unidades de almacenamiento digital son como una escalera donde cada escalón es 1,024 veces más grande que el anterior.
            
            Todo comienza con el bit, que es la unidad más pequeña. Un bit solo puede ser 0 o 1 (como un interruptor que está apagado o encendido).
            
            A partir del bit, vamos formando unidades más grandes, ¡justo como construimos palabras con letras!
            
            Así funciona esta "escalera digital":
        """.trimIndent()

        // Crear tabla de unidades simplificada
        createUnitsTable(unitsTableLayout)

        // Configurar el texto de la sección expandible
        expandableHeaderText.text = "¿POR QUÉ ES 1,024 Y NO 1,000?"
        expandableContent.text = """
            En el sistema decimal que usamos todos los días, cada unidad es 10 veces mayor que la anterior:
            • 10 unidades = 1 decena
            • 10 decenas = 1 centena
            • 10 centenas = 1 mil
            
            Pero en el sistema binario de las computadoras, cada unidad es 2 veces mayor:
            • 2¹ = 2
            • 2² = 4
            • 2³ = 8
            • ...y así hasta 2¹⁰ = 1,024
            
            Por eso, en informática:
            • 1 Kilobyte = 1,024 bytes (no 1,000)
            • 1 Megabyte = 1,024 KB = 1,048,576 bytes
            
            Es como si contáramos de 1,024 en 1,024 en lugar de contar de 1,000 en 1,000.
            
            En la vida diaria, las unidades que más usarás son:
            • Megabytes (MB): para aplicaciones y archivos pequeños
            • Gigabytes (GB): para juegos, películas y almacenamiento de dispositivos
        """.trimIndent()

        // Establecer imagen ilustrativa
        illustrationImageView.setImageResource(R.drawable.storage_units_hierarchy)

        // Configurar el comportamiento expandible
        setupExpandableSection()
    }

    private fun setupExpandableSection() {
        // Inicialmente, ocultar el contenido expandible pero mantener la sección visible
        expandableContent.visibility = View.GONE

        // Configurar el listener para expandir/contraer
        expandableHeaderLayout.setOnClickListener {
            toggleExpand()
        }
    }

    private fun toggleExpand() {
        isExpanded = !isExpanded

        // Rotar la flecha según el estado (180 grados si está expandido)
        val rotationAngle = if (isExpanded) 180f else 0f
        val arrowAnimator = ValueAnimator.ofFloat(expandArrowImage.rotation, rotationAngle)
        arrowAnimator.interpolator = AccelerateDecelerateInterpolator()
        arrowAnimator.duration = 300
        arrowAnimator.addUpdateListener { animation ->
            expandArrowImage.rotation = animation.animatedValue as Float
        }
        arrowAnimator.start()

        // Mostrar u ocultar el contenido con animación
        if (isExpanded) {
            // Primero hacer visible el contenido con altura 0
            expandableContent.visibility = View.VISIBLE
            expandableContent.alpha = 0f
            expandableContent.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

            // Animar la altura desde 0 hasta la altura completa
            val heightAnimator = ValueAnimator.ofInt(0, expandableContent.measuredHeight)
            heightAnimator.interpolator = AccelerateDecelerateInterpolator()
            heightAnimator.duration = 300
            heightAnimator.addUpdateListener { animation ->
                val params = expandableContent.layoutParams
                params.height = animation.animatedValue as Int
                expandableContent.layoutParams = params

                // Animar la transparencia gradualmente
                expandableContent.alpha = animation.animatedFraction
            }
            heightAnimator.start()
        } else {
            // Animar la altura desde la altura actual hasta 0
            val heightAnimator = ValueAnimator.ofInt(expandableContent.height, 0)
            heightAnimator.interpolator = AccelerateDecelerateInterpolator()
            heightAnimator.duration = 300
            heightAnimator.addUpdateListener { animation ->
                val params = expandableContent.layoutParams
                params.height = animation.animatedValue as Int
                expandableContent.layoutParams = params

                // Animar la transparencia gradualmente
                expandableContent.alpha = 1f - animation.animatedFraction

                // Cuando la animación termina, ocultar completamente el contenido
                if (animation.animatedFraction == 1.0f) {
                    expandableContent.visibility = View.GONE

                    // Restaurar la altura a WRAP_CONTENT para futuras expansiones
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    expandableContent.layoutParams = params
                }
            }
            heightAnimator.start()
        }
    }

    private fun createUnitsTable(tableLayout: TableLayout) {
        // Crear encabezados
        val headerRow = TableRow(requireContext())
        listOf("Unidad", "Símbolo", "¿Qué tan grande es?", "Ejemplo").forEach { header ->
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

        // Datos de unidades simplificados para secundaria
        val unitsData = listOf(
            arrayOf("Bit", "b", "La unidad más pequeña (0 o 1)", "Un interruptor encendido/apagado"),
            arrayOf("Byte", "B", "8 bits juntos", "Una letra o símbolo"),
            arrayOf("Kilobyte", "KB", "1,024 bytes", "Un mensaje de texto o correo breve"),
            arrayOf("Megabyte", "MB", "1,024 KB", "Una foto o canción"),
            arrayOf("Gigabyte", "GB", "1,024 MB", "Una película o juego"),
            arrayOf("Terabyte", "TB", "1,024 GB", "Todas tus fotos, videos y juegos juntos")
        )

        // Agregar filas de datos
        unitsData.forEach { rowData ->
            val row = TableRow(requireContext())

            rowData.forEach { cellData ->
                row.addView(createTableCell(cellData))
            }

            tableLayout.addView(row)
        }

        // Agregar nota sobre unidades mayores
        val footnoteRow = TableRow(requireContext())
        val footnoteCell = createTableCell("Existen unidades aún mayores (Petabyte, Exabyte, Zettabyte, Yottabyte) usadas por grandes empresas como Google o Facebook")
        footnoteCell.setTypeface(null, android.graphics.Typeface.ITALIC)

        // Hacer que la nota ocupe toda la fila
        val layoutParams = TableRow.LayoutParams()
        layoutParams.span = 4  // Ocupar las 4 columnas
        footnoteCell.layoutParams = layoutParams

        footnoteRow.addView(footnoteCell)
        tableLayout.addView(footnoteRow)
    }

    private fun createTableCell(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            setPadding(12, 8, 12, 8)
            textSize = 14f
        }
    }
}