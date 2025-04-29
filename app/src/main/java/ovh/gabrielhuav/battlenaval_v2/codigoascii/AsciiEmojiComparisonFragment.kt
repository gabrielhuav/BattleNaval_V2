package ovh.gabrielhuav.battlenaval_v2.codigoascii

import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R

class AsciiEmojiComparisonFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ascii_emoji_comparison, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val contentTextView = view.findViewById<TextView>(R.id.contentTextView)
        val appleEmojiImage = view.findViewById<ImageView>(R.id.appleEmojiImage)
        val androidEmojiImage = view.findViewById<ImageView>(R.id.androidEmojiImage)
        val windowsEmojiImage = view.findViewById<ImageView>(R.id.windowsEmojiImage)
        val unicodeTableImage = view.findViewById<ImageView>(R.id.unicodeTableImage)

        titleTextView.text = "¿Cómo Funciona el Código ASCII?"

        // Texto con títulos en Markdown
        val rawText = """
            Ya hablamos de ASCII: un sistema que asigna números a letras, números y símbolos básicos.  Pero, ¿por qué es importante?

            Piensa en los emojis.  Cuando envías un emoji a un amigo, ¿cómo sabe su teléfono qué carita mostrar?  Bueno, hay un "código" detrás de cada emoji.

            **ASCII: El Primer "Código" Digital**

            ASCII hacía algo similar, pero mucho más simple.  En lugar de emojis, asignaba un número a cada letra y símbolo.  Por ejemplo, la letra "A" era el número 65, y la letra "B" era el 66.

            **¿Por qué es importante tener un código?**

            Porque las computadoras no entienden letras directamente, solo entienden números.  ASCII permitía que las computadoras tradujeran esos números a letras en la pantalla, o que enviaran mensajes de texto a través de la red.

            **Los Emojis: ASCII, Pero Visual.**

            Piensa en los emojis como una versión moderna y mucho más elaborada de ASCII.  Cada emoji tiene un número (en Unicode, que es como un ASCII gigante), y tu teléfono usa ese número para mostrar el dibujo correcto.

            **¿Por qué los emojis se ven diferentes en cada teléfono?**

            Aquí es donde se pone interesante.  Aunque el *código* del emoji es el mismo en todos los teléfonos, cada compañía (Apple, Google, etc.) tiene su propio "estilo" para dibujar ese emoji.  Es como si todos estuvieran cantando la misma canción, pero cada uno con su propia voz.

            **¿Cómo se realciona el Código ASCII y los Emojis?**

            Tanto ASCII como los emojis (gracias a Unicode) nos enseñan que las computadoras necesitan un "código" para entender y mostrar información.  ASCII fue el primer paso, y los emojis son una prueba de lo lejos que hemos llegado.  ¡Ahora, en lugar de solo letras, podemos enviar caritas, animales y hasta comida virtual gracias a estos sistemas de codificación!
        """.trimIndent()

        // Procesar el texto para aplicar negritas a los títulos
        val spannableStringBuilder = SpannableStringBuilder()
        val regex = Regex("\\*\\*(.*?)\\*\\*")

        var lastIndex = 0
        regex.findAll(rawText).forEach { matchResult ->
            // Append text before the title
            spannableStringBuilder.append(rawText.substring(lastIndex, matchResult.range.first))

            // Extract title and apply bold style
            val title = matchResult.groupValues[1]
            val spannableTitle = SpannableString(title)
            spannableTitle.setSpan(StyleSpan(Typeface.BOLD), 0, title.length, 0)
            spannableStringBuilder.append(spannableTitle)

            lastIndex = matchResult.range.last + 1
        }

        // Append any remaining text after the last title
        spannableStringBuilder.append(rawText.substring(lastIndex))

        contentTextView.text = spannableStringBuilder

        // Establecer imágenes de emojis en diferentes plataformas
        appleEmojiImage.setImageResource(R.drawable.emoji_apple)
        androidEmojiImage.setImageResource(R.drawable.emoji_android)
        windowsEmojiImage.setImageResource(R.drawable.emoji_windows)
        unicodeTableImage.setImageResource(R.drawable.unicode_emoji_table)
    }
}