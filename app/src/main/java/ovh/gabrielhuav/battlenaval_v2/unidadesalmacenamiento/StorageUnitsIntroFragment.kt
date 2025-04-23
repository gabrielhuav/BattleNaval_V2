package ovh.gabrielhuav.battlenaval_v2.unidadesalmacenamiento

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R

class StorageUnitsIntroFragment : Fragment() {

    // Vistas para la sección expandible
    private lateinit var expandableCardView: CardView
    private lateinit var expandableHeaderLayout: LinearLayout
    private lateinit var expandableHeaderText: TextView
    private lateinit var expandArrowImage: ImageView
    private lateinit var expandableContent: TextView

    // Estado de expansión
    private var isExpanded = false

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

        // Inicializar las vistas expandibles
        expandableCardView = view.findViewById(R.id.expandableCardView)
        expandableHeaderLayout = view.findViewById(R.id.expandableHeaderLayout)
        expandableHeaderText = view.findViewById(R.id.expandableHeaderText)
        expandArrowImage = view.findViewById(R.id.expandArrowImage)
        expandableContent = view.findViewById(R.id.expandableContent)

        titleTextView.text = "¿Qué son las Unidades de Medida de Almacenamiento?"

        contentTextView.text = """
            ¿Te has preguntado alguna vez cómo saber cuántas fotos, videos, juegos o aplicaciones puedes guardar en tu celular o computadora?
            
            Para eso existen las unidades de medida de almacenamiento, que son como "medidores" que nos ayudan a saber cuánta información cabe en nuestros dispositivos digitales.
            
            Así como usamos:
            • Metros y kilómetros para medir distancias
            • Kilogramos para medir el peso
            • Litros para medir líquidos
            
            En el mundo digital usamos:
            • Bytes, kilobytes, megabytes y gigabytes para medir la cantidad de información
            
            Estas unidades nos ayudan a:
            • Saber cuántos archivos podemos guardar en nuestros dispositivos
            • Entender cuánto espacio ocupa una aplicación o juego
            • Decidir qué celular o computadora comprar según nuestras necesidades
            • Saber si podemos descargar un archivo o si ocupará demasiado espacio
        """.trimIndent()

        // Configurar el texto de la sección expandible
        expandableHeaderText.text = "¿SABÍAS QUE...?"
        expandableContent.text = """
            • Un mensaje de texto simple ocupa apenas unos bytes
            • Una foto típica de celular ocupa entre 2 y 5 megabytes
            • Un video de 1 minuto en HD puede ocupar 100 megabytes
            • Un juego para celular puede ocupar desde 50 megabytes hasta varios gigabytes
            • La memoria de un celular moderno puede ser de 64 o 128 gigabytes
            
            Imagina que tu celular es como una mochila donde guardas cosas:
            • Los mensajes de texto serían como clips pequeños
            • Las fotos serían como cuadernos
            • Los videos serían como libros gruesos
            • Los juegos serían como loncheras grandes
            
            ¡Las unidades de almacenamiento nos ayudan a saber cuánto cabe en nuestra "mochila digital"!
        """.trimIndent()

        // Establecer imagen ilustrativa
        illustrationImageView.setImageResource(R.drawable.storage_units_intro)

        // Configurar el comportamiento expandible
        setupExpandableSection()
    }

    private fun setupExpandableSection() {
        // Inicialmente, ocultar el contenido expandible pero mantener la sección visible
        expandableContent.visibility = View.GONE

        // Configurar el listener para expandir/contraer
        expandableHeaderLayout.setOnClickListener {
            toggleExpand()
        }
    }

    private fun toggleExpand() {
        isExpanded = !isExpanded

        // Rotar la flecha según el estado (180 grados si está expandido)
        val rotationAngle = if (isExpanded) 180f else 0f
        val arrowAnimator = ValueAnimator.ofFloat(expandArrowImage.rotation, rotationAngle)
        arrowAnimator.interpolator = AccelerateDecelerateInterpolator()
        arrowAnimator.duration = 300
        arrowAnimator.addUpdateListener { animation ->
            expandArrowImage.rotation = animation.animatedValue as Float
        }
        arrowAnimator.start()

        // Mostrar u ocultar el contenido con animación
        if (isExpanded) {
            // Primero hacer visible el contenido con altura 0
            expandableContent.visibility = View.VISIBLE
            expandableContent.alpha = 0f
            expandableContent.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

            // Animar la altura desde 0 hasta la altura completa
            val heightAnimator = ValueAnimator.ofInt(0, expandableContent.measuredHeight)
            heightAnimator.interpolator = AccelerateDecelerateInterpolator()
            heightAnimator.duration = 300
            heightAnimator.addUpdateListener { animation ->
                val params = expandableContent.layoutParams
                params.height = animation.animatedValue as Int
                expandableContent.layoutParams = params

                // Animar la transparencia gradualmente
                expandableContent.alpha = animation.animatedFraction
            }
            heightAnimator.start()
        } else {
            // Animar la altura desde la altura actual hasta 0
            val heightAnimator = ValueAnimator.ofInt(expandableContent.height, 0)
            heightAnimator.interpolator = AccelerateDecelerateInterpolator()
            heightAnimator.duration = 300
            heightAnimator.addUpdateListener { animation ->
                val params = expandableContent.layoutParams
                params.height = animation.animatedValue as Int
                expandableContent.layoutParams = params

                // Animar la transparencia gradualmente
                expandableContent.alpha = 1f - animation.animatedFraction

                // Cuando la animación termina, ocultar completamente el contenido
                if (animation.animatedFraction == 1.0f) {
                    expandableContent.visibility = View.GONE

                    // Restaurar la altura a WRAP_CONTENT para futuras expansiones
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    expandableContent.layoutParams = params
                }
            }
            heightAnimator.start()
        }
    }
}