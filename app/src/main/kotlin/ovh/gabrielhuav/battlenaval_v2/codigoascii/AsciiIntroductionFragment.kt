package ovh.gabrielhuav.battlenaval_v2.codigoascii

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

/** Origen y funcionamiento del estándar ASCII. */
class AsciiIntroductionFragment : ComposeFragment() {

    @Composable
    override fun Content() {
        AsciiIntroductionScreen()
    }
}

@Composable
private fun AsciiIntroductionScreen() {
    LessonScreen {
        LessonTitle(stringResource(R.string.ascii_intro_title))
        LessonParagraph(stringResource(R.string.ascii_intro_paragraph_1))
        LessonImage(
            drawable = R.drawable.ascii_illustration,
            contentDescription = stringResource(R.string.ascii_intro_image_description)
        )
        LessonParagraph(stringResource(R.string.ascii_intro_paragraph_2))
    }
}

@Preview(showBackground = true)
@Composable
private fun AsciiIntroductionScreenPreview() {
    BitBattlesTheme {
        AsciiIntroductionScreen()
    }
}
