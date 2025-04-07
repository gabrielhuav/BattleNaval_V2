package ovh.gabrielhuav.battlenaval_v2.codigoascii

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

class AsciiTableFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ascii_table, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val contentTextView = view.findViewById<TextView>(R.id.contentTextView)
        val asciiTableLayout = view.findViewById<TableLayout>(R.id.asciiTableLayout)
        val fullAsciiTableImage = view.findViewById<ImageView>(R.id.fullAsciiTableImage)

        titleTextView.text = "Tabla ASCII"

        contentTextView.text = """
            La tabla ASCII asigna números del 0 al 127 a letras, números, signos de puntuación y caracteres de control.
            
            Esta tabla muestra los caracteres ASCII imprimibles (del 32 al 126). Los primeros 32 caracteres (0-31) son caracteres de control no imprimibles como retorno de carro, salto de línea, etc.
            
            La tabla ASCII se divide en secciones:
            
            • 0-31: Caracteres de control (no imprimibles)
            • 32-47: Símbolos y puntuación (" ", "!", "#", etc.)
            • 48-57: Dígitos numéricos (0-9)
            • 58-64: Símbolos y puntuación (":", ";", "<", etc.)
            • 65-90: Letras mayúsculas (A-Z)
            • 91-96: Símbolos y puntuación ("[", "\", "]", etc.)
            • 97-122: Letras minúsculas (a-z)
            • 123-126: Símbolos y puntuación ("{", "|", "}", "~")
            • 127: Carácter DEL (no imprimible)
            
            Abajo se muestra una versión simplificada de la tabla ASCII, con los valores decimal, hexadecimal y el carácter correspondiente.
        """.trimIndent()

        // Llenar la tabla ASCII con algunos valores representativos
        createAsciiTable(asciiTableLayout)

        // Establecer imagen de la tabla ASCII completa
        fullAsciiTableImage.setImageResource(R.drawable.full_ascii_table)
    }

    private fun createAsciiTable(tableLayout: TableLayout) {
        // Crear encabezados
        val headerRow = TableRow(requireContext())
        listOf("Decimal", "Hex", "Carácter", "Decimal", "Hex", "Carácter").forEach { header ->
            val textView = TextView(requireContext()).apply {
                text = header
                setPadding(8, 8, 8, 8)
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_700))
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                textSize = 14f
            }
            headerRow.addView(textView)
        }
        tableLayout.addView(headerRow)

        // Crear filas para caracteres imprimibles representativos
        // Mostraremos algunos rangos importantes: espacio, números, letras, etc.
        val importantRanges = listOf(
            32 to 37,  // Espacio y símbolos básicos
            48 to 53,  // Algunos dígitos
            65 to 70,  // Algunas letras mayúsculas
            97 to 102  // Algunas letras minúsculas
        )

        for (range in importantRanges) {
            val row = TableRow(requireContext())

            for (i in range.first..range.second) {
                // Decimal
                row.addView(createTableCell(i.toString()))

                // Hexadecimal
                row.addView(createTableCell(String.format("%02X", i)))

                // Carácter
                val charStr = if (i == 32) "Espacio" else i.toChar().toString()
                row.addView(createTableCell(charStr))
            }

            tableLayout.addView(row)
        }
    }

    private fun createTableCell(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            setPadding(8, 8, 8, 8)
            textSize = 14f
        }
    }
}