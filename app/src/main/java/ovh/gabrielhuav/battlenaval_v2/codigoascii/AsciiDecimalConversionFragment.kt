package ovh.gabrielhuav.battlenaval_v2.codigoascii

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R

class AsciiDecimalConversionFragment : Fragment() {

    private val TAG = "AsciiDecimalConversionFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView called")
        return try {
            inflater.inflate(R.layout.fragment_ascii_decimal_conversion, container, false)
        } catch (e: Exception) {
            Log.e(TAG, "Error inflating view: ${e.message}", e)
            null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        if (!isAdded || activity == null || activity?.isFinishing == true) {
            Log.w(TAG, "Fragment not attached or activity is finishing, skipping UI setup")
            return
        }

        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val contentTextView = view.findViewById<TextView>(R.id.contentTextView)
        val expandableListView = view.findViewById<ExpandableListView>(R.id.asciiExpandableList)

        // Configurar nested scrolling para compatibilidad
        expandableListView?.isNestedScrollingEnabled = true

        titleTextView.text = "Conversión ASCII a Decimal"

        contentTextView.text = """
            ASCII es como un código secreto que las computadoras usan para entender letras, números y símbolos. Cada carácter, como "A" o "1", tiene un número especial llamado valor decimal.

            Para convertir:
            1. Busca el carácter asociado al valor decimal en la tabla ASCII

            Por ejemplo:
              - La letra "A" es el número 65.
              - El número "5" (como carácter) es el número 53.
              - Un espacio en blanco es el número 32.

            Toca una sección para ver los caracteres y sus números. Toca un carácter para más detalles.
        """.trimIndent()

        // Definir las secciones y caracteres
        val asciiGroups = listOf(
            AsciiGroup(
                "Símbolos", listOf(
                    AsciiChar(33, "!", "Signo de exclamación"),
                    AsciiChar(34, "\"", "Comillas"),
                    AsciiChar(35, "#", "Numeral"),
                    AsciiChar(36, "$", "Dólar"),
                    AsciiChar(46, ".", "Punto"),
                    AsciiChar(63, "?", "Interrogación")
                )
            ),
            AsciiGroup(
                "Números", (48..57).map { AsciiChar(it, it.toChar().toString(), "Dígito ${it.toChar()}") }
            ),
            AsciiGroup(
                "Letras mayúsculas", (65..90).map { AsciiChar(it, it.toChar().toString(), "Letra ${it.toChar()}") }
            ),
            AsciiGroup(
                "Letras minúsculas", (97..122).map { AsciiChar(it, it.toChar().toString(), "Letra ${it.toChar()}") }
            ),
            AsciiGroup(
                "Espacio", listOf(
                    AsciiChar(32, "Espacio", "Espacio en blanco")
                )
            )
        )

        // Configurar el ExpandableListView
        try {
            expandableListView?.let {
                val adapter = AsciiExpandableListAdapter(asciiGroups)
                it.setAdapter(adapter)

                // Expandir automáticamente el primer grupo
                it.expandGroup(0)

                // Manejar clics en los elementos hijos
                it.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
                    Log.d(TAG, "Child clicked: group=$groupPosition, child=$childPosition")
                    if (childPosition > 0) { // Ensure it's not the header
                        val char = asciiGroups[groupPosition].chars.getOrNull(childPosition - 1)
                        if (char != null && isAdded && activity?.isFinishing == false) {
                            try {
                                showCharDetailsDialog(char)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error handling child click: ${e.message}", e)
                            }
                        } else {
                            Log.w(TAG, "Click ignored: childPosition=$childPosition, isAdded=$isAdded, isFinishing=${activity?.isFinishing}")
                        }
                    }
                    true
                }

                // Log when groups are expanded
                it.setOnGroupExpandListener { groupPosition ->
                    Log.d(TAG, "Group expanded: ${asciiGroups[groupPosition].name}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up ExpandableListView: ${e.message}", e)
            if (e is android.os.DeadObjectException) {
                Log.e(TAG, "DeadObjectException caught, activity may be dead")
            }
        }
    }

    private fun showCharDetailsDialog(char: AsciiChar) {
        if (!isAdded || activity == null || activity?.isFinishing == true) {
            Log.w(TAG, "Cannot show dialog: Fragment not attached or activity is finishing")
            return
        }

        try {
            // Convertir el valor decimal a binario (8 bits)
            val binary = String.format("%8s", Integer.toBinaryString(char.decimal)).replace(' ', '0')
            AlertDialog.Builder(requireContext())
                .setTitle("Detalles del Carácter")
                .setMessage("""
                    Número: ${char.decimal}
                    Carácter: ${char.char}
                    Descripción: ${char.description}
                    Binario: $binary
                """.trimIndent())
                .setPositiveButton("OK", null)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing dialog: ${e.message}", e)
            if (e is android.os.DeadObjectException) {
                Log.e(TAG, "DeadObjectException caught in dialog, activity may be dead")
            }
        }
    }

    private inner class AsciiExpandableListAdapter(private val groups: List<AsciiGroup>) :
        BaseExpandableListAdapter() {

        override fun getGroupCount(): Int {
            Log.v(TAG, "getGroupCount: ${groups.size}")
            return groups.size
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            val count = groups[groupPosition].chars.size + 1 // +1 for the header
            Log.v(TAG, "getChildrenCount for group $groupPosition: $count")
            return count
        }

        override fun getGroup(groupPosition: Int): AsciiGroup {
            Log.v(TAG, "getGroup: $groupPosition")
            return groups[groupPosition]
        }

        override fun getChild(groupPosition: Int, childPosition: Int): Any {
            Log.v(TAG, "getChild: group=$groupPosition, child=$childPosition")
            return when (childPosition) {
                0 -> "header" // Use a string to identify the header
                else -> groups[groupPosition].chars[childPosition - 1]
            }
        }

        override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

        override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

        override fun hasStableIds(): Boolean = true

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = (childPosition != 0)

        override fun getGroupView(
            groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?
        ): View {
            Log.v(TAG, "getGroupView: group=$groupPosition, isExpanded=$isExpanded")
            val view = convertView ?: LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_ascii_group, parent, false)
            val groupNameTextView = view.findViewById<TextView>(R.id.groupNameTextView)
            groupNameTextView.text = groups[groupPosition].name
            return view
        }

        override fun getChildView(
            groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?
        ): View {
            Log.v(TAG, "getChildView: group=$groupPosition, child=$childPosition")

            val inflater = LayoutInflater.from(parent?.context)

            return if (childPosition == 0) {
                // Header row
                val headerView: View
                val headerHolder: HeaderViewHolder

                if (convertView == null || convertView.tag !is HeaderViewHolder) {
                    headerView = inflater.inflate(R.layout.item_ascii_header, parent, false)
                    headerHolder = HeaderViewHolder()
                    headerView.tag = headerHolder
                } else {
                    headerView = convertView
                    headerHolder = convertView.tag as HeaderViewHolder
                }

                headerView // Return the header view
            } else {
                // Data row
                val view: View
                val holder: ChildViewHolder

                if (convertView == null || convertView.tag !is ChildViewHolder) {
                    // Inflate the data row layout
                    view = inflater.inflate(R.layout.item_ascii_child, parent, false)
                    holder = ChildViewHolder(view)
                    view.tag = holder // Set the tag to the holder
                } else {
                    // Reuse the converted view
                    view = convertView
                    holder = view.tag as ChildViewHolder // Get the holder from the tag
                }

                try {
                    val char = groups[groupPosition].chars[childPosition - 1] // Adjust index for header

                    holder.decimalTextView.text = char.decimal.toString()
                    holder.binaryTextView.text = String.format("%8s", Integer.toBinaryString(char.decimal)).replace(' ', '0')
                    holder.charTextView.text = char.char

                } catch (e: Exception) {
                    Log.e(TAG, "Error setting child view: ${e.message}", e)
                    holder.decimalTextView.text = "Error"
                    holder.binaryTextView.text = "Error"
                    holder.charTextView.text = "Error"
                }

                return view // Return the data row view
            }
        }
    }

    internal class ChildViewHolder(view: View) {
        val decimalTextView: TextView = view.findViewById(R.id.decimalTextView)
        val binaryTextView: TextView = view.findViewById(R.id.binaryTextView)
        val charTextView: TextView = view.findViewById(R.id.charTextView)
    }

    // ViewHolder for the header
    internal class HeaderViewHolder
}