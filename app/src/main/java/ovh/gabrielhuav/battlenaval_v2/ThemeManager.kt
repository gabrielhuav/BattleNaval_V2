package ovh.gabrielhuav.battlenaval_v2

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import ovh.gabrielhuav.battlenaval_v2.R

/**
 * Administrador de temas para la aplicación.
 * Permite seleccionar y aplicar diferentes temas visuales.
 */
class ThemeManager {

    companion object {
        private const val PREFERENCES_FILE = "theme_pref"
        private const val KEY_THEME = "app_theme"

        // Constantes de temas
        const val THEME_GUINDA = 0
        const val THEME_ESCOM = 1

        /**
         * Obtiene el tema actual guardado en preferencias
         * @param context Contexto de la aplicación
         * @return ID del tema actual
         */
        @JvmStatic
        fun getTheme(context: Context): Int {
            val sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
            return sharedPreferences.getInt(KEY_THEME, THEME_GUINDA) // Tema Guinda por defecto
        }

        /**
         * Guarda la selección de tema en preferencias
         * @param context Contexto de la aplicación
         * @param themeId ID del tema seleccionado
         */
        @JvmStatic
        fun setTheme(context: Context, themeId: Int) {
            val sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt(KEY_THEME, themeId)
            editor.apply()
        }

        /**
         * Aplica el tema seleccionado a una actividad
         * @param activity Actividad a la que se aplicará el tema
         */
        @JvmStatic
        fun applyTheme(activity: AppCompatActivity) {
            val themeId = getTheme(activity)
            when (themeId) {
                THEME_ESCOM -> activity.setTheme(R.style.Theme_BattleNaval_V2_Escom)
                THEME_GUINDA -> activity.setTheme(R.style.Theme_BattleNaval_V2_Guinda)
                else -> activity.setTheme(R.style.Theme_BattleNaval_V2_Guinda)
            }
        }

        /**
         * Aplica el tema seleccionado específicamente a EducationalActivity
         * @param activity Instancia de EducationalActivity
         */
        @JvmStatic
        fun applyTheme(activity: ovh.gabrielhuav.battlenaval_v2.sistemabinario.EducationalActivity) {
            applyTheme(activity as AppCompatActivity)
        }

        /**
         * Aplica el tema seleccionado específicamente a GamesActivity
         * @param activity Instancia de GamesActivity
         */
        @JvmStatic
        fun applyTheme(activity: ovh.gabrielhuav.battlenaval_v2.sistemabinario.GamesActivity) {
            applyTheme(activity as AppCompatActivity)
        }
    }
}