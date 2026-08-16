package ovh.gabrielhuav.battlenaval_v2.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.ui.theme.BitBattlesTheme

/**
 * Base para los fragmentos ya migrados a Compose.
 *
 * Permite convertir las pantallas una por una sin tocar los `ViewPager2` ni los
 * adaptadores que las alojan: para quien las contiene siguen siendo `Fragment`
 * normales. Es el mecanismo de interop de la Fase 2 descrita en
 * `docs/MIGRACION-COMPOSE.md`.
 *
 * Para migrar un fragmento: hereda de aquí, mueve el contenido del XML a
 * [Content] y borra el layout.
 */
abstract class ComposeFragment : Fragment() {

    /** Contenido de la pantalla. Ya viene envuelto en [BitBattlesTheme]. */
    @Composable
    protected abstract fun Content()

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        // Libera la composición con el ciclo de vida de la vista del fragmento,
        // que es lo correcto dentro de un ViewPager2.
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BitBattlesTheme {
                Content()
            }
        }
    }
}
