package ovh.gabrielhuav.battlenaval_v2.sistemabinario

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R

class BinarySwitchesFragment : Fragment() {

    private lateinit var switches: Array<Switch>
    private lateinit var switchTexts: Array<TextView>
    private lateinit var valueLabels: Array<TextView>
    private lateinit var resultTextView: TextView
    private val switchValues = intArrayOf(8, 4, 2, 1)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_binary_switches, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val instructionsTextView = view.findViewById<TextView>(R.id.instructionsTextView)
        resultTextView = view.findViewById(R.id.resultTextView)

        titleTextView.text = "¡Convierte de binario a decimal con los interruptores!"

        instructionsTextView.text = """
            Cada interruptor representa un bit en el sistema binario.
            
            Cuando activas un interruptor, estás sumando su valor al número decimal.
            
            ¡Prueba diferentes combinaciones y ve cómo cambia el valor decimal!
            
            Recuerda:
            - Texto VERDE (1) = sumamos su valor
            - Texto ROJO (0) = no sumamos nada
        """.trimIndent()

        // Inicializar switches
        switches = arrayOf(
            view.findViewById(R.id.switch8),
            view.findViewById(R.id.switch4),
            view.findViewById(R.id.switch2),
            view.findViewById(R.id.switch1)
        )

        // Inicializar textos de estado
        switchTexts = arrayOf(
            view.findViewById(R.id.text8),
            view.findViewById(R.id.text4),
            view.findViewById(R.id.text2),
            view.findViewById(R.id.text1)
        )

        // Inicializar etiquetas de valores
        valueLabels = arrayOf(
            view.findViewById(R.id.value8),
            view.findViewById(R.id.value4),
            view.findViewById(R.id.value2),
            view.findViewById(R.id.value1)
        )

        // Configurar switches y etiquetas
        for (i in switches.indices) {
            switchTexts[i].text = "0"
            switchTexts[i].setTextColor(Color.RED)
            valueLabels[i].text = switchValues[i].toString()

            switches[i].setOnCheckedChangeListener { _, isChecked ->
                switchTexts[i].text = if (isChecked) "1" else "0"
                switchTexts[i].setTextColor(if (isChecked) Color.GREEN else Color.RED)
                updateDecimalValue()
            }
        }

        // Inicializar valor decimal
        updateDecimalValue()
    }

    private fun updateDecimalValue() {
        var decimal = 0
        var binaryString = ""

        for (i in switches.indices) {
            if (switches[i].isChecked) {
                decimal += switchValues[i]
                binaryString += "1"
            } else {
                binaryString += "0"
            }
        }

        val result = "$binaryString = $decimal"
        resultTextView.text = result

        // Hacer una pequeña animación para mostrar el cambio
        resultTextView.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(200)
            .withEndAction {
                resultTextView.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }
}