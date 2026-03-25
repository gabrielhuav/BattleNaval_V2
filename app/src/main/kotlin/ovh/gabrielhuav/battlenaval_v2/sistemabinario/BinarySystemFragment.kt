package ovh.gabrielhuav.battlenaval_v2.sistemabinario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R

class BinarySystemFragment : Fragment() {

    private lateinit var lightSwitch: Switch
    private lateinit var lightImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_binary_system, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val contentTextView = view.findViewById<TextView>(R.id.contentTextView)
        val illustrationImageView = view.findViewById<ImageView>(R.id.illustrationImageView)
        lightSwitch = view.findViewById(R.id.lightSwitch)
        lightImage = view.findViewById(R.id.lightImage)

        titleTextView.text = "¿Qué es el sistema binario?"

        contentTextView.text = """
            El sistema binario, conocido porque es el sistema que utilizan las 
            computadoras y el resto de dispositivos electrónicos como teléfonos o celulares, es un sistema de base 2. 
            
            Eso significa que es un sistema que solo utiliza dos números para representar todos 
            sus números y en el caso del código binario estas dos números son el 0 y el 1. 
            
            Los ordenadores utilizan el sistema binario porque solo trabajan con dos niveles de voltaje: 
            apagado o sin luz (0) y encendido o con luz (1).
            
            Prueba el interruptor de abajo para ver cómo funciona el sistema binario:
        """.trimIndent()

        // Configurar el interruptor
        lightSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                lightImage.setImageResource(R.drawable.light_bulb_on)
                lightSwitch.text = "1 - Encendido"
            } else {
                lightImage.setImageResource(R.drawable.light_bulb_off)
                lightSwitch.text = "0 - Apagado"
            }
        }

        // Estado inicial
        lightSwitch.isChecked = false
        lightSwitch.text = "0 - Apagado"
        lightImage.setImageResource(R.drawable.light_bulb_off)

        illustrationImageView.setImageResource(R.drawable.sistema_binario)
    }
}