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
    private lateinit var revealedCoordinatesTextView: TextView
    private lateinit var etCoordinateInput: EditText
    private lateinit var btnConfirmCoordinate: Button

    private var shipsPlaced = false
    private var isConnected = false
    private var opponentShips: List<Pair<Int, Int>> = emptyList()
    private var revealedCoordinates: MutableList<Pair<Int, Int>> = mutableListOf()
    private var currentRevealedCoordinate: Pair<Int, Int>? = null

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
        revealedCoordinatesTextView = findViewById(R.id.tvRevealedCoordinates)
        etCoordinateInput = findViewById(R.id.etConvertedCoordinates)
        btnConfirmCoordinate = findViewById(R.id.btnConfirmCoordinates)
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

        btnConfirmCoordinate.setOnClickListener {
            validateCoordinate()
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
                        sendPlayerShipsToOpponent()
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
        sendPlayerShipsToServer()
    }

    private fun handleIncomingMessage(message: String) {
        when {
            message.startsWith("SHIPS") -> {
                val shipsData = message.substringAfter(",")
                opponentShips = deserializeShipPositions(shipsData)
            }
            message.startsWith("SHOOT") -> {
                val shootData = message.substringAfter(",")
                val (x, y) = deserializeShot(shootData)

                // Verificar si hay un barco en la celda impactada
                if (playerBoard.isShipAt(x, y)) {
                    // Enviar un mensaje de impacto visual al oponente
                    bluetoothGameManager.sendMessage("ENEMY_HIT,$x,$y")
                    drawImpactOnPlayerBoard(x, y, Color.BLACK)
                    Toast.makeText(this, "¡El enemigo impactó en tus barcos!", Toast.LENGTH_SHORT).show()
                } else {
                    bluetoothGameManager.sendMessage("MISS,$x,$y")
                    Toast.makeText(this, "¡El enemigo falló!", Toast.LENGTH_SHORT).show()
                }
            }
            message.startsWith("ENEMY_HIT") -> {
                val hitData = message.substringAfter(",")
                val (x, y) = deserializeShot(hitData)

                // Cambiar el color de la celda impactada a rojo en el tablero del enemigo
                drawImpactOnEnemyBoard(x, y, Color.RED)
                Toast.makeText(this, "¡Impactaste en un barco enemigo!", Toast.LENGTH_SHORT).show()
            }
            message.startsWith("MISS") -> {
                Toast.makeText(this, "Fallaste el disparo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateCoordinate() {
        val userInput = etCoordinateInput.text.toString().trim()
        if (currentRevealedCoordinate != null) {
            val coordinateString = coordinateToString(currentRevealedCoordinate!!)
            if (userInput.equals(coordinateString, ignoreCase = true)) {
                drawCoordinateOnBoard(enemyBoard, currentRevealedCoordinate!!, Color.RED)
                etCoordinateInput.text.clear()
                currentRevealedCoordinate = null
            } else {
                Toast.makeText(this, "Incorrecto. Intenta nuevamente.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No hay coordenada revelada actualmente.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun coordinateToString(coordinate: Pair<Int, Int>): String {
        val (x, y) = coordinate
        val column = x + 1 // Convertimos la columna de índice a un valor 1-based
        val row = ('A' + y) // Convertimos la fila a una letra (A, B, C...)
        return "$row$column" // Concatenamos la fila y columna en formato texto
    }

    private fun drawCoordinateOnBoard(board: Board, coordinate: Pair<Int, Int>, color: Int) {
        val (x, y) = coordinate
        board.getCell(x, y).apply {
            setBackgroundColor(color)
            invalidate()
        }
    }

    private fun drawImpactOnPlayerBoard(x: Int, y: Int, color: Int) {
        val cell = playerBoard.getCell(x, y)
        cell.setBackgroundColor(color)
        cell.invalidate()
    }

    private fun drawImpactOnEnemyBoard(x: Int, y: Int, color: Int) {
        val cell = enemyBoard.getCell(x, y)
        cell.setBackgroundColor(color)
        cell.invalidate()
    }

    private fun deserializeShot(data: String): Pair<Int, Int> {
        val parts = data.split(",")
        return Pair(parts[0].toIntOrNull() ?: -1, parts[1].toIntOrNull() ?: -1)
    }

    private fun deserializeShipPositions(data: String): List<Pair<Int, Int>> {
        return data.split(";").mapNotNull {
            val parts = it.split(",")
            if (parts.size == 2) {
                val x = parts[0].toIntOrNull()
                val y = parts[1].toIntOrNull()
                if (x != null && y != null) Pair(x, y) else null
            } else null
        }
    }

    private fun placePlayerShips() {
        val shipData = listOf(Pair(4, Color.GREEN), Pair(3, Color.RED), Pair(2, Color.GRAY))
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
        bluetoothGameManager.sendMessage("SHIPS,${serializeShipCoordinates(playerShips)}")
    }

    private fun serializeShipCoordinates(ships: List<Pair<String, String>>): String {
        return ships.joinToString(";") {
            val (row, col) = it
            val x = col.toInt() - 1
            val y = row[0] - 'A'
            "$x,$y"
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
}
