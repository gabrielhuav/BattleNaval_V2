package ovh.gabrielhuav.battlenaval_v2.codigoascii

import android.os.Bundle
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

        titleTextView.text = "Código ASCII y su Aplicación"

        contentTextView.text = """
            ASCII fue la base para la comunicación de datos y representación de texto en computadoras, pero su alcance era limitado a un conjunto básico de caracteres.
            
            Aplicaciones prácticas del código ASCII:
            
            • Comunicaciones: Transmisión de mensajes entre computadoras usando códigos numéricos estándar
            • Desarrollo de software: Base para manipulación y procesamiento de texto
            • Almacenamiento de datos: Representación eficiente de texto en memoria
            • Procesos de entrada/salida: Interpretación de pulsaciones de teclado y visualización en pantalla
            
            La evolución de ASCII:
            
            El código ASCII, con sus 128 caracteres, era suficiente para el inglés, pero inadecuado para otros idiomas y símbolos. Esto llevó a la creación de estándares más amplios:
            
            1. ASCII Extendido: Amplió a 256 caracteres usando 8 bits
            2. ISO-8859: Familia de conjuntos de caracteres para diferentes idiomas
            3. Unicode: Estándar universal que puede representar prácticamente cualquier caracter de cualquier idioma
            
            El ejemplo más visible de esta evolución son los emojis modernos. Mientras que ASCII solo podía crear emoticones simples como ":)" o ":(", Unicode permite emojis completos con diferentes interpretaciones visuales según la plataforma.
            
            Esta evolución ilustra un principio fundamental en informática: la separación entre el código (representación numérica de un carácter) y su visualización (cómo se muestra al usuario).
        """.trimIndent()

        // Establecer imágenes de emojis en diferentes plataformas
        appleEmojiImage.setImageResource(R.drawable.emoji_apple)
        androidEmojiImage.setImageResource(R.drawable.emoji_android)
        windowsEmojiImage.setImageResource(R.drawable.emoji_windows)
        unicodeTableImage.setImageResource(R.drawable.unicode_emoji_table)
    }
}