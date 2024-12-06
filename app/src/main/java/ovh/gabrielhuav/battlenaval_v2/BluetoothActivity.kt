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
    private lateinit var delayTextView: TextView

    private var running = false
    private var isConnected = false
    private var currentTurnPlayer = 1 // 1 para jugador actual, 2 para enemigo
    private var revealedCoordinates: Pair<String, String>? = null

    // Límites de zoom
    private val minZoom = 0.5f
    private val maxZoom = 2.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)

        // Verificar permisos de Bluetooth
        checkBluetoothPermissions()

        // Configuración de vistas
        playerBoardFrame = findViewById(R.id.playerBoardFrame)
        enemyBoardFrame = findViewById(R.id.enemyBoardFrame)
        serverStatusTextView = findViewById(R.id.tvBluetoothStatus)
        specialRoundPanel = findViewById(R.id.specialRoundPanel)
        revealedCoordinatesTextView = findViewById(R.id.tvRevealedCoordinates)
        convertedCoordinatesEditText = findViewById(R.id.etConvertedCoordinates)
        confirmCoordinatesButton = findViewById(R.id.btnConfirmCoordinates)
        delayTextView = findViewById(R.id.tvDelayTimer)

        val startServerButton = findViewById<Button>(R.id.btnStartServer)
        val connectButton = findViewById<Button>(R.id.btnConnectToDevice)
        val backToMenuButton = findViewById<Button>(R.id.btnBackToMenu)
        val zoomInButton = findViewById<Button>(R.id.btnZoomIn)
        val zoomOutButton = findViewById<Button>(R.id.btnZoomOut)

        // Configuración de botones de zoom
        zoomInButton.setOnClickListener { adjustZoom(0.1f) }
        zoomOutButton.setOnClickListener { adjustZoom(-0.1f) }

        bluetoothGameManager = BluetoothGameManager(this)

        // Listener para mensajes recibidos
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

        // Listener para el estado del servidor
        bluetoothGameManager.onStateChanged = { state -> runOnUiThread { updateServerStatus(state) } }

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

        confirmCoordinatesButton.setOnClickListener { validateSpecialRoundAnswer() }
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

        // Iniciar la primera Ronda Especial
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

    private fun startSpecialRound() {
        // Selecciona una celda aleatoria que no haya sido revelada
        val randomCell = enemyBoard.getRandomUnrevealedCell()
        val horizontalChar = ('A'.code + randomCell.y) // Conversión para filas A-G
        val verticalBinary = randomCell.x.toString(2).padStart(3, '0') // Conversión para columnas 1-7

        val horizontalBinary = horizontalChar.toString(2).padStart(8, '0') // ASCII en 8 bits
        revealedCoordinates = Pair(horizontalBinary, verticalBinary)

        // Muestra las coordenadas generadas
        revealedCoordinatesTextView.text = "Coordenadas: $horizontalBinary,$verticalBinary"
        specialRoundPanel.visibility = View.VISIBLE

        // Marcar la celda revelada en el tablero del jugador que está revelando la información
        markRevealedCell(randomCell.x, randomCell.y)
    }

    private fun markRevealedCell(x: Int, y: Int) {
        // Obtener la celda en el tablero del jugador
        val cell = playerBoard.getCell(x, y)

        // Cambiar el color de fondo para identificarla o añadir un texto
        cell.setBackgroundColor(Color.MAGENTA) // Cambiar a un color distintivo
        cell.text = "X" // Opcional: Mostrar una "X" en la celda
        cell.invalidate() // Refrescar la celda para aplicar los cambios
    }

    private fun validateSpecialRoundAnswer() {
        // Verificar si las coordenadas ingresadas coinciden con las esperadas
        val input = convertedCoordinatesEditText.text.toString()
        val correctAnswer = "${revealedCoordinates!!.first},${revealedCoordinates!!.second}"

        if (input == correctAnswer) {
            Toast.makeText(this, "¡Respuesta Correcta!", Toast.LENGTH_SHORT).show()
            bluetoothGameManager.sendMessage("SPECIAL,$correctAnswer")
        } else {
            Toast.makeText(this, "Respuesta Incorrecta. Las coordenadas correctas son: $correctAnswer", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleSpecialRound(parts: List<String>) {
        val horizontal = parts[1]
        val vertical = parts[2]
        revealedCoordinatesTextView.text = "Coordenadas Enemigas: $horizontal, $vertical"
        specialRoundPanel.visibility = View.VISIBLE
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

    private fun adjustZoom(delta: Float) {
        val newZoom = playerBoard.scaleFactor + delta
        if (newZoom in minZoom..maxZoom) {
            playerBoard.adjustCellSize(newZoom)
            enemyBoard.adjustCellSize(newZoom)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CONNECT_DEVICE && resultCode == RESULT_OK) {
            val deviceAddress = data?.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS)
            if (deviceAddress != null) {
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

    private fun checkBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_CONNECT_DEVICE)
        }
    }

    companion object {
        private const val REQUEST_CONNECT_DEVICE = 1
    }
}
