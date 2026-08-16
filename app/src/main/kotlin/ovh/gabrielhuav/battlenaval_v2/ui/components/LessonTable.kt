package ovh.gabrielhuav.battlenaval_v2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tabla de consulta de una lección.
 *
 * Se desplaza en horizontal en vez de comprimir las columnas: las tablas de
 * unidades y de códigos ASCII se leen celda a celda, y partir las palabras en
 * pantallas estrechas las vuelve inservibles como material de consulta.
 *
 * @param footnote nota al pie opcional, que ocupa todo el ancho.
 */
@Composable
fun LessonTable(
    headers: List<String>,
    rows: List<List<String>>,
    modifier: Modifier = Modifier,
    columnWidth: Dp = 140.dp,
    footnote: String? = null
) {
    Column(modifier = modifier.horizontalScroll(rememberScrollState())) {
        Row(modifier = Modifier.background(MaterialTheme.colorScheme.primary)) {
            headers.forEach { header ->
                Text(
                    text = header,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .width(columnWidth)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        rows.forEach { row ->
            Row {
                row.forEach { cell ->
                    Text(
                        text = cell,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .width(columnWidth)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
            HorizontalDivider()
        }

        footnote?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .width(columnWidth * headers.size)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}
