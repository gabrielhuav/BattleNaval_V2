package ovh.gabrielhuav.battlenaval_v2

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.os.Handler
import android.os.Looper
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
    private lateinit var delayTimerTextView: TextView

    private var running = false
    private var isConnected = false
    private var revealedCoordinates: String? = null
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
        delayTimerTextView = findViewById(R.id.tvDelayTimer)

        // Inicialización de tableros
        playerBoard = Board(this, false) { }
        playerBoardFrame.addView(playerBoard)

        enemyBoard = Board(this, true) { cell ->
            if (!running || cell.wasShot || !isConnected) return@Board
            bluetoothGameManager.makeShot("${cell.y}${cell.x + 1}")
        }
        enemyBoardFrame.addView(enemyBoard)

        // Configuración del BluetoothGameManager
        bluetoothGameManager = BluetoothGameManager(this)
        bluetoothGameManager.onMessageReceived = { message ->
            runOnUiThread { handleIncomingMessage(message) }
        }
        bluetoothGameManager.onStateChanged = { state ->
            runOnUiThread { updateServerStatus(state) }
        }
        bluetoothGameManager.onUpdateColors = { coordinate, color ->
            runOnUiThread { updateBoardColor(coordinate, color) }
        }

        // Configuración de botones
        findViewById<Button>(R.id.btnStartServer).setOnClickListener {
            bluetoothGameManager.start()
            sendPlayerShips()
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
            message.startsWith("SETUP") -> {
                Toast.makeText(this, "Barcos del enemigo configurados.", Toast.LENGTH_SHORT).show()
            }
            message.startsWith("SPECIAL") -> handleSpecialRound(message.split("|")[1])
            message.startsWith("RESULT") -> handleIncomingResult(message.split("|"))
        }
    }

    private fun handleSpecialRound(revealedCoord: String) {
        revealedCoordinates = revealedCoord
        revealedCoordinatesTextView.text = "Coordenada revelada: $revealedCoord"
        revealedCoordinatesTextView.setTextColor(Color.RED)

        // Activar el panel de respuesta
        specialRoundPanel.visibility = View.VISIBLE
        startSpecialRoundTimer()
    }

    private fun handleIncomingResult(parts: List<String>) {
        val coordinate = parts[1]
        val result = BluetoothGameManager.ShotResult.valueOf(parts[2])
        val color = bluetoothGameManager.getColorForResult(result, true)
        updateBoardColor(coordinate, color)
    }

    private fun updateBoardColor(coordinate: String, color: Int) {
        val x = coordinate[1].toString().toInt() - 1
        val y = coordinate[0] - 'A'
        val cell = if (color == Color.RED || color == Color.MAGENTA) {
            enemyBoard.getCell(x, y)
        } else {
            playerBoard.getCell(x, y)
        }
        cell.setBackgroundColor(color)
        cell.invalidate()
    }

    private fun startSpecialRoundTimer() {
        delayTimerTextView.visibility = View.VISIBLE
        delayTimerTextView.text = "Esperando respuesta..."
        Handler(Looper.getMainLooper()).postDelayed({
            delayTimerTextView.visibility = View.GONE
        }, 10000) // 10 segundos
    }

    private fun validateSpecialRoundAnswer() {
        val playerAnswer = convertedCoordinatesEditText.text.toString().trim()
        val isCorrect = bluetoothGameManager.validateSpecialRoundAnswer(playerAnswer)
        if (isCorrect) {
            Toast.makeText(this, "¡Respuesta Correcta!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Respuesta Incorrecta.", Toast.LENGTH_LONG).show()
        }
        specialRoundPanel.visibility = View.GONE
    }

    private fun sendPlayerShips() {
        val playerShipsCoordinates = playerBoard.getShipCoordinates() // List<Pair<String, String>>

        val groupedCoordinates: List<List<String>> = playerShipsCoordinates
            .groupBy { it.first }
            .values
            .map { group -> group.map { "${it.first}${it.second}" } }

        val playerShips = groupedCoordinates.mapIndexed { index, coordinates ->
            val color = when (index) {
                0 -> Color.GREEN
                1 -> Color.RED
                else -> Color.GRAY
            }
            BluetoothGameManager.Ship(
                name = "Ship$index",
                color = color,
                coordinates = coordinates.toMutableList()
            )
        }

        bluetoothGameManager.setPlayerShips(playerShips, isServer = true)
    }

    private fun updateServerStatus(state: BluetoothGameManager.State) {
        serverStatusTextView.text = when (state) {
            BluetoothGameManager.State.LISTEN -> "Estado: Esperando Conexiones..."
            BluetoothGameManager.State.CONNECTING -> "Estado: Conectando..."
            BluetoothGameManager.State.CONNECTED -> "Estado: Conectado"
            BluetoothGameManager.State.SPECIAL_ROUND -> "Estado: Ronda Especial"
            else -> "Estado: Desconectado"
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

    companion object {
        private const val REQUEST_CONNECT_DEVICE = 1
    }
}
