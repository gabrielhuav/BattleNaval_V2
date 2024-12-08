package ovh.gabrielhuav.battlenaval_v2

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
    private lateinit var binaryCoordinatesTextView: TextView
    private lateinit var attackCoordinatesEditText: EditText
    private lateinit var sendAttackButton: Button

    private var running = false
    private var isConnected = false
    private var specialRoundActive = false
    private var revealedPlayerCell: Cell? = null
    private var revealedEnemyCell: Cell? = null
    private var isServer: Boolean = false
    private var playerCorrectAnswer: String = ""
    private var opponentCorrectAnswer: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)

        initializeViews()
        checkBluetoothPermissions()

        bluetoothGameManager = BluetoothGameManager(this)

        bluetoothGameManager.onMessageReceived = { message ->
            runOnUiThread { handleIncomingMessage(message) }
        }

        bluetoothGameManager.onStateChanged = { state ->
            runOnUiThread { updateServerStatus(state) }
        }

        findViewById<Button>(R.id.btnStartServer).setOnClickListener {
            isServer = true
            bluetoothGameManager.start()
            Toast.makeText(this, "Servidor Iniciado", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnConnectToDevice).setOnClickListener {
            isServer = false
            val intent = Intent(this, DeviceListActivity::class.java)
            startActivityForResult(intent, REQUEST_CONNECT_DEVICE)
        }

        findViewById<Button>(R.id.btnBackToMenu).setOnClickListener {
            bluetoothGameManager.stop()
            finish()
        }

        sendAttackButton.setOnClickListener {
            if (!specialRoundActive) performAttack()
        }
    }

    private fun initializeViews() {
        playerBoardFrame = findViewById(R.id.playerBoardFrame)
        enemyBoardFrame = findViewById(R.id.enemyBoardFrame)
        serverStatusTextView = findViewById(R.id.tvBluetoothStatus)
        specialRoundPanel = findViewById(R.id.specialRoundPanel)
        revealedCoordinatesTextView = findViewById(R.id.tvRevealedCoordinates)
        binaryCoordinatesTextView = findViewById(R.id.tvBinaryCoordinates)
        attackCoordinatesEditText = findViewById(R.id.etAttackCoordinates)
        sendAttackButton = findViewById(R.id.btnSendAttack)
    }

    private fun startNewGame() {
        running = true
        specialRoundActive = false

        runOnUiThread {
            playerBoardFrame.removeAllViews()
            enemyBoardFrame.removeAllViews()

            playerBoard = Board(this, false) { /* Optional click handler */ }
            playerBoardFrame.addView(playerBoard)
            placeShips(playerBoard)

            enemyBoard = Board(this, true) { cell ->
                if (!running || cell.wasShot || !isConnected) return@Board
                bluetoothGameManager.sendMessage("SHOOT,${cell.x},${cell.y}")
            }
            enemyBoardFrame.addView(enemyBoard)

            placeShips(enemyBoard)
        }
    }

    private fun placeShips(board: Board) {
        val shipConfigs = listOf(
            Ship(4, vertical = true, color = Color.GREEN),
            Ship(3, vertical = true, color = Color.RED),
            Ship(2, vertical = true, color = Color.GRAY)
        )

        shipConfigs.forEach { ship ->
            var placed = false
            while (!placed) {
                val x = Random.nextInt(0, 7)
                val y = Random.nextInt(0, 7)
                val vertical = Random.nextBoolean()

                if (board.placeShip(ship.copy(vertical = vertical), x, y)) {
                    placed = true
                }
            }
        }
    }

    private fun performAttack() {
        val input = attackCoordinatesEditText.text.toString().trim().uppercase()
        if (input.length < 2) {
            Toast.makeText(this, "Coordenadas inválidas", Toast.LENGTH_SHORT).show()
            return
        }

        val x = input[1].toString().toInt() - 1
        val y = input[0] - 'A'

        if (x in 0..6 && y in 0..6) {
            bluetoothGameManager.sendMessage("SHOOT,$x,$y")
            attackCoordinatesEditText.text.clear()
        } else {
            Toast.makeText(this, "Coordenadas fuera de rango", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleIncomingMessage(message: String) {
        val parts = message.split(",")
        when (parts[0]) {
            "SHOOT" -> handleIncomingShoot(parts)
            "RESULT" -> handleIncomingResult(parts)
            "REVEAL" -> handleReveal(parts) // Nuevo caso para manejar la celda revelada
        }
    }

    private fun handleReveal(parts: List<String>) {
        val x = parts[1].toInt()
        val y = parts[2].toInt()
        val revealedCell = enemyBoard.getCell(x, y)
        revealedCell.setBackgroundColor(Color.MAGENTA) // Pintar la celda en magenta
        revealedCell.text = "R" // Texto para indicar revelado
        revealedCell.invalidate()

        // Actualizar el texto mostrado en la interfaz
        val coordinate = "${('A' + y)}${x + 1}"
        revealedCoordinatesTextView.text = "Respuesta revelada al Enemigo: $coordinate"
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
            cell.setBackgroundColor(Color.RED)
        } else {
            cell.setBackgroundColor(Color.BLUE)
        }
        cell.invalidate()

        if (enemyBoard.ships == 0) {
            Toast.makeText(this, "¡GANASTE!", Toast.LENGTH_LONG).show()
            running = false
        }
    }

    private fun updateServerStatus(state: BluetoothGameManager.BluetoothState) {
        when (state) {
            BluetoothGameManager.BluetoothState.LISTENING -> serverStatusTextView.text = "Estado: Esperando Conexiones..."
            BluetoothGameManager.BluetoothState.CONNECTING -> serverStatusTextView.text = "Estado: Conectando..."
            BluetoothGameManager.BluetoothState.CONNECTED -> {
                serverStatusTextView.text = "Estado: Conectado"
                isConnected = true
                startNewGame()
            }
            BluetoothGameManager.BluetoothState.NONE -> serverStatusTextView.text = "Estado: Desconectado"
        }
    }

    private fun revealEnemyCell() {
        val randomCell = enemyBoard.getRandomShipCell()
        bluetoothGameManager.sendMessage("REVEAL,${randomCell.x},${randomCell.y}")
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

    companion object {
        private const val REQUEST_CONNECT_DEVICE = 1
    }
}
