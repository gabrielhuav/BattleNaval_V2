package ovh.gabrielhuav.battlenaval_v2.sistemabinario

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import ovh.gabrielhuav.battlenaval_v2.R

/**
 * Administrador de temas para la aplicación.
 */
object ThemeManager {
    private const val PREFERENCES_FILE = "theme_pref"
    private const val KEY_THEME = "app_theme"

    // Constantes de temas
    const val THEME_UNAM = 0
    const val THEME_IPN = 1

    /**
     * Obtiene el tema actual guardado en preferencias
     */
    @JvmStatic
    fun getTheme(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(KEY_THEME, THEME_UNAM) // Tema UNAM por defecto
    }

    /**
     * Guarda la selección de tema en preferencias
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
     */
    @JvmStatic
    fun applyTheme(activity: AppCompatActivity) {
        val themeId = getTheme(activity)
        when (themeId) {
            THEME_IPN -> activity.setTheme(R.style.Theme_BattleNaval_V2_IPN)
            THEME_UNAM -> activity.setTheme(R.style.Theme_BattleNaval_V2_UNAM)
            else -> activity.setTheme(R.style.Theme_BattleNaval_V2_UNAM)
        }
    }

    /**
     * Obtiene el wallpaper correspondiente al tema actual
     */
    @JvmStatic
    fun getThemeWallpaper(context: Context): Int {
        return when (getTheme(context)) {
            THEME_IPN -> R.drawable.wallpaper_ipn
            THEME_UNAM -> R.drawable.wallpaper_unam
            else -> R.drawable.wallpaper_unam
        }
    }
}