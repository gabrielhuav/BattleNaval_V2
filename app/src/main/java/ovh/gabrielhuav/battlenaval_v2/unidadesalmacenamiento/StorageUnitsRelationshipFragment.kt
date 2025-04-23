package ovh.gabrielhuav.battlenaval_v2.unidadesalmacenamiento

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ovh.gabrielhuav.battlenaval_v2.R

class StorageUnitsRelationshipFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_storage_units_relationship, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val contentTextView = view.findViewById<TextView>(R.id.contentTextView)
        val examplesTextView = view.findViewById<TextView>(R.id.examplesTextView)
        val comparisonTableLayout = view.findViewById<TableLayout>(R.id.comparisonTableLayout)
        val illustrationImageView = view.findViewById<ImageView>(R.id.illustrationImageView)

        titleTextView.text = "El Mundo Real y Digital: ¿Cómo se Relacionan?"

        contentTextView.text = """
            Nuestro mundo físico (el que podemos tocar) y el mundo digital (el de las computadoras) están conectados. Muchas cosas del mundo real pueden convertirse en información digital que guardamos en dispositivos.
            
            Algunos ejemplos de cómo convertimos cosas reales a digitales:
            
            • Una foto impresa → Una imagen digital (JPG, PNG)
            • Una carta escrita → Un documento de texto (PDF, DOC)
            • Un disco de música → Archivos de audio (MP3, WAV)
            • Un libro → Un libro electrónico (PDF, EPUB)
            • Un video en cinta → Un archivo de video (MP4, AVI)
            
            Al convertir algo físico a digital, lo transformamos en información que puede guardarse como bits y bytes.
        """.trimIndent()

        examplesTextView.text = """
            ¿CUÁNTO ESPACIO OCUPAN LAS COSAS QUE CONVERTIMOS?
            
            Piensa en estas comparaciones:
            
            • Una página de texto: 2 KB (kilobytes) aproximadamente
                → Podrías guardar 500 páginas en 1 MB
            
            • Una fotografía de calidad media: 2 MB (megabytes) aproximadamente
                → En 1 GB cabrían unas 500 fotos
            
            • Una canción de 3 minutos: 3-5 MB aproximadamente
                → Un álbum completo: 50-70 MB
            
            • Un video de 10 minutos en HD: 200-500 MB aproximadamente
                → Una película de 2 horas: 2-4 GB
            
            • Todos tus libros de texto de un año escolar (digitalizados): 
                → Aproximadamente 100-200 MB
            
            ¡Es increíble! Una biblioteca pequeña con 1,000 libros podría caber en una memoria USB de 8 GB. El mundo digital nos permite almacenar mucha información en espacios muy pequeños.
        """.trimIndent()

        // Crear tabla de comparación
        createComparisonTable(comparisonTableLayout)

        // Establecer imagen ilustrativa
        illustrationImageView.setImageResource(R.drawable.analog_vs_digital)
    }

    private fun createComparisonTable(tableLayout: TableLayout) {
        // Crear encabezados
        val headerRow = TableRow(requireContext())
        listOf("Objeto Físico", "Versión Digital", "Tamaño Digital Aprox.").forEach { header ->
            val textView = TextView(requireContext()).apply {
                text = header
                setPadding(8, 8, 8, 8)
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_700))
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                textSize = 16f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            headerRow.addView(textView)
        }
        tableLayout.addView(headerRow)

        // Datos de comparación
        val comparisonData = listOf(
            arrayOf("Libreta de notas (100 páginas)", "Documento de texto", "200 KB"),
            arrayOf("Álbum de fotos (100 imágenes)", "Carpeta con imágenes JPG", "200-300 MB"),
            arrayOf("Enciclopedia (20 tomos)", "Wikipedia (parcial)", "10 GB"),
            arrayOf("Colección de 100 CDs de música", "Biblioteca de MP3", "30-50 GB"),
            arrayOf("DVD con película", "Archivo MP4", "2-4 GB"),
            arrayOf("Tu mochila con libros de texto", "Carpeta con PDFs", "100-200 MB")
        )

        // Agregar filas de datos
        comparisonData.forEach { rowData ->
            val row = TableRow(requireContext())

            rowData.forEach { cellData ->
                row.addView(createTableCell(cellData))
            }

            tableLayout.addView(row)
        }
    }

    private fun createTableCell(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            setPadding(12, 8, 12, 8)
            textSize = 14f
        }
    }
}