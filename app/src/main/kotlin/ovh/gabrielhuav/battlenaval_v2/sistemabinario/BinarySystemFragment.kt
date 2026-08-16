package ovh.gabrielhuav.battlenaval_v2.sistemabinario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ovh.gabrielhuav.battlenaval_v2.R
import ovh.gabrielhuav.battlenaval_v2.ui.ComposeFragment
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonImage
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonParagraph
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonScreen
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonTitle
import ovh.gabrielhuav.battlenaval_v2.ui.theme.BitBattlesTheme

/**
 * El bit como estado de encendido/apagado.
 *
 * Incluye el interruptor con el foco, que es la metáfora central de la lección:
 * un bit no es un número abstracto, es un circuito con o sin corriente.
 */
class BinarySystemFragment : ComposeFragment() {

    @Composable
    override fun Content() {
        BinarySystemScreen()
    }
}

@Composable
private fun BinarySystemScreen() {
    LessonScreen {
        LessonTitle(stringResource(R.string.binary_system_lesson_title))
        LessonParagraph(stringResource(R.string.binary_system_content))
        LightBulbSwitch()
        LessonImage(
            drawable = R.drawable.sistema_binario,
            contentDescription = stringResource(R.string.binary_system_image_description)
        )
    }
}

/**
 * Foco que se enciende y se apaga con el interruptor.
 *
 * `rememberSaveable` conserva el estado al rotar el dispositivo, que ahora es
 * posible porque desde la versión 11 no se bloquea la orientación.
 */
@Composable
private fun LightBulbSwitch(modifier: Modifier = Modifier) {
    var isOn by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LessonImage(
            drawable = if (isOn) R.drawable.light_bulb_on else R.drawable.light_bulb_off,
            contentDescription = stringResource(
                if (isOn) R.string.binary_system_bulb_on_description
                else R.string.binary_system_bulb_off_description
            ),
            height = 100.dp
        )

        Text(
            text = stringResource(
                if (isOn) R.string.binary_system_switch_on else R.string.binary_system_switch_off
            )
        )

        Switch(checked = isOn, onCheckedChange = { isOn = it })
    }
}

@Preview(showBackground = true)
@Composable
private fun BinarySystemScreenPreview() {
    BitBattlesTheme {
        BinarySystemScreen()
    }
}
