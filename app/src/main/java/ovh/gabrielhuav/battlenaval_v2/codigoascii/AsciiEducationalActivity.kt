package ovh.gabrielhuav.battlenaval_v2.codigoascii

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import ovh.gabrielhuav.battlenaval_v2.R
import ovh.gabrielhuav.battlenaval_v2.ThemeManager

class AsciiEducationalActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var indicatorLayout: LinearLayout
    private lateinit var btnBack: Button
    private lateinit var btnHome: Button
    private lateinit var indicators: Array<ImageView>
    private val fragments = listOf<Fragment>(
        AsciiIntroductionFragment(),
        AsciiEmojiComparisonFragment(),
        AsciiTableFragment(),
        AsciiBinaryConversionFragment(),
        AsciiDecimalConversionFragment(),
        AsciiConverterFragment()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicar tema antes de inflar la vista
        ThemeManager.applyTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ascii_educational)

        // Inicializar vistas
        viewPager = findViewById(R.id.viewPager)
        indicatorLayout = findViewById(R.id.indicatorLayout)
        btnBack = findViewById(R.id.btnBack)
        btnHome = findViewById(R.id.btnHome)

        // Configurar ViewPager
        val pagerAdapter = AsciiEducationalPagerAdapter(this, fragments)
        viewPager.adapter = pagerAdapter

        // Crear indicadores
        createIndicators()

        // Listener para cambio de página
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
            }
        })

        // Configurar botones
        btnBack.setOnClickListener {
            onBackPressed()
        }

        btnHome.setOnClickListener {
            finish()
        }
    }

    private fun createIndicators() {
        indicators = Array(fragments.size) {
            ImageView(this).apply {
                setPadding(8, 0, 8, 0)
                setImageResource(R.drawable.indicator_inactive)
            }
        }

        indicators.forEach { indicator ->
            indicatorLayout.addView(indicator)
        }

        // Activar el primer indicador
        indicators[0].setImageResource(R.drawable.indicator_active)
    }

    private fun updateIndicators(position: Int) {
        for (i in indicators.indices) {
            indicators[i].setImageResource(
                if (i == position) R.drawable.indicator_active
                else R.drawable.indicator_inactive
            )
        }
    }
}