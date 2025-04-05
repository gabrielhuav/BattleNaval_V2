package ovh.gabrielhuav.battlenaval_v2.sistemabinario

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import ovh.gabrielhuav.battlenaval_v2.R
import ovh.gabrielhuav.battlenaval_v2.ThemeManager

/**
 * Actividad que contiene los juegos educativos sobre sistema binario
 */
class GamesActivity : AppCompatActivity() {

    private lateinit var navigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicar tema antes de inflar la vista
        ThemeManager.applyTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games)

        // Configurar action bar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.games_challenges_title)
        }

        // Inicializar componentes UI
        navigationView = findViewById(R.id.bottom_navigation)

        // Configurar listener de navegación
        navigationView.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null

            when (item.itemId) {
                R.id.nav_switches -> selectedFragment = SwitchesGameFragment()
                R.id.nav_practice -> selectedFragment = PracticeFragment()
                R.id.nav_challenges -> selectedFragment = ChallengesFragment()
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment)
                return@setOnItemSelectedListener true
            }
            false
        }

        // Cargar fragmento predeterminado
        loadFragment(SwitchesGameFragment())
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}