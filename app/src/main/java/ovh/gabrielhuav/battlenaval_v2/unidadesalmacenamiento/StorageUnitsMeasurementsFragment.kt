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

class StorageUnitsMeasurementsFragment : Fragment() {

    // Vistas para la sección expandible 1 (Vida Diaria)
    private lateinit var expandableCardView1: CardView
    private lateinit var expandableHeaderLayout1: LinearLayout
    private lateinit var expandableHeaderText1: TextView
    private lateinit var expandArrowImage1: ImageView
    private lateinit var expandableContentLayout1: LinearLayout
    private lateinit var expandableContent1: TextView
    private lateinit var illustrationImage1: ImageView

    // Vistas para la sección expandible 2 (Mundo Digital)
    private lateinit var expandableCardView2: CardView
    private lateinit var expandableHeaderLayout2: LinearLayout
    private lateinit var expandableHeaderText2: TextView
    private lateinit var expandArrowImage2: ImageView
    private lateinit var expandableContentLayout2: LinearLayout
    private lateinit var expandableContent2: TextView
    private lateinit var illustrationImage2: ImageView

    // Estados de expansión
    private var isExpanded1 = false
    private var isExpanded2 = false

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

        // Inicializar las vistas expandibles de la sección 1 (Vida Diaria)
        expandableCardView1 = view.findViewById(R.id.expandableCardView1)
        expandableHeaderLayout1 = view.findViewById(R.id.expandableHeaderLayout1)
        expandableHeaderText1 = view.findViewById(R.id.expandableHeaderText1)
        expandArrowImage1 = view.findViewById(R.id.expandArrowImage1)
        expandableContentLayout1 = view.findViewById(R.id.expandableContentLayout1)
        expandableContent1 = view.findViewById(R.id.expandableContent1)
        illustrationImage1 = view.findViewById(R.id.illustrationImage1)

        // Inicializar las vistas expandibles de la sección 2 (Mundo Digital)
        expandableCardView2 = view.findViewById(R.id.expandableCardView2)
        expandableHeaderLayout2 = view.findViewById(R.id.expandableHeaderLayout2)
        expandableHeaderText2 = view.findViewById(R.id.expandableHeaderText2)
        expandArrowImage2 = view.findViewById(R.id.expandArrowImage2)
        expandableContentLayout2 = view.findViewById(R.id.expandableContentLayout2)
        expandableContent2 = view.findViewById(R.id.expandableContent2)
        illustrationImage2 = view.findViewById(R.id.illustrationImage2)

        titleTextView.text = "¿Cómo Medimos en el Mundo Real y Digital?"

        contentTextView.text = """
            Así como medimos cosas en la vida diaria, también necesitamos medir información en el mundo digital.
        """.trimIndent()

        // Configurar texto e imagen para la sección expandible 1 (Vida Diaria)
        expandableHeaderText1.text = "EN NUESTRA VIDA DIARIA MEDIMOS MUCHAS COSAS:"
        expandableContent1.text = """
            • Distancia: metros y kilómetros (para saber lo lejos que está algo)
            • Peso: gramos y kilogramos (para saber cuánto pesa algo)
            • Tiempo: segundos, minutos y horas (para saber cuánto dura algo)
            • Temperatura: grados Celsius (para saber si hace frío o calor)
            • Volumen: litros y mililitros (para saber cuánto líquido hay)
        """.trimIndent()
        illustrationImage1.setImageResource(R.drawable.physical_measurements)

        // Configurar texto e imagen para la sección expandible 2 (Mundo Digital)
        expandableHeaderText2.text = "EN EL MUNDO DIGITAL TAMBIÉN MEDIMOS COSAS:"
        expandableContent2.text = """
            • Almacenamiento: bytes, kilobytes, megabytes y gigabytes (para saber cuánta información podemos guardar)
            • Velocidad de internet: megabits por segundo (Mbps) (para saber qué tan rápido descargamos)
            • Velocidad de procesador: gigahercios (GHz) (para saber qué tan rápido funciona una computadora)
            • Calidad de imagen: megapíxeles (MP) (para saber qué tan nítida será una foto)
            • Calidad de sonido: bits y kilohercio (kHz) (para saber qué tan claro escucharemos)
            
            Estas medidas nos ayudan a comparar diferentes dispositivos y entender sus capacidades.
            
            Por ejemplo: Un celular con 128 GB de almacenamiento puede guardar el doble de fotos, 
            videos y aplicaciones que uno con 64 GB.
        """.trimIndent()
        illustrationImage2.setImageResource(R.drawable.digital_measurements)

