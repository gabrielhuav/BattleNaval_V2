package ovh.gabrielhuav.battlenaval_v2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import ovh.gabrielhuav.battlenaval_v2.sistemabinario.EducationalActivity
import ovh.gabrielhuav.battlenaval_v2.battlenavalbinario.BluetoothActivity
import ovh.gabrielhuav.battlenaval_v2.battlenavalbinario.SinglePlayerActivity
import ovh.gabrielhuav.battlenaval_v2.sistemabinario.GamesActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnLearn: Button
    private lateinit var btnGames: Button
    private lateinit var btnSettings: Button

    // Botones de juegos (se mostrarán/ocultarán)
    private lateinit var layoutGamesSubMenu: LinearLayout
    private lateinit var btnBattleNaval: Button
    private lateinit var btnBattleNavalSinglePlayer: Button
    private lateinit var btnBattleNavalBluetooth: Button
    private lateinit var btnBinaryGames: Button

    // Layout para opciones de Batalla Naval (se mostrará/ocultará)
    private lateinit var layoutBattleNavalOptions: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicar tema antes de inflar la vista
        val themeId = ThemeManager.getTheme(this)
        when (themeId) {
            ThemeManager.THEME_ESCOM -> setTheme(R.style.Theme_BattleNaval_V2_Escom)
            ThemeManager.THEME_GUINDA -> setTheme(R.style.Theme_BattleNaval_V2_Guinda)
            else -> setTheme(R.style.Theme_BattleNaval_V2_Guinda)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_unified)

        // Inicializar vistas
        btnLearn = findViewById(R.id.btnLearn)
        btnGames = findViewById(R.id.btnGames)
        btnSettings = findViewById(R.id.btnSettings)

        layoutGamesSubMenu = findViewById(R.id.layoutGamesSubMenu)
        btnBattleNaval = findViewById(R.id.btnBattleNaval)
        btnBattleNavalSinglePlayer = findViewById(R.id.btnBattleNavalSinglePlayer)
        btnBattleNavalBluetooth = findViewById(R.id.btnBattleNavalBluetooth)
        btnBinaryGames = findViewById(R.id.btnBinaryGames)

        layoutBattleNavalOptions = findViewById(R.id.layoutBattleNavalOptions)

        // Configurar listeners
        btnLearn.setOnClickListener {
            val intent = Intent(this, EducationalActivity::class.java)
            startActivity(intent)
        }

        btnGames.setOnClickListener {
            // Mostrar/ocultar opciones de juegos
            val isVisible = layoutGamesSubMenu.visibility == View.VISIBLE
            layoutGamesSubMenu.visibility = if (isVisible) View.GONE else View.VISIBLE

            // Si estaba visible el submenu de Battle Naval, ocultarlo también
            if (layoutBattleNavalOptions.visibility == View.VISIBLE && isVisible) {
                layoutBattleNavalOptions.visibility = View.GONE
            }
        }

        btnBattleNaval.setOnClickListener {
            // Mostrar/ocultar opciones de Battle Naval
            val isVisible = layoutBattleNavalOptions.visibility == View.VISIBLE
            layoutBattleNavalOptions.visibility = if (isVisible) View.GONE else View.VISIBLE
        }

        btnBattleNavalSinglePlayer.setOnClickListener {
            val intent = Intent(this, SinglePlayerActivity::class.java)
            startActivity(intent)
        }

        btnBattleNavalBluetooth.setOnClickListener {
            val intent = Intent(this, BluetoothActivity::class.java)
            startActivity(intent)
        }

        btnBinaryGames.setOnClickListener {
            val intent = Intent(this, GamesActivity::class.java)
            startActivity(intent)
        }

        btnSettings.setOnClickListener {
            showThemeDialog()
        }

        // Inicialmente los submenús están ocultos
        layoutGamesSubMenu.visibility = View.GONE
        layoutBattleNavalOptions.visibility = View.GONE
    }

    private fun showThemeDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_theme_selector)
        dialog.setTitle(getString(R.string.select_theme))

        // Obtener tema actual
        val currentTheme = ThemeManager.getTheme(this)

        // Configurar radio buttons
        val radioGroup = dialog.findViewById<android.widget.RadioGroup>(R.id.themeRadioGroup)
        val radioGuinda = dialog.findViewById<android.widget.RadioButton>(R.id.radioGuinda)
        val radioEscom = dialog.findViewById<android.widget.RadioButton>(R.id.radioEscom)

        // Marcar el tema actual
        if (currentTheme == ThemeManager.THEME_GUINDA) {
            radioGuinda.isChecked = true
        } else {
            radioEscom.isChecked = true
        }

        // Listener para selección de tema
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedTheme = when (checkedId) {
                R.id.radioGuinda -> ThemeManager.THEME_GUINDA
                else -> ThemeManager.THEME_ESCOM
            }

            // Guardar y aplicar el tema seleccionado
            ThemeManager.setTheme(this, selectedTheme)
            dialog.dismiss()

            // Reiniciar la actividad para aplicar el tema
            recreate()
        }

        dialog.show()
    }
}