package ovh.gabrielhuav.battlenaval_v2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        val singlePlayerButton = findViewById<Button>(R.id.btnSinglePlayer)
        val bluetoothButton = findViewById<Button>(R.id.btnBluetooth)

        singlePlayerButton.setOnClickListener {
            val intent = Intent(this, SinglePlayerActivity::class.java)
            startActivity(intent)
        }

        bluetoothButton.setOnClickListener {
            val intent = Intent(this, BluetoothActivity::class.java)
            startActivity(intent)
        }
    }
}
