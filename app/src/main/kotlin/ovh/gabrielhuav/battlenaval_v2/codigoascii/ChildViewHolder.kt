package ovh.gabrielhuav.battlenaval_v2.codigoascii

import android.view.View
import android.widget.TextView
import ovh.gabrielhuav.battlenaval_v2.R

class ChildViewHolder(view: View) {
    val decimalTextView: TextView = view.findViewById(R.id.decimalTextView)
    val binaryTextView: TextView = view.findViewById(R.id.binaryTextView)
    val charTextView: TextView = view.findViewById(R.id.charTextView)
}