package ovh.gabrielhuav.battlenaval_v2.codigoascii

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R

class AsciiIntroductionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ascii_introduction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val contentTextView1 = view.findViewById<TextView>(R.id.contentTextView1)
        val contentTextView2 = view.findViewById<TextView>(R.id.contentTextView2)
        val illustrationImageView = view.findViewById<ImageView>(R.id.illustrationImageView)

        titleTextView.text = "¿Qué es el Código ASCII?"

        contentTextView1.text = """
            ASCII (American Standard Code for Information Interchange) es un estándar de codificación de caracteres que asigna valores numéricos a letras, números, signos de puntuación y otros caracteres.
            
            Desarrollado en la década de 1960, ASCII fue uno de los primeros estándares que permitió a las computadoras intercambiar información de texto de manera consistente.
        """.trimIndent()

        // Establecer imagen ilustrativa (ahora entre los dos textos)
        illustrationImageView.setImageResource(R.drawable.ascii_illustration)

        contentTextView2.text = """
            Características principales:
            
            • Utiliza 7 bits para representar cada carácter, permitiendo 128 valores posibles (del 0 al 127)
            • Los primeros 32 valores (0-31) son caracteres de control (no imprimibles) como salto de línea o retorno de carro
            • Los valores 32-126 representan caracteres imprimibles como letras, números y símbolos
            • El ASCII extendido usa 8 bits (añadiendo un bit más), permitiendo 256 valores posibles
            
            El código ASCII fue fundamental para el desarrollo de la computación moderna, aunque actualmente ha sido ampliamente reemplazado por Unicode (que incluye ASCII como su primer bloque) para soportar caracteres de todos los idiomas del mundo.
            
            Sin embargo, entender ASCII sigue siendo importante para comprender los fundamentos de la representación de texto en computadoras y la relación entre caracteres y sus equivalentes numéricos y binarios.
        """.trimIndent()
    }
}