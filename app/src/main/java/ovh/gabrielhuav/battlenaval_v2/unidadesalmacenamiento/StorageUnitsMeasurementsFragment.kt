package ovh.gabrielhuav.battlenaval_v2.unidadesalmacenamiento

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R

class StorageUnitsMeasurementsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_storage_units_measurements, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val contentTextView = view.findViewById<TextView>(R.id.contentTextView)
        val illustrationImage1 = view.findViewById<ImageView>(R.id.illustrationImage1)
        val illustrationImage2 = view.findViewById<ImageView>(R.id.illustrationImage2)

        titleTextView.text = "Medición de Fenómenos Físicos y Digitales"

        contentTextView.text = """
            Así como medimos fenómenos físicos con unidades específicas, también necesitamos unidades especializadas para medir fenómenos digitales.
            
            En el mundo físico:
            
            • Distancia: metros, kilómetros, millas
            • Tiempo: segundos, minutos, horas
            • Peso: gramos, kilogramos, toneladas
            • Temperatura: grados Celsius, Fahrenheit, Kelvin
            • Volumen: litros, galones, metros cúbicos
            
            Estas unidades nos ayudan a cuantificar y comparar magnitudes físicas de manera precisa.
            
            De manera similar, en el mundo digital necesitamos unidades para medir:
            
            • Almacenamiento: bits, bytes, kilobytes, etc.
            • Velocidad de transmisión: bits por segundo (bps), megabits por segundo (Mbps)
            • Frecuencia de procesamiento: hercios (Hz), gigahercios (GHz)
            • Resolución de pantalla: píxeles, megapíxeles
            • Calidad de audio: bits de profundidad, frecuencia de muestreo (kHz)
            
            Relación entre mediciones analógicas y digitales:
            
            Muchos dispositivos modernos tienen tanto representaciones analógicas como digitales. Por ejemplo, un reloj puede mostrar la hora con manecillas (analógico) o con números digitales. Un termómetro puede usar una columna de mercurio (analógico) o una pantalla LED (digital).
            
            En estos casos, aunque la representación visual cambia, la magnitud subyacente que se mide sigue siendo la misma. La diferencia está en cómo se visualiza y procesa la información.
        """.trimIndent()

        // Establecer imágenes ilustrativas
        illustrationImage1.setImageResource(R.drawable.physical_measurements)
        illustrationImage2.setImageResource(R.drawable.digital_measurements)
    }
}