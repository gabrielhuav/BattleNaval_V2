package ovh.gabrielhuav.battlenaval_v2

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.random.Random

class BluetoothActivity : AppCompatActivity() {
    private lateinit var bluetoothGameManager: BluetoothGameManager
    private lateinit var playerBoardFrame: FrameLayout
    private lateinit var enemyBoardFrame: FrameLayout
    private lateinit var playerBoard: Board
    private lateinit var enemyBoard: Board
    private lateinit var serverStatusTextView: TextView

    private var shipsPlaced = false
    private var isConnected = false
    private var opponentShips: List<Pair<Int, Int>> = emptyList()
    private var lastOpponentMessage: String? = null // Variable para guardar el último mensaje recibido

    private lateinit var devicePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)

        checkBluetoothPermissions()
        initializeViews()
        initializeBoards()

        devicePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val deviceAddress = result.data?.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS)
                deviceAddress?.let {
                    bluetoothGameManager.connect(it)
                }
            }
        }

        bluetoothGameManager = BluetoothGameManager(this, playerBoard, enemyBoard, BluetoothService(this)).apply {
            onStateChanged = { state -> updateServerStatus(state) }
            onMessageReceived = { message ->
                handleIncomingMessage(message)
            }
        }

        setupButtons()
    }

    private fun initializeViews() {
        playerBoardFrame = findViewById(R.id.playerBoardFrame)
        enemyBoardFrame = findViewById(R.id.enemyBoardFrame)
        serverStatusTextView = findViewById(R.id.tvBluetoothStatus)
    }

    private fun initializeBoards() {
        playerBoard = Board(this, false) {}
        playerBoardFrame.addView(playerBoard)

        enemyBoard = Board(this, true) { cell ->
            if (cell.wasShot || !isConnected) return@Board
            bluetoothGameManager.sendMessage("SHOOT,${cell.x},${cell.y}")
        }
        enemyBoardFrame.addView(enemyBoard)
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnStartServer).setOnClickListener {
            bluetoothGameManager.start()
            Toast.makeText(this, "Servidor iniciado.", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnConnectToDevice).setOnClickListener {
            val intent = Intent(this, DeviceListActivity::class.java)
            devicePickerLauncher.launch(intent)
        }

        findViewById<Button>(R.id.btnBackToMenu).setOnClickListener {
            bluetoothGameManager.stop()
            finish()
        }

        findViewById<Button>(R.id.btnZoomIn).setOnClickListener {
            sendPlayerShipsToOpponent() // Reenvía los barcos al oponente
            drawOpponentShipsOnEnemyBoard() // Dibuja los barcos del oponente con el último mensaje recibido
        }
    }

    private fun updateServerStatus(state: BluetoothGameManager.State) {
        runOnUiThread {
            when (state) {
                BluetoothGameManager.State.LISTEN -> serverStatusTextView.text = "Estado: Esperando conexiones..."
                BluetoothGameManager.State.CONNECTING -> serverStatusTextView.text = "Estado: Conectando..."
                BluetoothGameManager.State.CONNECTED -> {
                    serverStatusTextView.text = "Estado: Conectado"
                    isConnected = true
                    if (!shipsPlaced) {
                        placePlayerShips()
                        sendPlayerShipsToOpponent() // Envía los barcos al oponente
                        shipsPlaced = true
                    }
                }
                BluetoothGameManager.State.NONE -> {
                    serverStatusTextView.text = "Estado: Desconectado"
                    shipsPlaced = false
                }
            }
        }
    }

    private fun sendPlayerShipsToOpponent() {
        val playerShips = playerBoard.getShipCoordinates()
        bluetoothGameManager.sendMessage("SHIPS,${serializeNumericShipPositions(playerShips)}")
    }

    private fun checkBluetoothPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 1)
        }
    }

    private fun handleIncomingMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this, "Mensaje recibido: $message", Toast.LENGTH_SHORT).show()
        }

        when {
            message.startsWith("SHIPS") -> {
                val shipsData = message.substringAfter(",")
                val receivedShips = deserializeShipPositions(shipsData)

                if (receivedShips.isNotEmpty()) {
                    opponentShips = receivedShips
                    lastOpponentMessage = message // Guarda el mensaje recibido
                    runOnUiThread {
                        Toast.makeText(this, "Barcos del oponente recibidos: $receivedShips", Toast.LENGTH_SHORT).show()
                    }
                    // Envía confirmación de recepción
                    bluetoothGameManager.sendMessage("RECEIVED")
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Error: Datos de barcos vacíos o inválidos.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            message == "RECEIVED" -> {
                runOnUiThread {
                    Toast.makeText(this, "El oponente confirmó la recepción de los barcos.", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                runOnUiThread {
                    Toast.makeText(this, "Mensaje desconocido recibido: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun drawOpponentShipsOnEnemyBoard() {
        if (opponentShips.isEmpty() && lastOpponentMessage != null) {
            opponentShips = deserializeShipPositions(lastOpponentMessage!!.substringAfter(","))
        }

        if (opponentShips.isEmpty()) {
            Toast.makeText(this, "No hay barcos del oponente para dibujar.", Toast.LENGTH_SHORT).show()
            return
        }

        runOnUiThread {
            enemyBoard.clearShips()

            opponentShips.forEach { (x, y) ->
                try {
                    if (x in 0..6 && y in 0..6) {
                        val ship = Ship(1, false, Color.RED)
                        enemyBoard.placeShip(ship, x, y)
                    } else {
                        Toast.makeText(this, "Coordenadas fuera de rango: ($x, $y)", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al procesar coordenadas: ($x, $y)", Toast.LENGTH_SHORT).show()
                }
            }

            enemyBoard.redrawBoard()
            Toast.makeText(this, "Barcos del oponente dibujados en el tablero 2.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun placePlayerShips() {
        val shipData = listOf(Pair(4, Color.GREEN), Pair(3, Color.BLUE), Pair(2, Color.YELLOW))
        for ((size, color) in shipData) {
            while (true) {
                val x = Random.nextInt(0, 7)
                val y = Random.nextInt(0, 7)
                val vertical = Random.nextBoolean()
                if (playerBoard.placeShip(Ship(size, vertical, color), x, y)) break
            }
        }
    }

    private fun serializeNumericShipPositions(ships: List<Pair<String, String>>): String {
        return ships.joinToString(";") {
            val (row, col) = it
            val x = col.toInt() - 1
            val y = row[0] - 'A'
            "$x,$y"
        }
    }

    private fun deserializeShipPositions(data: String): List<Pair<Int, Int>> {
        return data.split(";").mapNotNull {
            val parts = it.split(",")
            if (parts.size == 2) {
                val x = parts[0].toIntOrNull()
                val y = parts[1].toIntOrNull()
                if (x != null && y != null && x in 0..6 && y in 0..6) Pair(x, y) else null
            } else null
        }
    }
}
