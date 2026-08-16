package ovh.gabrielhuav.battlenaval_v2.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta institucional, espejo de `res/values/colors.xml`.
 *
 * Mientras la migración a Compose esté a medias, estos valores y los del XML
 * describen los mismos colores: si cambias uno, cambia el otro. Cuando ya no
 * queden pantallas con vistas, este archivo pasa a ser la única fuente.
 */

// UNAM — azul institucional con acento dorado
val UnamPrimary = Color(0xFF004680)
val UnamPrimaryDark = Color(0xFF00336B)
val UnamAccent = Color(0xFFFCD116)
val UnamPrimaryNight = Color(0xFF00336B)
val UnamPrimaryDarkNight = Color(0xFF00234A)
val UnamAccentNight = Color(0xFFDBB100)

// IPN — guinda institucional
val IpnPrimary = Color(0xFF7A193E)
val IpnPrimaryDark = Color(0xFF5C1430)
val IpnAccent = Color(0xFFFFFFFF)
val IpnPrimaryNight = Color(0xFF5C1430)
val IpnPrimaryDarkNight = Color(0xFF3E0D20)
val IpnAccentNight = Color(0xFFE0E0E0)

// Superficies del modo oscuro
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF2D2D2D)
val DarkTextPrimary = Color(0xFFF1F1F1)
val DarkTextSecondary = Color(0xFFBDBDBD)

// Paleta del tablero: azul marino de radar con cian neón.
// Es la que usan Cell y los layouts del juego, y la que necesitará el tablero
// en Compose cuando llegue la Fase 5.
val BoardBackground = Color(0xFF0A192F)
val BoardWater = Color(0xFF112240)
val BoardGrid = Color(0xFF233554)
val BoardHeader = Color(0xFF0F294D)
val BoardNeon = Color(0xFF00E5FF)
val BoardHit = Color(0xFFFF3D00)
val BoardHitCore = Color(0xFFFFEA00)
val BoardSplash = Color(0xFF80D8FF)
