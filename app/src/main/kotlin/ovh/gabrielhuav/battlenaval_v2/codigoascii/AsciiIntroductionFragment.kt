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
            El código ASCII (American Standard Code for Information Interchange) es un sistema de codificación que permite a las computadoras representar caracteres (letras, números, símbolos) utilizando números.  Es como una tabla de equivalencias universal para que diferentes dispositivos puedan "entenderse".

            En esencia, ASCII asigna un valor numérico a cada carácter.  Por ejemplo:
            • La letra "A" mayúscula corresponde al número decimal 65.
            • El dígito "5" está representado por el número 53.
            • El símbolo de exclamación "!" tiene asignado el número 33.

            Cuando escribes algo en tu teclado, la computadora no guarda las letras directamente.  En su lugar, almacena los números ASCII correspondientes, que luego utiliza para mostrar el texto en la pantalla.
        """.trimIndent()

        // Establecer imagen ilustrativa (entre los dos textos)
        illustrationImageView.setImageResource(R.drawable.ascii_illustration)

        contentTextView2.text = """
            ¿Cómo funciona el código ASCII?

            • Cada carácter tiene un número asignado dentro del rango de 0 a 127.  Estos números se almacenan en binario (ceros y unos) dentro de la computadora.
            • Cuando la computadora necesita mostrar un texto, consulta la tabla ASCII para convertir esos números en los caracteres correspondientes.
            • Por ejemplo, para mostrar la palabra "HOLA", la computadora utilizaría la secuencia de números ASCII: 72 (H), 79 (O), 76 (L), 65 (A).

            Es importante saber que ASCII tiene sus limitaciones.  Solo puede representar un número limitado de caracteres.  Por eso, se desarrolló Unicode, un estándar mucho más amplio que incluye caracteres de prácticamente todos los idiomas del mundo, permitiendo una mayor compatibilidad y representación de textos diversos.  Pero ASCII sentó las bases para la comunicación digital que usamos hoy en día.
        """.trimIndent()
    }
}