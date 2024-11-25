package ovh.gabrielhuav.battlenaval_v2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class BluetoothActivity : AppCompatActivity() {
    private lateinit var bluetoothGameManager: BluetoothGameManager
    private lateinit var boardContainer: FrameLayout
    private lateinit var playerBoard: Board
    private lateinit var enemyBoard: Board
    private lateinit var serverStatusTextView: TextView
    private var running = false
    private var isConnected = false
    private lateinit var boardsLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)

        boardContainer = findViewById(R.id.boardContainer)
        serverStatusTextView = findViewById(R.id.tvBluetoothStatus)
        val startServerButton = findViewById<Button>(R.id.btnStartServer)
        val connectButton = findViewById<Button>(R.id.btnConnectToDevice)
        val backToMenuButton = findViewById<Button>(R.id.btnBackToMenu)

        // Crear el layout que contendrá ambos tableros
        boardsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE // Oculto hasta que haya conexión
        }
        boardContainer.addView(boardsLayout)

        bluetoothGameManager = BluetoothGameManager(this)

        // Listener para mensajes recibidos
        bluetoothGameManager.onMessageReceived = { message ->
            runOnUiThread {
                val parts = message.split(",")
                when (parts[0]) {
                    "SHOOT" -> {
                        val x = parts[1].toInt()
                        val y = parts[2].toInt()
                        val cell = playerBoard.getCell(x, y)
                        val hit = cell.shoot()
                        // Enviar resultado del disparo
                        bluetoothGameManager.sendMessage("RESULT,$x,$y,$hit")

                        if (playerBoard.ships == 0) {
                            Toast.makeText(this, "¡PERDISTE!", Toast.LENGTH_LONG).show()
                            running = false
                        }
                    }
                    "RESULT" -> {
                        val x = parts[1].toInt()
                        val y = parts[2].toInt()
                        val hit = parts[3].toBoolean()
                        val cell = enemyBoard.getCell(x, y)
                        cell.wasShot = true
                        if (hit) {
                            cell.ship = Ship(1, false) // Ship dummy para mostrar el hit
                        }
                        cell.invalidate()

                        if (enemyBoard.ships == 0) {
                            Toast.makeText(this, "¡GANASTE!", Toast.LENGTH_LONG).show()
                            running = false
                        }
                    }
                }
            }
        }

        // Listener para el estado del servidor
        bluetoothGameManager.onStateChanged = { state ->
            runOnUiThread {
                when (state) {
                    BluetoothGameManager.State.LISTEN -> {
                        serverStatusTextView.text = "Estado: Esperando Conexiones..."
                        isConnected = false
                        boardsLayout.visibility = View.GONE
                    }
                    BluetoothGameManager.State.CONNECTING -> {
                        serverStatusTextView.text = "Estado: Conectando..."
                        isConnected = false
                        boardsLayout.visibility = View.GONE
                    }
                    BluetoothGameManager.State.CONNECTED -> {
                        serverStatusTextView.text = "Estado: Conectado"
                        isConnected = true
                        startNewGame()
                        boardsLayout.visibility = View.VISIBLE
                    }
                    BluetoothGameManager.State.NONE -> {
                        serverStatusTextView.text = "Estado: Desconectado"
                        isConnected = false
                        boardsLayout.visibility = View.GONE
                    }
                }
            }
        }

        startServerButton.setOnClickListener {
            bluetoothGameManager.start()
            Toast.makeText(this, "Servidor Iniciado", Toast.LENGTH_SHORT).show()
        }

        connectButton.setOnClickListener {
            val intent = Intent(this, DeviceListActivity::class.java)
            startActivityForResult(intent, REQUEST_CONNECT_DEVICE)
        }

        backToMenuButton.setOnClickListener {
            bluetoothGameManager.stop()
            finish()
        }
    }

    private fun startNewGame() {
        running = true
        boardsLayout.removeAllViews()

        // Crear tablero del jugador (con barcos visibles)
        playerBoard = Board(this, false) { }
        placePlayerShips()

        // Crear tablero enemigo (sin barcos visibles)
        enemyBoard = Board(this, true) { cell ->
            if (!running || cell.wasShot || !isConnected) return@Board
            // Enviar el disparo al otro dispositivo
            bluetoothGameManager.sendMessage("SHOOT,${cell.x},${cell.y}")
        }

        // Agregar ambos tableros al layout
        boardsLayout.apply {
            addView(playerBoard, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f
            ))
            addView(enemyBoard, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f
            ))
        }
    }

    private fun placePlayerShips() {
        val shipData = listOf(
            Pair(4, Color.GREEN),
            Pair(3, Color.RED),
            Pair(2, Color.GRAY)
        )

        for ((size, color) in shipData) {
            while (true) {
                val x = Random.nextInt(0, 7)
                val y = Random.nextInt(0, 7)
                val vertical = Random.nextBoolean()

                if (playerBoard.placeShip(Ship(size, vertical, color), x, y)) break
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CONNECT_DEVICE && resultCode == RESULT_OK) {
            val deviceAddress = data?.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS)
            if (deviceAddress != null) {
                Toast.makeText(this, "Conectando a $deviceAddress", Toast.LENGTH_SHORT).show()
                val device = bluetoothGameManager.getDeviceByAddress(deviceAddress)
                if (device != null) {
                    bluetoothGameManager.connect(device)
                } else {
                    Toast.makeText(this, "Dispositivo no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothGameManager.stop()
    }

    companion object {
        private const val REQUEST_CONNECT_DEVICE = 1
    }
}