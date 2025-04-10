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

class StorageUnitsTypesFragment : Fragment() {

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

        titleTextView.text = "Tipos de Unidades de Almacenamiento"

        contentTextView.text = """
            Las unidades de medida de almacenamiento están organizadas en una jerarquía, donde cada unidad es 1024 veces mayor que la unidad anterior (o 2¹⁰, ya que las computadoras trabajan en base 2).
            
            Aunque en el uso común a veces se redondea a 1000 para simplificar, la definición técnica utiliza 1024 debido a la naturaleza binaria de los sistemas informáticos.
            
            Esta jerarquía comienza con el bit, la unidad más básica, que representa un solo dígito binario (0 o 1), y continúa con unidades cada vez más grandes para representar cantidades mayores de información.
            
            A continuación se presentan las principales unidades de almacenamiento:
        """.trimIndent()

        // Crear tabla de unidades
        createUnitsTable(unitsTableLayout)

        // Establecer imagen ilustrativa
        illustrationImageView.setImageResource(R.drawable.storage_units_hierarchy)
    }

    private fun createUnitsTable(tableLayout: TableLayout) {
        // Crear encabezados
        val headerRow = TableRow(requireContext())
        listOf("Unidad", "Símbolo", "Tamaño", "Equivalencia").forEach { header ->
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

        // Datos de unidades
        val unitsData = listOf(
            arrayOf("Bit", "b", "1 dígito binario (0 o 1)", "Unidad básica de información"),
            arrayOf("Byte", "B", "8 bits", "1 carácter alfanumérico"),
            arrayOf("Kilobyte", "KB", "1,024 bytes", "Una página de texto (≈2 KB)"),
            arrayOf("Megabyte", "MB", "1,024 KB", "Una canción MP3 (≈3-5 MB)"),
            arrayOf("Gigabyte", "GB", "1,024 MB", "Una película (≈1-4 GB)"),
            arrayOf("Terabyte", "TB", "1,024 GB", "Toda tu colección de música y videos"),
            arrayOf("Petabyte", "PB", "1,024 TB", "Datos de grandes empresas"),
            arrayOf("Exabyte", "EB", "1,024 PB", "Datos de internet de un país"),
            arrayOf("Zettabyte", "ZB", "1,024 EB", "Datos globales de internet (anual)"),
            arrayOf("Yottabyte", "YB", "1,024 ZB", "Datos de centros de datos mundiales")
        )

        // Agregar filas de datos
        unitsData.forEach { rowData ->
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
            textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        }
    }
}