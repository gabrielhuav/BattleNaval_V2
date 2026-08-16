package ovh.gabrielhuav.battlenaval_v2.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Piezas comunes de las pantallas didácticas.
 *
 * Centralizarlas mantiene la tipografía y los márgenes iguales en los 26
 * fragmentos educativos conforme se van migrando, y evita que cada pantalla
 * reinvente su propio espaciado como pasaba con los layouts XML.
 */

/**
 * Contenedor de una lección: desplazable en vertical, con el margen estándar.
 *
 * Es desplazable a propósito, no por estética: desde la versión 11 las
 * actividades no bloquean la orientación, así que toda pantalla debe caber
 * también en horizontal y en ventanas pequeñas.
 */
@Composable
fun LessonScreen(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

/** Título de una lección. */
@Composable
fun LessonTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

/** Subtítulo dentro de una lección, para separar apartados. */
@Composable
fun LessonSubtitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

/** Párrafo de cuerpo de una lección. */
@Composable
fun LessonParagraph(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

/**
 * Ilustración de una lección. Ocupa el ancho disponible con altura fija, igual
 * que los `ImageView` de 200dp de los layouts originales.
 *
 * @param contentDescription texto para lectores de pantalla; `null` si la
 *   imagen es decorativa y el texto contiguo ya la explica.
 */
@Composable
fun LessonImage(
    @DrawableRes drawable: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp
) {
    Image(
        painter = painterResource(drawable),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    )
}
