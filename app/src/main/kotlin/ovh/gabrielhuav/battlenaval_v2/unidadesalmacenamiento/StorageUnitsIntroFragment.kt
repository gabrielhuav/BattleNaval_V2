package ovh.gabrielhuav.battlenaval_v2.unidadesalmacenamiento

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ovh.gabrielhuav.battlenaval_v2.R
import ovh.gabrielhuav.battlenaval_v2.ui.ComposeFragment
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonExpandableCard
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonImage
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonParagraph
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonScreen
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonTitle
import ovh.gabrielhuav.battlenaval_v2.ui.theme.BitBattlesTheme

/** Del bit al byte: por qué se miden los datos y con qué unidades. */
class StorageUnitsIntroFragment : ComposeFragment() {

    @Composable
    override fun Content() {
        StorageUnitsIntroScreen()
    }
}

@Composable
private fun StorageUnitsIntroScreen() {
    LessonScreen {
        LessonTitle(stringResource(R.string.storage_intro_title))
        LessonParagraph(stringResource(R.string.storage_intro_content))
        LessonImage(
            drawable = R.drawable.storage_units_intro,
            contentDescription = stringResource(R.string.storage_intro_image_description)
        )
        LessonExpandableCard(
            header = stringResource(R.string.storage_intro_did_you_know_header),
            content = stringResource(R.string.storage_intro_did_you_know_content)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StorageUnitsIntroScreenPreview() {
    BitBattlesTheme {
        StorageUnitsIntroScreen()
    }
}
