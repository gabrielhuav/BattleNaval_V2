package ovh.gabrielhuav.battlenaval_v2

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
    private lateinit var delayTimerTextView: TextView

    private var shipsPlaced = false
    private var isConnected = false
    private var opponentShips: List<Pair<Int, Int>> = emptyList()
    private var lastOpponentMessage: String? = null
    private var revealedCoordinates: MutableList<Pair<Int, Int>> = mutableListOf()

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
        delayTimerTextView = findViewById(R.id.tvDelayTimer)
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

        findViewById<Button>(R.id.btnRevealNextCoordante).setOnClickListener {
            revealNextCoordinate()
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

    private fun revealNextCoordinate() {
        if (!isConnected || opponentShips.isEmpty()) {
            Toast.makeText(this, "Conexión no establecida o no hay barcos del oponente.", Toast.LENGTH_SHORT).show()
            return
        }

        val nextCoordinate = opponentShips.find { it !in revealedCoordinates }
        if (nextCoordinate != null) {
            revealedCoordinates.add(nextCoordinate)
            drawCoordinateOnBoard(enemyBoard, nextCoordinate, Color.RED)
            revealedCoordinatesTextView.text = "Coordenadas reveladas: ${coordinateToString(nextCoordinate)}"
        } else {
            Toast.makeText(this, "Todas las coordenadas ya han sido reveladas.", Toast.LENGTH_SHORT).show()
        }

        startDelayTimer()
    }

    private fun drawCoordinateOnBoard(board: Board, coordinate: Pair<Int, Int>, color: Int) {
        val (x, y) = coordinate
        board.getCell(x, y).apply {
            setBackgroundColor(color)
            invalidate()
        }
    }

    private fun startDelayTimer() {
        delayTimerTextView.visibility = android.view.View.VISIBLE
        object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                delayTimerTextView.text = "Siguiente turno en ${millisUntilFinished / 1000} segundos..."
            }

            override fun onFinish() {
                delayTimerTextView.visibility = android.view.View.GONE
            }
        }.start()
    }

    private fun handleIncomingMessage(message: String) {
        if (message.startsWith("SHIPS")) {
            val shipsData = message.substringAfter(",")
            opponentShips = deserializeShipPositions(shipsData)
        }
    }

    private fun coordinateToString(coordinate: Pair<Int, Int>): String {
        val (x, y) = coordinate
        val column = x + 1
        val row = ('A' + y)
        return "$row$column"
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

    private fun sendPlayerShipsToOpponent() {
        sendPlayerShipsToServer()
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
