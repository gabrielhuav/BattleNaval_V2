package ovh.gabrielhuav.battlenaval_v2.codigoascii

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R

class AsciiConverterFragment : Fragment() {

    private lateinit var inputText: EditText
    private lateinit var decimalResult: TextView
    private lateinit var binaryResult: TextView
    private lateinit var hexResult: TextView
    private lateinit var clearButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ascii_converter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar vistas
        inputText = view.findViewById(R.id.inputText)
        decimalResult = view.findViewById(R.id.decimalResult)
        binaryResult = view.findViewById(R.id.binaryResult)
        hexResult = view.findViewById(R.id.hexResult)
        clearButton = view.findViewById(R.id.clearButton)

        // Configurar filtro para permitir solo caracteres ASCII válidos
        inputText.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            source.filter { it.code in 0..127 }
        })

        // Configurar TextWatcher para actualizar las conversiones en tiempo real
        inputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                updateConversions(s.toString())
            }
        })

        // Configurar botón de limpiar
        clearButton.setOnClickListener {
            inputText.setText("")
            clearResults()
        }

        // Inicializar resultados vacíos
        clearResults()
    }

    private fun updateConversions(input: String) {
        if (input.isEmpty()) {
            clearResults()
            return
        }

        val charArray = input.toCharArray()
        val decimalValues = charArray.map { it.code }
        val binaryValues = decimalValues.map { Integer.toBinaryString(it).padStart(8, '0') }
        val hexValues = decimalValues.map { String.format("%02X", it) }

        // Actualizar los resultados
        decimalResult.text = formatArray(decimalValues)
        binaryResult.text = formatArray(binaryValues)
        hexResult.text = formatArray(hexValues)
    }

    private fun clearResults() {
        decimalResult.text = "N/A"
        binaryResult.text = "N/A"
        hexResult.text = "N/A"
    }

    private fun <T> formatArray(list: List<T>): String {
        if (list.isEmpty()) return "N/A"

        return list.joinToString(", ")
    }
}