package ovh.gabrielhuav.battlenaval_v2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.compose.material3.RadioButton
import ovh.gabrielhuav.battlenaval_v2.sistemabinario.EducationalActivity
import ovh.gabrielhuav.battlenaval_v2.sistemabinario.GamesActivity
import ovh.gabrielhuav.battlenaval_v2.sistemabinario.ThemeManager
import ovh.gabrielhuav.battlenaval_v2.battlenavalbinario.BluetoothActivity
import ovh.gabrielhuav.battlenaval_v2.battlenavalbinario.SinglePlayerActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnLearn: Button
    private lateinit var btnGames: Button
    private lateinit var btnSettings: Button
    private lateinit var backgroundImage: ImageView

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
        ThemeManager.applyTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_unified)

        // Inicializar vistas
        backgroundImage = findViewById(R.id.backgroundImage)
        btnLearn = findViewById(R.id.btnLearn)
        btnGames = findViewById(R.id.btnGames)
        btnSettings = findViewById(R.id.btnSettings)

        layoutGamesSubMenu = findViewById(R.id.layoutGamesSubMenu)
        btnBattleNaval = findViewById(R.id.btnBattleNaval)
        btnBattleNavalSinglePlayer = findViewById(R.id.btnBattleNavalSinglePlayer)
        btnBattleNavalBluetooth = findViewById(R.id.btnBattleNavalBluetooth)
        btnBinaryGames = findViewById(R.id.btnBinaryGames)

        layoutBattleNavalOptions = findViewById(R.id.layoutBattleNavalOptions)

        // Aplicar wallpaper según el tema
        backgroundImage.setImageResource(ThemeManager.getThemeWallpaper(this))

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
        // Crear diálogo con estilo personalizado para fondo gris
        val dialog = android.app.Dialog(this, R.style.Theme_Dialog_Gray)
        dialog.setContentView(R.layout.dialog_theme_selector)
        dialog.setTitle(getString(R.string.select_theme))

        // Obtener tema actual
        val currentTheme = ThemeManager.getTheme(this)

        // Configurar radio buttons
        val radioUNAM = dialog.findViewById<RadioButton>(R.id.radioUNAM)
        val radioIPN = dialog.findViewById<RadioButton>(R.id.radioIPN)

        // Botones de acción
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnApply = dialog.findViewById<Button>(R.id.btnApply)

        // Marcar el tema actual
        when (currentTheme) {
            ThemeManager.THEME_UNAM -> radioUNAM.isChecked = true
            ThemeManager.THEME_IPN -> radioIPN.isChecked = true
        }

        // Implementar comportamiento de exclusión mutua manual (ya que estamos usando CardViews)
        radioUNAM.setOnClickListener {
            radioUNAM.isChecked = true
            radioIPN.isChecked = false
        }

        radioIPN.setOnClickListener {
            radioUNAM.isChecked = false
            radioIPN.isChecked = true
        }

        // También hacer que las tarjetas completas sean clickeables
        val cardUNAM = dialog.findViewById<CardView>(R.id.cardUNAM)
        val cardIPN = dialog.findViewById<CardView>(R.id.cardIPN)

        cardUNAM?.setOnClickListener {
            radioUNAM.isChecked = true
            radioIPN.isChecked = false
        }

        cardIPN?.setOnClickListener {
            radioUNAM.isChecked = false
            radioIPN.isChecked = true
        }

        // Botón Cancelar
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Botón Aplicar
        btnApply.setOnClickListener {
            val selectedTheme = when {
                radioUNAM.isChecked -> ThemeManager.THEME_UNAM
                radioIPN.isChecked -> ThemeManager.THEME_IPN
                else -> currentTheme // Mantener el tema actual si nada está seleccionado
            }

            // Solo aplicar si realmente cambió el tema
            if (selectedTheme != currentTheme) {
                ThemeManager.setTheme(this, selectedTheme)
                dialog.dismiss()

                // Mostrar un mensaje de confirmación
                Toast.makeText(this, "Tema actualizado", Toast.LENGTH_SHORT).show()

                // Reiniciar la actividad para aplicar el nuevo tema
                recreate()
            } else {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

}