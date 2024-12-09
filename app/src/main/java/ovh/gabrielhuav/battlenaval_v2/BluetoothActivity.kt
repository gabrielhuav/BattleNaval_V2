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
                    sendPlayerShipsToServer()
                }
            }
        }

        bluetoothGameManager = BluetoothGameManager(this, playerBoard, enemyBoard, BluetoothService(this)).apply {
            onStateChanged = { state -> updateServerStatus(state) }
            onMessageReceived = { message -> handleIncomingMessage(message) }
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
            copyShipsToEnemyBoard()
        }
    }

    private fun copyShipsToEnemyBoard() {
        runOnUiThread {
            val playerShips = playerBoard.getShipCoordinates()

            // Limpia el tablero enemigo antes de copiar los barcos
            enemyBoard.clearShips()

            playerShips.forEach { (row, col) ->
                try {
                    val y = row[0] - 'A' // Convertir fila (A, B, ...) a índice.
                    val x = col.toInt() - 1 // Convertir columna (1, 2, ...) a índice.

                    if (x in 0..6 && y in 0..6) {
                        val ship = Ship(1, false, Color.BLUE) // Color distintivo.
                        val placed = enemyBoard.placeShip(ship, x, y)
                        if (!placed) {
                            Toast.makeText(this, "No se pudo colocar barco en posición ($row, $col)", Toast.LENGTH_SHORT).show()
                        } else {
                            enemyBoard.getCell(x, y).invalidate() // Redibuja la celda después de colocar el barco
                        }
                    } else {
                        Toast.makeText(this, "Coordenadas fuera de rango: ($row, $col)", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al colocar barco en ($row, $col): ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            enemyBoard.invalidate() // Fuerza el redibujado completo del tablero enemigo
            Toast.makeText(this, "Barcos copiados al tablero 2.", Toast.LENGTH_SHORT).show()
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
                        sendPlayerShipsToClient()
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
        if (message.startsWith("SHIPS")) {
            val shipsData = message.substringAfter(",")
            val receivedShips = deserializeShipPositions(shipsData)

            if (receivedShips.isNotEmpty()) {
                drawShipsOnEnemyBoard(receivedShips)
            } else {
                Toast.makeText(this, "No se recibieron barcos válidos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun drawShipsOnEnemyBoard(ships: List<Pair<Int, Int>>) {
        runOnUiThread {
            enemyBoard.clearShips()

            ships.forEach { (x, y) ->
                try {
                    if (x in 0..6 && y in 0..6) {
                        val ship = Ship(1, false, Color.BLUE)
                        enemyBoard.placeShip(ship, x, y)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al colocar barco en ($x, $y): ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            enemyBoard.invalidate()
            Toast.makeText(this, "Tablero enemigo actualizado.", Toast.LENGTH_SHORT).show()
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

    private fun sendPlayerShipsToServer() {
        val playerShips = playerBoard.getShipCoordinates()
        bluetoothGameManager.sendMessage("SHIPS,${serializeNumericShipPositions(playerShips)}")
    }

    private fun sendPlayerShipsToClient() {
        val playerShips = playerBoard.getShipCoordinates()
        bluetoothGameManager.sendMessage("SHIPS,${serializeNumericShipPositions(playerShips)}")
    }

    private fun serializeNumericShipPositions(ships: List<Pair<String, String>>): String {
        return ships.joinToString(";") {
            val (x, y) = convertToNumericCoordinates(it.first, it.second)
            "$x,$y"
        }
    }

    private fun deserializeShipPositions(data: String): List<Pair<Int, Int>> {
        return data.split(";").mapNotNull {
            val parts = it.split(",")
            if (parts.size == 2) {
                val y = parts[0][0] - 'A'
                val x = parts[1].toIntOrNull()?.minus(1) ?: -1
                if (x in 0..6 && y in 0..6) Pair(x, y) else null
            } else null
        }
    }

    private fun convertToNumericCoordinates(row: String, col: String): Pair<Int, Int> {
        val y = row[0] - 'A'
        val x = col.toInt() - 1
        return Pair(x, y)
    }
}
