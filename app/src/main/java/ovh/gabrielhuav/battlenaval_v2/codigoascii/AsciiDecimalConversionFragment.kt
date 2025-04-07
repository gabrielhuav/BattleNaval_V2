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

class AsciiDecimalConversionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ascii_decimal_conversion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val contentTextView = view.findViewById<TextView>(R.id.contentTextView)
        val exampleTableLayout = view.findViewById<TableLayout>(R.id.exampleTableLayout)
        val illustrationImageView = view.findViewById<ImageView>(R.id.illustrationImageView)

        titleTextView.text = "Conversión ASCII a Decimal"

        contentTextView.text = """
            La conversión entre caracteres ASCII y valores decimales es directa, ya que cada carácter ASCII tiene asignado un único valor numérico.
            
            Para convertir un carácter ASCII a su valor decimal:
            
            1. Simplemente busca el valor asociado al carácter en la tabla ASCII
            
            Para convertir un valor decimal a su carácter ASCII:
            
            1. Busca el carácter asociado al valor decimal en la tabla ASCII
            
            Estas conversiones son esenciales para entender cómo las computadoras procesan texto. Internamente, cuando escribes "Hola", la computadora almacena los valores 72, 111, 108, 97.
            
            Algunos patrones útiles que puedes recordar:
            
            • Las letras mayúsculas (A-Z) ocupan los valores 65-90
            • Las letras minúsculas (a-z) ocupan los valores 97-122
            • Los dígitos (0-9) ocupan los valores 48-57
            • La diferencia entre una letra mayúscula y su minúscula es 32
            
            Una característica importante es que los dígitos como caracteres (ej: '5') tienen valores ASCII diferentes a su valor numérico real (53 vs 5). Esta distinción es crucial para entender la manipulación de texto en programación.
            
            A continuación se muestran algunos ejemplos de caracteres ASCII y sus valores decimales:
        """.trimIndent()

        // Crear ejemplos en la tabla
        createExampleTable(exampleTableLayout)

        // Establecer imagen ilustrativa
        illustrationImageView.setImageResource(R.drawable.ascii_decimal_conversion)
    }

    private fun createExampleTable(tableLayout: TableLayout) {
        // Crear encabezados
        val headerRow = TableRow(requireContext())
        listOf("Carácter", "Valor ASCII (Decimal)", "Notas").forEach { header ->
            val textView = TextView(requireContext()).apply {
                text = header
                setPadding(8, 8, 8, 8)
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_700))
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                textSize = 16f
            }
            headerRow.addView(textView)
        }
        tableLayout.addView(headerRow)

        // Datos de ejemplo
        val exampleData = listOf(
            Triple("A", 65, "Primera letra mayúscula"),
            Triple("Z", 90, "Última letra mayúscula"),
            Triple("a", 97, "Primera letra minúscula"),
            Triple("z", 122, "Última letra minúscula"),
            Triple("0", 48, "Primer dígito (carácter)"),
            Triple("9", 57, "Último dígito (carácter)"),
            Triple("Espacio", 32, "Carácter de espacio"),
            Triple("Enter", 13, "Retorno de carro (CR)"),
            Triple("Tab", 9, "Tabulador")
        )

        // Agregar filas de datos
        exampleData.forEach { (char, decimal, notes) ->
            val row = TableRow(requireContext())

            // Carácter
            row.addView(createTableCell(char))

            // Valor decimal
            row.addView(createTableCell(decimal.toString()))

            // Notas
            row.addView(createTableCell(notes))

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