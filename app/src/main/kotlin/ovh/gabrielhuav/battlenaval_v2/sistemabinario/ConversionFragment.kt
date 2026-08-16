package ovh.gabrielhuav.battlenaval_v2.sistemabinario

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ovh.gabrielhuav.battlenaval_v2.R
import ovh.gabrielhuav.battlenaval_v2.ui.ComposeFragment
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonParagraph
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonScreen
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonSubtitle
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonTitle
import ovh.gabrielhuav.battlenaval_v2.ui.theme.BitBattlesTheme

/** Presentación del método de divisiones sucesivas. */
class ConversionFragment : ComposeFragment() {

    @Composable
    override fun Content() {
        ConversionScreen()
    }
}

@Composable
private fun ConversionScreen() {
    LessonScreen {
        LessonTitle(stringResource(R.string.conversion_lesson_title))
        LessonSubtitle(stringResource(R.string.conversion_lesson_subtitle))
        LessonParagraph(stringResource(R.string.conversion_lesson_content))
    }
}

@Preview(showBackground = true)
@Composable
private fun ConversionScreenPreview() {
    BitBattlesTheme {
        ConversionScreen()
    }
}
