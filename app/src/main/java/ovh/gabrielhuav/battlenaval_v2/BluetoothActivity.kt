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
    private lateinit var convertedCoordinatesEditText: EditText
    private lateinit var confirmCoordinatesButton: Button

    private var running = false
    private var isConnected = false
    private var revealedCoordinates: Pair<String, String>? = null

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
        convertedCoordinatesEditText = findViewById(R.id.etConvertedCoordinates)
        confirmCoordinatesButton = findViewById(R.id.btnConfirmCoordinates)

        val startServerButton = findViewById<Button>(R.id.btnStartServer)
        val connectButton = findViewById<Button>(R.id.btnConnectToDevice)
        val backToMenuButton = findViewById<Button>(R.id.btnBackToMenu)

        bluetoothGameManager = BluetoothGameManager(this)

        bluetoothGameManager.onMessageReceived = { message ->
            runOnUiThread {
                val parts = message.split(",")
                when (parts[0]) {
                    "SHOOT" -> handleIncomingShoot(parts)
                    "RESULT" -> handleIncomingResult(parts)
                    "SPECIAL" -> handleSpecialRound(parts)
                }
            }
        }

        bluetoothGameManager.onStateChanged = { state ->
            runOnUiThread { updateServerStatus(state) }
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

        confirmCoordinatesButton.setOnClickListener {
            validateSpecialRoundAnswer()
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
        startSpecialRound()
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

    private fun startSpecialRound() {
        try {
            // Seleccionar una celda con un barco del enemigo
            val randomShipCell = playerBoard.getRandomShipCell() // Cambia a enemyBoard si es jugador 2 relevando

            // Conversión de coordenadas
            val horizontalChar = ('A'.code + randomShipCell.y).toChar()
            val verticalIndex = randomShipCell.x + 1

            val horizontalBinary = horizontalChar.code.toString(2).padStart(8, '0')
            val verticalBinary = verticalIndex.toString(2).padStart(3, '0')

            // Guardar coordenadas
            revealedCoordinates = Pair(horizontalBinary, verticalBinary)
            val correctAnswer = "$horizontalChar$verticalIndex"

            // Mostrar las coordenadas al otro jugador
            revealedCoordinatesTextView.text =
                "Coordenadas: $horizontalBinary,$verticalBinary\nRespuesta para el enemigo: $correctAnswer"
            specialRoundPanel.visibility = View.VISIBLE
            convertedCoordinatesEditText.visibility = View.VISIBLE
            confirmCoordinatesButton.visibility = View.VISIBLE

            // Marcar la celda seleccionada en el tablero del jugador
            markRevealedCell(randomShipCell.x, randomShipCell.y)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al iniciar la ronda especial: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }



    private fun validateSpecialRoundAnswer() {
        val input = convertedCoordinatesEditText.text.toString().trim() // Respuesta del jugador 2
        val horizontalChar = revealedCoordinates!!.first.toInt(2).toChar() // Decodifica ASCII
        val verticalIndex = revealedCoordinates!!.second.toInt(2) // Decodifica Binario
        val correctAnswer = "$horizontalChar$verticalIndex"

        if (input == correctAnswer) {
            Toast.makeText(this, "¡Respuesta Correcta!", Toast.LENGTH_SHORT).show()
            bluetoothGameManager.sendMessage("SPECIAL,$correctAnswer")
        } else {
            Toast.makeText(
                this,
                "Respuesta Incorrecta. Coordenadas correctas: $correctAnswer",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun markRevealedCell(x: Int, y: Int) {
        try {
            val cell = playerBoard.getCell(x, y) // Marcar celda en el tablero del jugador que revela
            if (cell.ship != null) {
                cell.setBackgroundColor(Color.MAGENTA)
                cell.text = "X"
                cell.invalidate()
            } else {
                println("Error: La celda marcada no pertenece a un barco.")
            }
        } catch (e: Exception) {
            println("Error al marcar la celda revelada: ${e.message}")
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
        Toast.makeText(this, "Ronda Especial: ${parts.joinToString(",")}", Toast.LENGTH_SHORT).show()
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
}
