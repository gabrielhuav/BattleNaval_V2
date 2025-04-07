package ovh.gabrielhuav.battlenaval_v2.sistemabinario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R

class NumerationSystemFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_numeration_system, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val contentTextView1 = view.findViewById<TextView>(R.id.contentTextView1)
        val contentTextView2 = view.findViewById<TextView>(R.id.contentTextView2)
        val illustrationImageView = view.findViewById<ImageView>(R.id.illustrationImageView)

        titleTextView.text = "¿Qué es un sistema de numeración?"

        contentTextView1.text = """
            Un sistema de numeración es como un juego especial para contar y escribir números.

            El sistema decimal, que usamos todos los días, usa 10 símbolos diferentes:
            0, 1, 2, 3, 4, 5, 6, 7, 8 y 9

            Con estos 10 símbolos podemos escribir cualquier número, ¡incluso los más grandes!
        """.trimIndent()

        contentTextView2.text = """
            Cada posición en un número tiene un valor especial:
            
            • Unidades: El dígito que está más a la derecha. Vale de 1 en 1.
            • Decenas: El segundo dígito de derecha a izquierda. Vale de 10 en 10.
            • Centenas: El tercer dígito de derecha a izquierda. Vale de 100 en 100.
            • Unidades de millar: El cuarto dígito. Vale de 1000 en 1000.
            
            Por ejemplo, en el número 2,345:
            • El 5 está en el lugar de las unidades (vale 5 × 1 = 5)
            • El 4 está en el lugar de las decenas (vale 4 × 10 = 40)
            • El 3 está en el lugar de las centenas (vale 3 × 100 = 300)
            • El 2 está en el lugar de las unidades de millar (vale 2 × 1000 = 2000)
            
            Al sumarlos: 2000 + 300 + 40 + 5 = 2,345
            
            En el sistema decimal, cada posición vale 10 veces más que la posición anterior.
        """.trimIndent()

        illustrationImageView.setImageResource(R.drawable.sistema_decimal)
    }
}