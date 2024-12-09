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
import com.google.gson.Gson
import java.io.File
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
    private lateinit var etCoordinateInput: EditText
    private lateinit var btnConfirmCoordinate: Button
    private lateinit var btnSaveGame: Button

    private var shipsPlaced = false
    private var isConnected = false
    private var opponentShips: List<Pair<Int, Int>> = emptyList()
    private var lastOpponentMessage: String? = null
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
        delayTimerTextView = findViewById(R.id.tvDelayTimer)
        etCoordinateInput = findViewById(R.id.etConvertedCoordinates)
        btnConfirmCoordinate = findViewById(R.id.btnConfirmCoordinates)
        btnSaveGame = findViewById(R.id.btnSaveGame)
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
            if (currentRevealedCoordinate == null) {
                revealNextCoordinate()
            } else {
                Toast.makeText(this, "Debe adivinar la coordenada actual antes de avanzar.", Toast.LENGTH_SHORT).show()
            }
        }

        btnConfirmCoordinate.setOnClickListener {
            validateCoordinate()
        }

        btnSaveGame.setOnClickListener {
            saveGameState()
            displaySavedGameState()
            Toast.makeText(this, "Estado de la partida guardado.", Toast.LENGTH_SHORT).show()
        }
    }

    // Método para guardar el estado del juego
    private fun saveGameState() {
        // Construir el estado del juego
        val gameState = GameState(
            playerShips = getShipData(playerBoard),
            enemyShips = getShipData(enemyBoard),
            revealedCoordinates = revealedCoordinates,
            currentRevealedCoordinate = currentRevealedCoordinate
        )

        // Serializar a JSON
        val json = Gson().toJson(gameState)

        // Guardar en un archivo interno
        val file = File(filesDir, "game_state.json")
        file.writeText(json)

        // Confirmación en los logs
        println("Estado del juego guardado: $json")
    }

    // Método para mostrar el estado guardado en un AlertDialog
    private fun displaySavedGameState() {
        val file = File(filesDir, "game_state.json")
        if (!file.exists()) {
            Toast.makeText(this, "No hay un estado guardado para mostrar.", Toast.LENGTH_SHORT).show()
            return
        }

        val json = file.readText() // Leer contenido del archivo
        val dialog = AlertDialog.Builder(this)
            .setTitle("Estado Guardado")
            .setMessage(json)
            .setPositiveButton("Cerrar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
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

        // Buscar la siguiente coordenada que no haya sido revelada
        val nextCoordinate = opponentShips.find { it !in revealedCoordinates }
        if (nextCoordinate != null) {
            // Actualizar la coordenada actual revelada
            currentRevealedCoordinate = nextCoordinate
            revealedCoordinates.add(nextCoordinate)

            // Dibujar la celda en verde oscuro para apuntar
            drawCoordinateOnBoard(enemyBoard, nextCoordinate, Color.parseColor("#006400")) // Verde oscuro
            revealedCoordinatesTextView.text = "Coordenada revelada: ${coordinateToString(nextCoordinate)}"
        } else {
            Toast.makeText(this, "Todas las coordenadas ya han sido reveladas.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateCoordinate() {
        val userInput = etCoordinateInput.text.toString().trim()
        if (currentRevealedCoordinate != null) {
            // Convertir la coordenada revelada a binario ASCII
            val coordinateString = coordinateToBinaryASCII(currentRevealedCoordinate!!)
            if (userInput.equals(coordinateString, ignoreCase = true)) {
                Toast.makeText(this, "¡Correcto! Coordenada impactada.", Toast.LENGTH_SHORT).show()

                // Cambiar el color a rojo para indicar un impacto confirmado
                drawCoordinateOnBoard(enemyBoard, currentRevealedCoordinate!!, Color.RED)
                etCoordinateInput.text.clear()
                currentRevealedCoordinate = null
                delayTimerTextView.visibility = android.view.View.GONE
            } else {
                // Mostrar la respuesta correcta en formato binario
                Toast.makeText(
                    this,
                    "Incorrecto. La respuesta correcta era: $coordinateString",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(this, "No hay coordenada revelada actualmente.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun coordinateToBinaryASCII(coordinate: Pair<Int, Int>): String {
        val (x, y) = coordinate
        val letter = ('A' + y).toString() // Convertir índice de fila a letra
        val number = (x + 1).toString() // Convertir índice de columna a número

        // Convertir a binario ASCII
        val letterBinary = letter[0].code.toString(2).padStart(8, '0')
        val numberBinary = number[0].code.toString(2).padStart(8, '0')

        return "$letterBinary,$numberBinary"
    }


    private fun drawCoordinateOnBoard(board: Board, coordinate: Pair<Int, Int>, color: Int) {
        val (x, y) = coordinate
        board.getCell(x, y).apply {
            setBackgroundColor(color)
            invalidate()
        }
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
