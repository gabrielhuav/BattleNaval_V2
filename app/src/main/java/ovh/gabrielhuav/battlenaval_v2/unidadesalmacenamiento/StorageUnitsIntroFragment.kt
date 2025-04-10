package ovh.gabrielhuav.battlenaval_v2.unidadesalmacenamiento

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R

class StorageUnitsIntroFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_storage_units_intro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val contentTextView = view.findViewById<TextView>(R.id.contentTextView)
        val illustrationImageView = view.findViewById<ImageView>(R.id.illustrationImageView)

        titleTextView.text = "Unidades de Medida de Almacenamiento"

        contentTextView.text = """
            Las unidades de medida de almacenamiento son estándares utilizados para cuantificar la capacidad de almacenamiento de datos en dispositivos informáticos.
            
            Así como utilizamos metros para medir distancias o kilogramos para medir peso, en informática necesitamos unidades especiales para medir la cantidad de información que podemos almacenar.
            
            Estas unidades son fundamentales en la informática porque:
            
            • Permiten medir de manera precisa la capacidad de almacenamiento digital
            • Facilitan la comparación entre diferentes dispositivos y tecnologías
            • Ayudan a determinar requisitos de espacio para programas y archivos
            • Son esenciales para planificar infraestructuras tecnológicas
            
            Al igual que las medidas físicas tienen unidades básicas (como el metro o el gramo) y unidades derivadas (como el kilómetro o el kilogramo), las medidas de almacenamiento tienen su propia jerarquía basada en potencias de 2, debido a la naturaleza binaria de las computadoras.
            
            En las siguientes secciones, exploraremos las diferentes unidades de medida de almacenamiento, cómo se relacionan entre sí, y cómo interpretar estas unidades en el contexto de dispositivos y tecnologías actuales.
        """.trimIndent()

        // Establecer imagen ilustrativa
        illustrationImageView.setImageResource(R.drawable.storage_units_intro)
    }
}