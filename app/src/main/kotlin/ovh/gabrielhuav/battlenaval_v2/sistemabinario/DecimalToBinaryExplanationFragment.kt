package ovh.gabrielhuav.battlenaval_v2.sistemabinario

import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.text.set
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
        val contentTextViewDivision = view.findViewById<TextView>(R.id.contentTextViewDivision)
        val contentTextViewPowers = view.findViewById<TextView>(R.id.contentTextViewPowers)
        val exampleTableLayout = view.findViewById<TableLayout>(R.id.exampleTableLayout)
        val illustrationImageView = view.findViewById<ImageView>(R.id.illustrationImageView)
        val lsbMsbExplanation = view.findViewById<TextView>(R.id.lsbMsbExplanation)

        titleTextView.text = "Convertir de Decimal a Binario"

        // Método principal - División sucesiva por 2
        val divisionText = """
            Convertir un número decimal a binario es un proceso sencillo usando el método de división.

            Método principal - División sucesiva por 2:
            
            1. Divide el número decimal entre 2.
            2. Anota el cociente y el residuo (0 o 1).
            3. Divide el cociente nuevamente entre 2.
            4. Repite hasta que el cociente sea 0.
            5. El número binario se forma tomando los residuos en orden inverso (de abajo hacia arriba).
            
            Por ejemplo, para convertir el número 28 a binario:
            
            • 28 ÷ 2 = 14, residuo 0
            • 14 ÷ 2 = 7, residuo 0
            • 7 ÷ 2 = 3, residuo 1
            • 3 ÷ 2 = 1, residuo 1
            • 1 ÷ 2 = 0, residuo 1
            
            Leyendo los residuos de abajo hacia arriba: 11100
        """.trimIndent()

        // Método alternativo - Potencias de 2
        val powersText = """
            Método alternativo - Potencias de 2:
            
            1. Encuentra la mayor potencia de 2 que cabe en tu número.
            2. Resta esa potencia de tu número.
            3. Continúa el proceso con el número restante.
            4. Coloca un 1 en cada posición donde usaste una potencia de 2.
            5. Coloca un 0 en las posiciones que no usaste.
            
            Para el mismo 28:
            
            • La mayor potencia de 2 menor o igual a 28 es 16 (2⁴)
            • 28 - 16 = 12
            • La mayor potencia de 2 menor o igual a 12 es 8 (2³)
            • 12 - 8 = 4
            • La mayor potencia de 2 menor o igual a 4 es 4 (2²)
            • 4 - 4 = 0
            
            Resultado: 11100 (1 en 16, 1 en 8, 1 en 4, 0 en 2, 0 en 1)
        """.trimIndent()

        // Aplicar negritas a los títulos
        contentTextViewDivision.text = applyBoldToTitle(divisionText, "Método principal - División sucesiva por 2:")
        contentTextViewPowers.text = applyBoldToTitle(powersText, "Método alternativo - Potencias de 2:")

        // Configurar la imagen después del método de división
        illustrationImageView.setImageResource(R.drawable.decimal_to_binary)

        // Crear tabla de ejemplo para 28 -> 11100
        createExampleTable(exampleTableLayout)

        // Explicación breve de LSB y MSB para niños
                lsbMsbExplanation.text = """
            ¿Qué son LSB y MSB?
            • LSB (el bit más pequeño): Es el último número binario que obtienes. En este caso vale 2⁰, o sea 1.
            • MSB (el bit más grande): Es el primer número binario que obtienes. En este caso vale 2⁴, o sea 16.
        """.trimIndent()
    }

    private fun applyBoldToTitle(text: String, title: String): SpannableString {
        val spannable = SpannableString(text)
        val titleStart = text.indexOf(title)
        if (titleStart >= 0) {
            val titleEnd = titleStart + title.length
            spannable[titleStart..titleEnd] = StyleSpan(android.graphics.Typeface.BOLD)
        }
        return spannable
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