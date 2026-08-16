package ovh.gabrielhuav.battlenaval_v2.sistemabinario

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ovh.gabrielhuav.battlenaval_v2.R
import ovh.gabrielhuav.battlenaval_v2.ui.ComposeFragment
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonParagraph
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonScreen
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonTitle
import ovh.gabrielhuav.battlenaval_v2.ui.theme.BitBattlesTheme

/**
 * Primera lección del módulo de sistema binario.
 *
 * Primera pantalla migrada a Compose (Fase 2). Sigue siendo un `Fragment`, así
 * que `EducationalPagerAdapter` y el `ViewPager2` que la alojan no cambian.
 */
class IntroductionFragment : ComposeFragment() {

    @Composable
    override fun Content() {
        IntroductionScreen()
    }
}

@Composable
private fun IntroductionScreen() {
    LessonScreen {
        LessonTitle(stringResource(R.string.intro_binary_title))
        LessonParagraph(stringResource(R.string.intro_binary_paragraph_1))
        LessonParagraph(stringResource(R.string.intro_binary_paragraph_2))
    }
}

@Preview(showBackground = true)
@Composable
private fun IntroductionScreenPreview() {
    BitBattlesTheme {
        IntroductionScreen()
    }
}
