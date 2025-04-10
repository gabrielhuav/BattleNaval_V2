package ovh.gabrielhuav.battlenaval_v2.unidadesalmacenamiento

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R

class StorageUnitsRelationshipFragment : Fragment() {

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

        titleTextView.text = "Relación entre Unidades Analógicas y Digitales"

        contentTextView.text = """
            Tanto en el mundo analógico como en el digital, necesitamos medir y cuantificar información. Aunque las representaciones son diferentes, muchas veces están midiendo los mismos fenómenos.
            
            Los dispositivos analógicos suelen representar la información de forma continua, mediante elementos físicos como agujas, columnas de líquido o manecillas. Por otro lado, los dispositivos digitales utilizan representaciones discretas, generalmente numéricas, que pueden ser procesadas por computadoras.
            
            Algunos ejemplos comunes de dispositivos con versiones analógicas y digitales incluyen relojes, termómetros, velocímetros y básculas. En cada caso, aunque la representación visual cambia, la magnitud medida sigue siendo la misma.
            
            La siguiente tabla muestra ejemplos de dispositivos de medición y sus versiones analógicas y digitales:
        """.trimIndent()

        // Crear tabla de comparación
        createComparisonTable(comparisonTableLayout)

        // Establecer imagen ilustrativa
        illustrationImageView.setImageResource(R.drawable.analog_vs_digital)
    }

    private fun createComparisonTable(tableLayout: TableLayout) {
        // Crear encabezados
        val headerRow = TableRow(requireContext())
        listOf("Tipo de Medición", "Versión Analógica", "Versión Digital").forEach { header ->
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
            arrayOf("Tiempo", "Reloj de manecillas", "Reloj digital (LCD/LED)"),
            arrayOf("Temperatura", "Termómetro de mercurio", "Termómetro digital"),
            arrayOf("Velocidad", "Velocímetro de aguja", "Velocímetro digital"),
            arrayOf("Peso", "Báscula de resorte", "Báscula digital"),
            arrayOf("Presión arterial", "Esfigmomanómetro de mercurio", "Monitor digital de presión"),
            arrayOf("Volumen de sonido", "VU meter con aguja", "Medidor digital en decibelios")
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