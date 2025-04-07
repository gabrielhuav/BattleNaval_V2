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

class AsciiBinaryConversionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ascii_binary_conversion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val contentTextView = view.findViewById<TextView>(R.id.contentTextView)
        val exampleTableLayout = view.findViewById<TableLayout>(R.id.exampleTableLayout)
        val illustrationImageView = view.findViewById<ImageView>(R.id.illustrationImageView)

        titleTextView.text = "Conversión ASCII a Binario"

        contentTextView.text = """
            Convertir caracteres ASCII a binario es un proceso sencillo, ya que cada carácter ASCII tiene un valor numérico asignado entre 0 y 127.
            
            Para convertir un carácter ASCII a binario:
            
            1. Identificar el valor decimal del carácter en la tabla ASCII
            2. Convertir ese valor decimal a su representación binaria
            3. Asegurarse de que el resultado tenga 7 bits (añadiendo ceros a la izquierda si es necesario)
            
            Por ejemplo, para convertir la letra 'A' a binario:
            
            1. El valor ASCII de 'A' es 65
            2. 65 en binario es 1000001
            3. Como ya tiene 7 bits, no es necesario añadir ceros
            
            Otro ejemplo, para convertir '5' a binario:
            
            1. El valor ASCII del carácter '5' es 53
            2. 53 en binario es 110101
            3. Añadimos un cero a la izquierda para tener 7 bits: 0110101
            
            Esta conversión es fundamental en la comunicación digital, ya que las computadoras almacenan y transmiten todos los datos en forma binaria.
            
            A continuación se muestran algunos ejemplos de caracteres ASCII convertidos a binario:
        """.trimIndent()

        // Crear ejemplos en la tabla
        createExampleTable(exampleTableLayout)

        // Establecer imagen ilustrativa
        illustrationImageView.setImageResource(R.drawable.ascii_binary_conversion)
    }

    private fun createExampleTable(tableLayout: TableLayout) {
        // Crear encabezados
        val headerRow = TableRow(requireContext())
        listOf("Carácter", "ASCII (Dec)", "Binario (7 bits)").forEach { header ->
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

        // Datos de ejemplo
        val exampleData = listOf(
            Triple("A", 65, "1000001"),
            Triple("B", 66, "1000010"),
            Triple("a", 97, "1100001"),
            Triple("1", 49, "0110001"),
            Triple("@", 64, "1000000"),
            Triple("Espacio", 32, "0100000"),
            Triple("!", 33, "0100001")
        )

        // Agregar filas de datos
        exampleData.forEach { (char, decimal, binary) ->
            val row = TableRow(requireContext())

            // Carácter
            row.addView(createTableCell(char))

            // Valor decimal
            row.addView(createTableCell(decimal.toString()))

            // Valor binario
            row.addView(createTableCell(binary))

            tableLayout.addView(row)
        }
    }

    private fun createTableCell(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            setPadding(16, 12, 16, 12)
            textSize = 14f
            textAlignment = View.TEXT_ALIGNMENT_CENTER
        }
    }
}