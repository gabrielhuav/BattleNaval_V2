package ovh.gabrielhuav.battlenaval_v2.unidadesalmacenamiento

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ovh.gabrielhuav.battlenaval_v2.R
import ovh.gabrielhuav.battlenaval_v2.ui.ComposeFragment
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonExpandableCard
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonImage
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonParagraph
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonScreen
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonTable
import ovh.gabrielhuav.battlenaval_v2.ui.components.LessonTitle
import ovh.gabrielhuav.battlenaval_v2.ui.theme.BitBattlesTheme

/** La escalera de unidades, del bit al terabyte, y por qué el factor es 1024. */
class StorageUnitsTypesFragment : ComposeFragment() {

    @Composable
    override fun Content() {
        StorageUnitsTypesScreen()
    }
}

@Composable
private fun StorageUnitsTypesScreen() {
    LessonScreen {
        LessonTitle(stringResource(R.string.storage_types_title))
        LessonParagraph(stringResource(R.string.storage_types_content))

        LessonTable(
            headers = stringArrayResource(R.array.storage_units_table_headers).toList(),
            rows = listOf(
                R.array.storage_units_table_bit,
                R.array.storage_units_table_byte,
                R.array.storage_units_table_kilobyte,
                R.array.storage_units_table_megabyte,
                R.array.storage_units_table_gigabyte,
                R.array.storage_units_table_terabyte
            ).map { stringArrayResource(it).toList() },
            footnote = stringResource(R.string.storage_types_footnote)
        )

        LessonImage(
            drawable = R.drawable.storage_units_hierarchy,
            contentDescription = stringResource(R.string.storage_types_image_description)
        )
        LessonExpandableCard(
            header = stringResource(R.string.storage_types_did_you_know_header),
            content = stringResource(R.string.storage_types_did_you_know_content)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StorageUnitsTypesScreenPreview() {
    BitBattlesTheme {
        StorageUnitsTypesScreen()
    }
}
