package ovh.gabrielhuav.battlenaval_v2

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

/**
 * Utilidades de pantalla de borde a borde.
 *
 * Al segmentar Android 16 (API 36) el modo de borde a borde es obligatorio y los
 * atributos `android:statusBarColor` y `android:navigationBarColor` se ignoran.
 * Llamar a `enableEdgeToEdge()` de forma explícita consigue el mismo aspecto en
 * Android 14 y anteriores, en lugar de dejar barras opacas solo en esos
 * dispositivos.
 *
 * Los layouts de actividad llevan `android:fitsSystemWindows="true"` en su raíz,
 * así que el contenido sigue quedando por debajo de la barra de estado y por
 * encima de la de navegación. Estas funciones solo se ocupan del color de los
 * iconos de las barras.
 */

/**
 * Para pantallas con fondo oscuro fijo: el tablero de juego (`#0A192F`), la
 * lista de dispositivos Bluetooth y las pantallas con wallpaper institucional.
 * Fuerza iconos claros porque el fondo es oscuro en modo claro y en modo oscuro.
 */
fun ComponentActivity.enableEdgeToEdgeDark() {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
    )
}

/**
 * Para las pantallas educativas, que siguen el tema DayNight: los iconos de las
 * barras se adaptan solos al modo claro u oscuro del sistema.
 */
fun ComponentActivity.enableEdgeToEdgeDayNight() {
    enableEdgeToEdge()
}
