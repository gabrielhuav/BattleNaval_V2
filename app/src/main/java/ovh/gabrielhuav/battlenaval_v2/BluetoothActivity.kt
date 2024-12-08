package ovh.gabrielhuav.battlenaval_v2

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
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

    private var running = false
    private var isConnected = false
    private var revealedCoordinates: Pair<String, String>? = null
    private var enemyShips: List<Pair<String, String>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)

        checkBluetoothPermissions()

        // Inicialización de vistas
        playerBoardFrame = findViewById(R.id.playerBoardFrame)
        enemyBoardFrame = findViewById(R.id.enemyBoardFrame)
        serverStatusTextView = findViewById(R.id.tvBluetoothStatus)
        specialRoundPanel = findViewById(R.id.specialRoundPanel)
        revealedCoordinatesTextView = findViewById(R.id.tvRevealedCoordinates)
        enemyCoordinatesTextView = findViewById(R.id.tvEnemyCoordinates)
        convertedCoordinatesEditText = findViewById(R.id.etConvertedCoordinates)
        confirmCoordinatesButton = findViewById(R.id.btnConfirmCoordinates)

        // Inicialización de tableros
        playerBoard = Board(this, false) { }
        playerBoardFrame.addView(playerBoard)

        // Configuración del BluetoothGameManager
        bluetoothGameManager = BluetoothGameManager(this)
        bluetoothGameManager.onMessageReceived = { message ->
            runOnUiThread { handleIncomingMessage(message) }
        }
        bluetoothGameManager.onStateChanged = { state ->
            runOnUiThread { updateServerStatus(state) }
        }

        // Configuración de botones
        findViewById<Button>(R.id.btnStartServer).setOnClickListener {
            bluetoothGameManager.start()
            sendPlayerShips() // Ahora playerBoard ya está inicializado
            Toast.makeText(this, "Servidor Iniciado", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnConnectToDevice).setOnClickListener {
            val intent = Intent(this, DeviceListActivity::class.java)
            startActivityForResult(intent, REQUEST_CONNECT_DEVICE)
        }

        findViewById<Button>(R.id.btnBackToMenu).setOnClickListener {
            bluetoothGameManager.stop()
            finish()
        }

        confirmCoordinatesButton.setOnClickListener {
            validateSpecialRoundAnswer()
        }
    }
    private fun handleIncomingMessage(message: String) {
        when {
            message.startsWith("SHIPS") -> {
                enemyShips = deserializeShipPositions(message.substring(6))
                println("Barcos recibidos del enemigo: $enemyShips")
                if (enemyShips.isNotEmpty()) {
                    Toast.makeText(this, "Barcos del enemigo recibidos.", Toast.LENGTH_SHORT).show()

                    // Inicia la ronda especial una vez que se reciben los barcos
                    startSpecialRound()
                } else {
                    Toast.makeText(this, "Error: No se recibieron barcos del enemigo.", Toast.LENGTH_SHORT).show()
                }
            }
            message.startsWith("SPECIAL") -> handleSpecialRound(message.split(","))
            // Otros casos...
        }
    }




    private fun startSpecialRound() {
        try {
            if (enemyShips.isEmpty()) {
                throw IllegalStateException("No se han recibido las posiciones de los barcos del enemigo.")
            }

            // Selecciona un barco enemigo al azar
            val randomEnemyShip = enemyShips.random()
            revealedCoordinates = randomEnemyShip // Asigna las coordenadas reveladas correctamente

            val binaryCoordinates = "${convertToBinary(randomEnemyShip.first)},${convertToBinary(randomEnemyShip.second)}"
            enemyCoordinatesTextView.text = "Enemy Coordinates (binary): $binaryCoordinates"
            revealedCoordinatesTextView.text = "Text Coordinates: ${randomEnemyShip.first}${randomEnemyShip.second}"

            val x = randomEnemyShip.second.toInt() - 1
            val y = randomEnemyShip.first[0] - 'A'
            markRevealedCell(x, y)

            // Envía las coordenadas reveladas al oponente
            bluetoothGameManager.sendMessage("SPECIAL,${randomEnemyShip.first},${randomEnemyShip.second}")
        } catch (e: Exception) {
            Toast.makeText(this, "Error al iniciar la ronda especial: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun markRevealedCell(x: Int, y: Int) {
        try {
            val cell = enemyBoard.getCell(x, y)
            cell.setBackgroundColor(Color.MAGENTA) // Cambia el color de la celda seleccionada
            cell.text = "R" // Indica que esta celda está revelada
            cell.invalidate() // Fuerza el redibujado de la celda
            println("Celda marcada como revelada en posición ($x, $y)") // Depuración
        } catch (e: Exception) {
            Toast.makeText(this, "Error al marcar la celda revelada: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }



    private fun startNewGame() {
        running = true

        playerBoardFrame.removeAllViews()
        enemyBoardFrame.removeAllViews()

        playerBoard = Board(this, false) { }
        playerBoardFrame.addView(playerBoard)
        placePlayerShips()

        enemyBoard = Board(this, true) { cell ->
            if (!running || cell.wasShot || !isConnected) return@Board
            bluetoothGameManager.sendMessage("SHOOT,${cell.x},${cell.y}")
        }
        enemyBoardFrame.addView(enemyBoard)

        placeEnemyShips()

        // Envía los barcos del jugador
        sendPlayerShips()

        // Si se reciben barcos, comienza la ronda especial
        if (enemyShips.isNotEmpty()) {
            startSpecialRound()
        } else {
            Toast.makeText(this, "Esperando barcos del enemigo...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendPlayerShips() {
        val playerShips = playerBoard.getShipCoordinates()
        println("Enviando barcos del jugador: $playerShips") // Depuración
        bluetoothGameManager.sendMessage("SHIPS,${serializeShipPositions(playerShips)}")
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

    private fun placeEnemyShips() {
        val shipData = listOf(Pair(4, Color.GREEN), Pair(3, Color.RED), Pair(2, Color.GRAY))
        for ((size, color) in shipData) {
            while (true) {
                val x = Random.nextInt(0, 7)
                val y = Random.nextInt(0, 7)
                val vertical = Random.nextBoolean()

                if (enemyBoard.placeShip(Ship(size, vertical, color), x, y)) break
            }
        }
    }

    private fun validateSpecialRoundAnswer() {
        if (revealedCoordinates == null) {
            Toast.makeText(this, "Error: No se han revelado coordenadas para validar.", Toast.LENGTH_SHORT).show()
            return
        }

        val input = convertedCoordinatesEditText.text.toString().trim()
        val correctAnswer = "${revealedCoordinates!!.first}${revealedCoordinates!!.second}"

        if (input == correctAnswer) {
            Toast.makeText(this, "¡Respuesta Correcta!", Toast.LENGTH_SHORT).show()
            bluetoothGameManager.sendMessage("SPECIAL_RESULT,true")
        } else {
            Toast.makeText(this, "Respuesta Incorrecta. Coordenadas correctas: $correctAnswer", Toast.LENGTH_LONG).show()
            bluetoothGameManager.sendMessage("SPECIAL_RESULT,false")
        }

        // Ocultar los elementos de la UI de la ronda especial
        specialRoundPanel.visibility = View.GONE
        convertedCoordinatesEditText.visibility = View.GONE
        confirmCoordinatesButton.visibility = View.GONE
    }

    private fun handleSpecialRoundResult(parts: List<String>) {
        val isCorrect = parts[1].toBoolean()

        runOnUiThread {
            if (isCorrect) {
                Toast.makeText(this, "Opponent solved the special round correctly!", Toast.LENGTH_SHORT).show()
                // Optionally, add a reward or bonus
            } else {
                Toast.makeText(this, "Opponent failed the special round!", Toast.LENGTH_SHORT).show()
                // Optionally, add a penalty
            }
        }
    }

    private fun handleIncomingShoot(parts: List<String>) {
        val x = parts[1].toInt()
        val y = parts[2].toInt()
        val cell = playerBoard.getCell(x, y)
        val hit = cell.shoot()
        bluetoothGameManager.sendMessage("RESULT,$x,$y,$hit")

        if (playerBoard.ships == 0) {
            Toast.makeText(this, "¡PERDISTE!", Toast.LENGTH_LONG).show()
            running = false
        }
    }

    private fun handleIncomingResult(parts: List<String>) {
        val x = parts[1].toInt()
        val y = parts[2].toInt()
        val hit = parts[3].toBoolean()
        val cell = enemyBoard.getCell(x, y)
        cell.wasShot = true
        if (hit) {
            cell.ship = Ship(1, false)
        }
        cell.invalidate()

        if (enemyBoard.ships == 0) {
            Toast.makeText(this, "¡GANASTE!", Toast.LENGTH_LONG).show()
            running = false
        }
    }

    private fun handleSpecialRound(parts: List<String>) {
        if (parts.size >= 3) {
            val revealedRow = parts[1]
            val revealedColumn = parts[2]

            // Mark the revealed cell for the current player
            val x = revealedColumn.toInt() - 1
            val y = revealedRow[0] - 'A'

            runOnUiThread {
                markRevealedCell(x, y)

                // Optional: Show a toast with the revealed coordinates
                Toast.makeText(this, "Special Round: Revealed $revealedRow$revealedColumn", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateServerStatus(state: BluetoothGameManager.State) {
        when (state) {
            BluetoothGameManager.State.LISTEN -> serverStatusTextView.text = "Estado: Esperando Conexiones..."
            BluetoothGameManager.State.CONNECTING -> serverStatusTextView.text = "Estado: Conectando..."
            BluetoothGameManager.State.CONNECTED -> {
                serverStatusTextView.text = "Estado: Conectado"
                isConnected = true
                startNewGame()
            }
            BluetoothGameManager.State.NONE -> serverStatusTextView.text = "Estado: Desconectado"
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CONNECT_DEVICE && resultCode == RESULT_OK) {
            try {
                val deviceAddress = data?.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS)
                if (deviceAddress != null) {
                    val device = bluetoothGameManager.getDeviceByAddress(deviceAddress)
                    if (device != null) {
                        bluetoothGameManager.connect(device)
                    } else {
                        Toast.makeText(this, "Dispositivo no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error al conectar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CONNECT_DEVICE = 1
    }

    // Serialización y conversión
    private fun serializeShipPositions(ships: List<Pair<String, String>>): String {
        return ships.joinToString(";") { "${it.first},${it.second}" }
    }

    private fun deserializeShipPositions(data: String): List<Pair<String, String>> {
        return data.split(";").map {
            val parts = it.split(",")
            Pair(parts[0], parts[1]) // Asegúrate de que estas coordenadas sean válidas
        }
    }


    private fun convertToBinary(coordinate: String): String {
        return coordinate.map { char ->
            char.code.toString(2).padStart(8, '0')
        }.joinToString("")
    }
}
