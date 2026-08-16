package ovh.gabrielhuav.battlenaval_v2.sistemabinario

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ovh.gabrielhuav.battlenaval_v2.R
import ovh.gabrielhuav.battlenaval_v2.ui.ComposeFragment
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonImage
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonParagraph
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonScreen
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonTitle
import ovh.gabrielhuav.battlenaval_v2.ui.theme.BitBattlesTheme

/** Sistemas de numeración posicionales y el valor de cada posición. */
class NumerationSystemFragment : ComposeFragment() {

    @Composable
    override fun Content() {
        NumerationSystemScreen()
    }
}

@Composable
private fun NumerationSystemScreen() {
    LessonScreen {
        LessonTitle(stringResource(R.string.numeration_title))
        LessonParagraph(stringResource(R.string.numeration_paragraph_1))
        LessonImage(
            drawable = R.drawable.sistema_decimal,
            contentDescription = stringResource(R.string.numeration_image_description)
        )
        LessonParagraph(stringResource(R.string.numeration_paragraph_2))
    }
}

@Preview(showBackground = true)
@Composable
private fun NumerationSystemScreenPreview() {
    BitBattlesTheme {
        NumerationSystemScreen()
    }
}
