package ovh.gabrielhuav.battlenaval_v2.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import ovh.gabrielhuav.battlenaval_v2.ThemeManager

/**
 * Tema de Compose equivalente a los estilos `Theme.BattleNaval_V2.*` del XML.
 *
 * Lee la preferencia institucional del mismo [ThemeManager] que usan las
 * Activities, de modo que una pantalla en Compose y una en vistas se ven igual
 * mientras dure la migración por fases.
 */

private val UnamLight = lightColorScheme(
    primary = UnamPrimary,
    onPrimary = Color.White,
    primaryContainer = UnamPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = UnamAccent,
    onSecondary = Color.Black
)

private val UnamDark = darkColorScheme(
    primary = UnamPrimaryNight,
    onPrimary = Color.White,
    primaryContainer = UnamPrimaryDarkNight,
    onPrimaryContainer = Color.White,
    secondary = UnamAccentNight,
    onSecondary = Color.Black,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary
)

private val IpnLight = lightColorScheme(
    primary = IpnPrimary,
    onPrimary = Color.White,
    primaryContainer = IpnPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = IpnAccent,
    onSecondary = Color.Black
)

private val IpnDark = darkColorScheme(
    primary = IpnPrimaryNight,
    onPrimary = Color.White,
    primaryContainer = IpnPrimaryDarkNight,
    onPrimaryContainer = Color.White,
    secondary = IpnAccentNight,
    onSecondary = Color.Black,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary
)

/**
 * Envuelve contenido de Compose con la identidad institucional elegida.
 *
 * @param institution `ThemeManager.THEME_UNAM` o `ThemeManager.THEME_IPN`. Por
 *   defecto se lee de las preferencias, igual que hace `ThemeManager.applyTheme`.
 */
@Composable
fun BitBattlesTheme(
    institution: Int = rememberInstitution(LocalContext.current),
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        institution == ThemeManager.THEME_IPN && darkTheme -> IpnDark
        institution == ThemeManager.THEME_IPN -> IpnLight
        darkTheme -> UnamDark
        else -> UnamLight
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}

/**
 * Preferencia institucional guardada. Se lee una vez por composición; el cambio
 * de tema recrea la Activity, así que no hace falta observarla.
 */
@Composable
private fun rememberInstitution(context: Context): Int = ThemeManager.getTheme(context)
