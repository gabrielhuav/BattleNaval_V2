package ovh.gabrielhuav.battlenaval_v2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import ovh.gabrielhuav.battlenaval_v2.sistemabinario.EducationalActivity
import ovh.gabrielhuav.battlenaval_v2.battlenavalbinario.BluetoothActivity
import ovh.gabrielhuav.battlenaval_v2.battlenavalbinario.SinglePlayerActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnBattleNaval: Button
    private lateinit var btnSinglePlayer: Button
    private lateinit var btnBluetooth: Button
    private lateinit var btnEducational: Button
    private lateinit var btnChangeTheme: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicar tema antes de inflar la vista
        ThemeManager.applyTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_unified)

        // Inicializar vistas
        btnBattleNaval = findViewById(R.id.btnBattleNaval)
        btnSinglePlayer = findViewById(R.id.btnSinglePlayer)
        btnBluetooth = findViewById(R.id.btnBluetooth)
        btnEducational = findViewById(R.id.btnEducational)
        btnChangeTheme = findViewById(R.id.btnChangeTheme)

        // Configurar listeners
        btnBattleNaval.setOnClickListener {
            // Mostrar/ocultar opciones de Battle Naval
            val isVisible = btnSinglePlayer.visibility == View.VISIBLE
            val newVisibility = if (isVisible) View.GONE else View.VISIBLE

            btnSinglePlayer.visibility = newVisibility
            btnBluetooth.visibility = newVisibility
        }

        btnSinglePlayer.setOnClickListener {
            val intent = Intent(this, SinglePlayerActivity::class.java)
            startActivity(intent)
        }

        btnBluetooth.setOnClickListener {
            val intent = Intent(this, BluetoothActivity::class.java)
            startActivity(intent)
        }

        btnEducational.setOnClickListener {
            val intent = Intent(this, EducationalActivity::class.java)
            startActivity(intent)
        }

        btnChangeTheme.setOnClickListener {
            showThemeDialog()
        }

        // Inicialmente las opciones de Battle Naval están ocultas
        btnSinglePlayer.visibility = View.GONE
        btnBluetooth.visibility = View.GONE
    }

    private fun showThemeDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_theme_selector)
        dialog.setTitle("Seleccionar Tema")

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