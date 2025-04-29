package ovh.gabrielhuav.battlenaval_v2.sistemabinario

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R
import ovh.gabrielhuav.battlenaval_v2.codigoascii.AsciiEducationalActivity
import ovh.gabrielhuav.battlenaval_v2.unidadesalmacenamiento.StorageUnitsActivity

class LearningOptionsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_learning_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar los listeners para las tarjetas
        val cardBinarySystem = view.findViewById<CardView>(R.id.cardBinarySystem)
        val cardAsciiCode = view.findViewById<CardView>(R.id.cardAsciiCode)
        val cardStorageUnits = view.findViewById<CardView>(R.id.cardStorageUnits)
        val btnBackToMenu = view.findViewById<Button>(R.id.btnBackToMenu)

        cardBinarySystem.setOnClickListener {
            // Abrir la actividad educativa existente
            val intent = Intent(activity, EducationalActivity::class.java)
            startActivity(intent)
        }

        cardAsciiCode.setOnClickListener {
            // Abrir la nueva actividad de código ASCII
            val intent = Intent(activity, AsciiEducationalActivity::class.java)
            startActivity(intent)
        }

        cardStorageUnits.setOnClickListener {
            // Abrir la actividad de unidades de almacenamiento
            val intent = Intent(activity, StorageUnitsActivity::class.java)
            startActivity(intent)
        }

        btnBackToMenu.setOnClickListener {
            // En lugar de cerrar la actividad, ocultamos el fragmento y mostramos la UI principal
            if (activity != null) {
                // Si estamos en la MainActivity, ocultamos el fragmento y mostramos el contenido principal
                val mainActivity = activity
                val fragmentContainer = mainActivity?.findViewById<View>(R.id.fragment_container)
                val mainContentLayout = mainActivity?.findViewById<View>(R.id.mainContentLayout)

                fragmentContainer?.visibility = View.GONE
                mainContentLayout?.visibility = View.VISIBLE

                // También nos aseguramos de quitar este fragmento del backstack
                activity?.supportFragmentManager?.popBackStack()
            }
        }
    }
}