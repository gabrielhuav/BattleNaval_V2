package ovh.gabrielhuav.battlenaval_v2

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
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
    private lateinit var specialRoundPanel: LinearLayout
    private lateinit var revealedCoordinatesTextView: TextView
    private lateinit var enemyCoordinatesTextView: TextView
    private lateinit var convertedCoordinatesEditText: EditText
    private lateinit var confirmCoordinatesButton: Button

    private var shipsPlaced = false
    private var running = false
    private var isConnected = false
    private var revealedCoordinates: Pair<String, String>? = null
    private var enemyShips: List<Pair<String, String>> = emptyList()

    private lateinit var devicePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)

        checkBluetoothPermissions()
        initializeViews() // Initialize views
        initializeBoards() // Initialize boards

        // Initialize the ActivityResultLauncher
        devicePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val deviceAddress = result.data?.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS)
                deviceAddress?.let {
                    bluetoothGameManager.connect(it) // Connect to the selected device
                    sendPlayerShipsToServer() // Send player ships to the server
                }
            }
        }

        bluetoothGameManager = BluetoothGameManager(this, playerBoard, enemyBoard, BluetoothService(this)).apply {
            onStateChanged = { state -> updateServerStatus(state) }
            onMessageReceived = { message -> handleIncomingMessage(message) }
        }

        setupButtons() // Set up the buttons
    }


    private fun initializeViews() {
        playerBoardFrame = findViewById(R.id.playerBoardFrame)
        enemyBoardFrame = findViewById(R.id.enemyBoardFrame)
        serverStatusTextView = findViewById(R.id.tvBluetoothStatus)
        specialRoundPanel = findViewById(R.id.specialRoundPanel)
        revealedCoordinatesTextView = findViewById(R.id.tvRevealedCoordinates)
        enemyCoordinatesTextView = findViewById(R.id.tvEnemyCoordinates)
        convertedCoordinatesEditText = findViewById(R.id.etConvertedCoordinates)
        confirmCoordinatesButton = findViewById(R.id.btnConfirmCoordinates)
    }

    private fun initializeBoards() {
        // Inicializar el tablero del jugador
        playerBoard = Board(this, false) { }
        playerBoardFrame.addView(playerBoard)

        // Inicializar el tablero enemigo
        enemyBoard = Board(this, true) { cell ->
            if (!running || cell.wasShot || bluetoothGameManager.getState() != BluetoothGameManager.State.CONNECTED) return@Board
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

        confirmCoordinatesButton.setOnClickListener {
            validateSpecialRoundAnswer()
        }
    }

    private fun updateServerStatus(state: BluetoothGameManager.State) {
        runOnUiThread {
            when (state) {
                BluetoothGameManager.State.LISTEN -> serverStatusTextView.text = "Estado: Esperando Conexiones..."
                BluetoothGameManager.State.CONNECTING -> serverStatusTextView.text = "Estado: Conectando..."
                BluetoothGameManager.State.CONNECTED -> {
                    serverStatusTextView.text = "Estado: Conectado"
                    isConnected = true
                    if (!shipsPlaced) {
                        placePlayerShips()
                        sendPlayerShipsToClient()
                        shipsPlaced = true
                    }
                    bluetoothGameManager.loadGameState()
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

    override fun onDestroy() {
        super.onDestroy()
        bluetoothGameManager.stop()
    }

    // Método para enviar barcos del cliente al servidor
    private fun sendPlayer2Ships() {
        val playerShips = playerBoard.getShipCoordinates()
        bluetoothGameManager.sendMessage("PLAYER2_SHIPS,${serializeShipPositions(playerShips)}")
        Toast.makeText(this, "Barcos del jugador 2 enviados al servidor.", Toast.LENGTH_SHORT).show()
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
        Toast.makeText(this, "Barcos colocados automáticamente en el tablero del jugador.", Toast.LENGTH_SHORT).show()
    }

    // Enviar barcos del servidor al cliente después de establecer la conexión
    private fun sendPlayerShipsToServer() {
        val playerShips = playerBoard.getShipCoordinates()
        val serializedShips = serializeShipPositions(playerShips)
        bluetoothGameManager.sendMessage("SHIPS,$serializedShips")
        Toast.makeText(this, "Barcos enviados al servidor: $serializedShips", Toast.LENGTH_SHORT).show()
    }

    private fun sendPlayerShipsToClient() {
        val playerShips = playerBoard.getShipCoordinates()
        val serializedShips = serializeShipPositions(playerShips)
        bluetoothGameManager.sendMessage("SHIPS,$serializedShips")
        Toast.makeText(this, "Barcos enviados al cliente: $serializedShips", Toast.LENGTH_SHORT).show()
    }

    private fun handleIncomingMessage(message: String) {
        Toast.makeText(this, "Mensaje recibido: $message", Toast.LENGTH_SHORT).show()

        if (message.startsWith("SHIPS")) {
            val shipsData = message.substringAfter(",")
            val receivedShips = deserializeShipPositions(shipsData)

            if (receivedShips.isNotEmpty()) {
                Toast.makeText(this, "Barcos del enemigo recibidos: $receivedShips", Toast.LENGTH_SHORT).show()
                drawShipsOnEnemyBoard(receivedShips)
            } else {
                Toast.makeText(this, "Error: No se recibieron barcos válidos.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Mensaje desconocido: $message", Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawShipsOnEnemyBoard(ships: List<Pair<String, String>>) {
        Toast.makeText(this, "Dibujando barcos en el tablero enemigo.", Toast.LENGTH_SHORT).show()
        enemyBoard.clearShips()

        ships.forEach { (row, col) ->
            val x = col.toIntOrNull()?.minus(1)
            val y = row[0] - 'A'

            if (x != null && x in 0..7 && y in 0..7) {
                val ship = Ship(1, false) // Aquí puedes ajustar el tamaño y orientación del barco
                if (enemyBoard.placeShip(ship, x, y)) {
                    Toast.makeText(this, "Barco colocado en posición ($x, $y).", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al colocar barco en posición ($x, $y).", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Coordenadas inválidas: ($row, $col).", Toast.LENGTH_SHORT).show()
            }
        }

        enemyBoard.invalidate()
        Toast.makeText(this, "Tablero enemigo redibujado.", Toast.LENGTH_SHORT).show()
    }


    private fun markRevealedCell(x: Int, y: Int) {
        try {
            val cell = enemyBoard.getCell(x, y)
            cell.setBackgroundColor(Color.MAGENTA) // Cambia el color de la celda seleccionada
            cell.text = "R" // Indica que esta celda está revelada
            cell.invalidate() // Fuerza el redibujado de la celda
            Toast.makeText(this, "Celda marcada como revelada en posición ($x, $y)", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al marcar la celda revelada: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun handleIncomingResult(parts: List<String>) {
        if (parts.size < 4) return
        val x = parts[1].toIntOrNull() ?: return
        val y = parts[2].toIntOrNull() ?: return
        val hit = parts[3].toBoolean()
        val cell = enemyBoard.getCell(x, y)
        cell.wasShot = true
        if (hit) {
            cell.ship = Ship(1, false) // Dibuja el impacto con un barco si fue un golpe
        }

        enemyBoard.invalidate()
        Toast.makeText(this, "Disparo enemigo en ($x, $y) fue ${if (hit) "un impacto" else "fallido"}", Toast.LENGTH_SHORT).show()

        if (enemyBoard.ships == 0) {
            Toast.makeText(this, "¡GANASTE!", Toast.LENGTH_LONG).show()
            running = false
        }
    }

    private fun handleIncomingShoot(parts: List<String>) {
        if (parts.size < 3) return
        val x = parts[1].toIntOrNull() ?: return
        val y = parts[2].toIntOrNull() ?: return
        val cell = playerBoard.getCell(x, y)
        val hit = cell.shoot()

        playerBoard.invalidate()
        Toast.makeText(this, "Disparo recibido en ($x, $y) fue ${if (hit) "un impacto" else "fallido"}", Toast.LENGTH_SHORT).show()

        bluetoothGameManager.sendMessage("RESULT,$x,$y,$hit")

        if (playerBoard.ships == 0) {
            Toast.makeText(this, "¡PERDISTE!", Toast.LENGTH_LONG).show()
            running = false
        }
    }

    private fun handleSpecialRound(parts: List<String>) {
        if (parts.size < 3) return
        val revealedRow = parts[1]
        val revealedColumn = parts[2]

        val x = revealedColumn.toInt() - 1
        val y = revealedRow[0] - 'A'

        runOnUiThread {
            markRevealedCell(x, y)
            Toast.makeText(this, "Ronda Especial: Coordenadas reveladas $revealedRow$revealedColumn", Toast.LENGTH_SHORT).show()
        }
    }


    private fun validateSpecialRoundAnswer() {
        val input = convertedCoordinatesEditText.text.toString().trim()
        val correctAnswer = "${revealedCoordinates!!.first}${revealedCoordinates!!.second}"

        if (input == correctAnswer) {
            bluetoothGameManager.sendMessage("SPECIAL_RESULT,true")
            Toast.makeText(this, "¡Respuesta Correcta!", Toast.LENGTH_SHORT).show()
        } else {
            bluetoothGameManager.sendMessage("SPECIAL_RESULT,false")
            Toast.makeText(this, "Respuesta Incorrecta.", Toast.LENGTH_SHORT).show()
        }

        specialRoundPanel.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CONNECT_DEVICE && resultCode == RESULT_OK) {
            val deviceAddress = data?.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS)
            deviceAddress?.let {
                bluetoothGameManager.connect(it) // Conecta al servidor
                if (!shipsPlaced) {
                    placePlayerShips() // Coloca los barcos en el cliente
                    sendPlayerShipsToServer() // Envía los barcos al servidor
                    shipsPlaced = true
                }
            }
        }
    }


    private fun serializeShipPositions(ships: List<Pair<String, String>>): String {
        return ships.joinToString(";") { "${it.first},${it.second}" }
    }

    private fun deserializeShipPositions(data: String): List<Pair<String, String>> {
        return data.split(";").mapNotNull {
            val parts = it.split(",")
            if (parts.size == 2) Pair(parts[0], parts[1]) else null
        }
    }

    private fun convertToBinary(coordinate: String): String {
        return coordinate.map { it.code.toString(2).padStart(8, '0') }.joinToString("")
    }

    companion object {
        private const val REQUEST_CONNECT_DEVICE = 1
    }
}