        // Configurar el comportamiento expandible
        setupExpandableSections()
    }

    private fun setupExpandableSections() {
        // Inicialmente, ocultar los contenidos expandibles
        expandableContentLayout1.visibility = View.GONE
        expandableContentLayout2.visibility = View.GONE

        // Configurar listeners para expandir/contraer
        expandableHeaderLayout1.setOnClickListener {
            toggleExpand1()
        }

        expandableHeaderLayout2.setOnClickListener {
            toggleExpand2()
        }
    }

    private fun toggleExpand1() {
        isExpanded1 = !isExpanded1

        // Rotar la flecha según el estado
        val rotationAngle = if (isExpanded1) 180f else 0f
        val arrowAnimator = ValueAnimator.ofFloat(expandArrowImage1.rotation, rotationAngle)
        arrowAnimator.interpolator = AccelerateDecelerateInterpolator()
        arrowAnimator.duration = 300
        arrowAnimator.addUpdateListener { animation ->
            expandArrowImage1.rotation = animation.animatedValue as Float
        }
        arrowAnimator.start()

        // Mostrar u ocultar el contenido con animación simplificada
        if (isExpanded1) {
            // Simplemente hacemos visible el contenido
            expandableContentLayout1.visibility = View.VISIBLE
            expandableContentLayout1.alpha = 0f

            // Animar la transparencia para una transición suave
            val alphaAnimator = ValueAnimator.ofFloat(0f, 1f)
            alphaAnimator.duration = 300
            alphaAnimator.addUpdateListener { animation ->
                expandableContentLayout1.alpha = animation.animatedValue as Float
            }
            alphaAnimator.start()
        } else {
            // Animar la transparencia hacia 0 y luego ocultar
            val alphaAnimator = ValueAnimator.ofFloat(1f, 0f)
            alphaAnimator.duration = 300
            alphaAnimator.addUpdateListener { animation ->
                expandableContentLayout1.alpha = animation.animatedValue as Float
                if (animation.animatedFraction == 1.0f) {
                    expandableContentLayout1.visibility = View.GONE
                }
            }
            alphaAnimator.start()
        }
    }

    private fun toggleExpand2() {
        isExpanded2 = !isExpanded2

        // Rotar la flecha según el estado
        val rotationAngle = if (isExpanded2) 180f else 0f
        val arrowAnimator = ValueAnimator.ofFloat(expandArrowImage2.rotation, rotationAngle)
        arrowAnimator.interpolator = AccelerateDecelerateInterpolator()
        arrowAnimator.duration = 300
        arrowAnimator.addUpdateListener { animation ->
            expandArrowImage2.rotation = animation.animatedValue as Float
        }
        arrowAnimator.start()

        // Mostrar u ocultar el contenido con animación simplificada
        if (isExpanded2) {
            // Simplemente hacemos visible el contenido
            expandableContentLayout2.visibility = View.VISIBLE
            expandableContentLayout2.alpha = 0f

            // Animar la transparencia para una transición suave
            val alphaAnimator = ValueAnimator.ofFloat(0f, 1f)
            alphaAnimator.duration = 300
            alphaAnimator.addUpdateListener { animation ->
                expandableContentLayout2.alpha = animation.animatedValue as Float
            }
            alphaAnimator.start()
        } else {
            // Animar la transparencia hacia 0 y luego ocultar
            val alphaAnimator = ValueAnimator.ofFloat(1f, 0f)
            alphaAnimator.duration = 300
            alphaAnimator.addUpdateListener { animation ->
                expandableContentLayout2.alpha = animation.animatedValue as Float
                if (animation.animatedFraction == 1.0f) {
                    expandableContentLayout2.visibility = View.GONE
                }
            }
            alphaAnimator.start()
        }
    }
}