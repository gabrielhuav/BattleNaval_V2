package ovh.gabrielhuav.battlenaval_v2.sistemabinario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R

class DecimalToBinaryExplanationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_decimal_to_binary_explanation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val contentTextView = view.findViewById<TextView>(R.id.contentTextView)
        val exampleTableLayout = view.findViewById<TableLayout>(R.id.exampleTableLayout)
        val illustrationImageView = view.findViewById<ImageView>(R.id.illustrationImageView)

        titleTextView.text = "Convertir de Decimal a Binario"

        contentTextView.text = """
            Convertir un número decimal a binario es como dividir "porciones" de un número.

            Sigue estos pasos:
            
            1. Encuentra la mayor potencia de 2 que cabe en tu número.
            2. Resta esa potencia de tu número.
            3. Continúa el proceso con el número restante.
            4. Coloca un 1 en cada posición donde usaste una potencia de 2.
            5. Coloca un 0 en las posiciones que no usaste.
            
            Por ejemplo, para convertir el número 28 a binario:
            
            • La mayor potencia de 2 menor o igual a 28 es 16 (2⁴)
            • 28 - 16 = 12 (nos quedan 12 por convertir)
            • La mayor potencia de 2 menor o igual a 12 es 8 (2³)
            • 12 - 8 = 4 (nos quedan 4 por convertir)
            • La mayor potencia de 2 menor o igual a 4 es 4 (2²)
            • 4 - 4 = 0 (ya no queda nada)
            
            Por lo tanto, usamos: 16 + 8 + 4 = 28
            
            Esto se representa como 11100 en binario.
            (1 en posición de 16, 1 en posición de 8, 1 en posición de 4, 0 en posición de 2, 0 en posición de 1)
        """.trimIndent()

        // Crear tabla de ejemplo para 28 -> 11100
        createExampleTable(exampleTableLayout)

        illustrationImageView.setImageResource(R.drawable.decimal_to_binary)
    }

    private fun createExampleTable(tableLayout: TableLayout) {
        // Encabezados
        val headerRow = TableRow(requireContext())
        val headers = listOf("División", "Cociente", "Residuo (Bit)")
        for (header in headers) {
            headerRow.addView(createTextView(header, true))
        }
        tableLayout.addView(headerRow)

        // Datos del método de división para 28 -> 11100
        val divisions = listOf(
            Triple("28 ÷ 2", "14", "0 (LSB)"),
            Triple("14 ÷ 2", "7", "0"),
            Triple("7 ÷ 2", "3", "1"),
            Triple("3 ÷ 2", "1", "1"),
            Triple("1 ÷ 2", "0", "1 (MSB)")
        )

        for (division in divisions) {
            val row = TableRow(requireContext())
            row.addView(createTextView(division.first))
            row.addView(createTextView(division.second))
            row.addView(createTextView(division.third))
            tableLayout.addView(row)
        }

        // Resultado final
        val resultRow = TableRow(requireContext())
        val resultCell = createTextView("Resultado binario (de abajo hacia arriba): 11100", true)
        val layoutParams = TableRow.LayoutParams()
        layoutParams.span = 3  // Ocupar 3 columnas
        resultCell.layoutParams = layoutParams
        resultRow.addView(resultCell)
        tableLayout.addView(resultRow)
    }

    private fun createTextView(text: String, isHeader: Boolean = false): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            setPadding(16, 8, 16, 8)
            if (isHeader) {
                setBackgroundResource(R.color.teal_700)
                setTextColor(resources.getColor(android.R.color.white))
            }
        }
    }
}