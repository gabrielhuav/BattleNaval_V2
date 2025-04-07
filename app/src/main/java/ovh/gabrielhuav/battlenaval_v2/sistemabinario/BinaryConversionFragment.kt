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

class BinaryConversionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_binary_conversion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val contentTextView = view.findViewById<TextView>(R.id.contentTextView)
        val binaryTableLayout = view.findViewById<TableLayout>(R.id.binaryTableLayout)
        val exampleTableLayout = view.findViewById<TableLayout>(R.id.exampleTableLayout)
        val illustrationImageView = view.findViewById<ImageView>(R.id.illustrationImageView)

        titleTextView.text = "Convertir un número binario a decimal"

        contentTextView.text = """
            Convertir un número binario a decimal es como sumar el valor de las luces encendidas.

            Cada posición en un número binario tiene un valor especial:
            
            Imagina que tienes luces mágicas 💡 y cada una vale el doble que la anterior:
            
            La 1ª luz vale 1
            La 2ª luz vale 2
            La 3ª luz vale 4
            La 4ª luz vale 8
            Y así sucesivamente...
            
            Si la luz está encendida (1), sumamos su valor.
            Si la luz está apagada (0), no sumamos nada.
            
            Veamos un ejemplo con el número binario 1101:
            
            1 × 8 = 8
            1 × 4 = 4
            0 × 2 = 0
            1 × 1 = 1
            
            8 + 4 + 0 + 1 = 13
            
            ¡Por lo tanto, 1101 en binario es igual a 13 en decimal!
        """.trimIndent()

        // Crear tabla de valores binarios
        createBinaryTable(binaryTableLayout)

        // Crear tabla de ejemplo para 1101 -> 13
        createExampleTable(exampleTableLayout)

        illustrationImageView.setImageResource(R.drawable.tabla_binario)
    }

    private fun createBinaryTable(tableLayout: TableLayout) {
        // Encabezados
        val headerRow = TableRow(requireContext())
        headerRow.addView(createTextView("Posición", true))
        headerRow.addView(createTextView("Valor", true))
        tableLayout.addView(headerRow)

        // Datos
        val positions = listOf("8ª luz", "7ª luz", "6ª luz", "5ª luz",
            "4ª luz", "3ª luz", "2ª luz", "1ª luz")
        val values = listOf("128", "64", "32", "16", "8", "4", "2", "1")

        for (i in positions.indices) {
            val row = TableRow(requireContext())
            row.addView(createTextView(positions[i]))
            row.addView(createTextView(values[i]))
            tableLayout.addView(row)
        }
    }

    private fun createExampleTable(tableLayout: TableLayout) {
        // Encabezados
        val headerRow = TableRow(requireContext())
        val values = listOf("8", "4", "2", "1")
        for (value in values) {
            headerRow.addView(createTextView(value, true))
        }
        tableLayout.addView(headerRow)

        // Valores binarios
        val dataRow = TableRow(requireContext())
        val binaries = listOf("1", "1", "0", "1")
        for (binary in binaries) {
            dataRow.addView(createTextView(binary))
        }
        tableLayout.addView(dataRow)

        // Valores calculados
        val calculationRow = TableRow(requireContext())
        val calculations = listOf("1×8=8", "1×4=4", "0×2=0", "1×1=1")
        for (calculation in calculations) {
            calculationRow.addView(createTextView(calculation))
        }
        tableLayout.addView(calculationRow)

        // Total
        val totalRow = TableRow(requireContext())
        val totalCell = createTextView("Total: 8+4+0+1=13", true)
        totalCell.setPadding(16, 16, 16, 16)

        // Usar un TableRow.LayoutParams con span para que ocupe todas las columnas
        val layoutParams = TableRow.LayoutParams()
        layoutParams.span = 4  // Ocupar 4 columnas
        totalCell.layoutParams = layoutParams

        totalRow.addView(totalCell)
        tableLayout.addView(totalRow)
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