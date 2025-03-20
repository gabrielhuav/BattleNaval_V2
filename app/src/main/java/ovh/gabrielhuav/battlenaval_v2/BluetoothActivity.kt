package ovh.gabrielhuav.battlenaval_v2

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlin.random.Random

class BluetoothActivity : AppCompatActivity() {
    private lateinit var bluetoothGameManager: BluetoothGameManager
    private lateinit var playerBoardFrame: FrameLayout
    private lateinit var enemyBoardFrame: FrameLayout
    private lateinit var playerBoard: Board
    private lateinit var enemyBoard: Board
    private lateinit var serverStatusTextView: TextView
    private var running = false
    private var isConnected = false

    // Para el desafío de conversión binaria
    private lateinit var binaryConversionDialog: BinaryConversionDialog
    private var lastRevealedCell: Pair<Int, Int>? = null

    // Límites de zoom
    private val minZoom = 0.5f
    private val maxZoom = 2.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)

        playerBoardFrame = findViewById(R.id.playerBoardFrame)
        enemyBoardFrame = findViewById(R.id.enemyBoardFrame)
        serverStatusTextView = findViewById(R.id.tvBluetoothStatus)
        val startServerButton = findViewById<Button>(R.id.btnStartServer)
        val connectButton = findViewById<Button>(R.id.btnConnectToDevice)
        val backToMenuButton = findViewById<Button>(R.id.btnBackToMenu)
        val zoomInButton = findViewById<Button>(R.id.btnZoomIn)
        val zoomOutButton = findViewById<Button>(R.id.btnZoomOut)

        // Inicializar el diálogo de conversión binaria
        binaryConversionDialog = BinaryConversionDialog(this)

        // Configuración de botones de zoom
        zoomInButton.setOnClickListener {
            adjustZoom(0.1f)
        }

        zoomOutButton.setOnClickListener {
            adjustZoom(-0.1f)
        }

        bluetoothGameManager = BluetoothGameManager(this)

        // Listener para mensajes recibidos
        bluetoothGameManager.onMessageReceived = { message ->
            runOnUiThread {
                val parts = message.split(",")
                when (parts[0]) {
                    "SHOOT" -> handleIncomingShoot(parts)
                    "RESULT" -> handleIncomingResult(parts)
                    "REVEAL" -> handleIncomingReveal(parts)
                }
            }
        }

        // Listener para el estado del servidor
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
    }

    private fun startNewGame() {
        running = true
        lastRevealedCell = null

        // Limpia los tableros existentes
        playerBoardFrame.removeAllViews()
        enemyBoardFrame.removeAllViews()

        // Crear tablero del jugador
        playerBoard = Board(this, false) { }
        playerBoardFrame.addView(playerBoard)
        placePlayerShips()

        // Crear tablero del enemigo
        enemyBoard = Board(this, true) { cell ->
            if (!running || cell.wasShot || !isConnected) return@Board

            // Mostrar el desafío de conversión binaria antes de disparar
            showBinaryConversionChallenge(cell.x, cell.y)
        }
        enemyBoardFrame.addView(enemyBoard)

        // Revelar una celda aleatoria del enemigo al inicio
        sendRevealedCell()
    }

    private fun showBinaryConversionChallenge(clickedX: Int, clickedY: Int) {
        // Determinar si debemos reutilizar la última coordenada
        val reuseLastCoordinate = lastRevealedCell != null
        val cellToReveal = lastRevealedCell ?: findEnemyShipCell()

        if (cellToReveal != null) {
            Log.d("BluetoothActivity", "Mostrando desafío: coordenada revelada (${cellToReveal.first},${cellToReveal.second}) - ${('A' + cellToReveal.second)}${cellToReveal.first+1}")
        }

        binaryConversionDialog.showConversionChallenge(
            cellToReveal?.first,
            cellToReveal?.second,
            onCorrectConversion = { shootX, shootY, isCorrectConversion ->
                // Si la conversión fue correcta, disparar en la coordenada revelada
                // Si fue incorrecta, disparar en la coordenada que ingresó el usuario

                val cell = enemyBoard.getCell(shootX, shootY)

                // Solo permitir acertar a barcos si la conversión fue correcta
                if (!cell.wasShot) {
                    if (isCorrectConversion) {
                        // Conversión correcta: disparo normal
                        sendShootMessage(shootX, shootY)
                        Log.d("BluetoothActivity", "Disparo correcto enviado para ${BinaryConversionHelper.coordsToString(shootX, shootY)}")
                    } else {
                        // Conversión incorrecta: asegurarse de no acertar a un barco por casualidad
                        if (cell.ship != null) {
                            // Si hay un barco, no permitir acertar - buscar otra celda vacía
                            val emptyCell = findSafeEmptyCell()
                            if (emptyCell != null) {
                                val emptyCellX = emptyCell.first
                                val emptyCellY = emptyCell.second
                                sendShootMessage(emptyCellX, emptyCellY)
                                Log.d("BluetoothActivity", "Conversión incorrecta, redirigiendo disparo a celda vacía: ${BinaryConversionHelper.coordsToString(emptyCellX, emptyCellY)}")

                                Toast.makeText(this, "Disparo incorrecto en ${BinaryConversionHelper.coordsToString(emptyCellX, emptyCellY)}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // No hay barco, disparar normalmente
                            sendShootMessage(shootX, shootY)
                            Log.d("BluetoothActivity", "Disparo incorrecto enviado para ${BinaryConversionHelper.coordsToString(shootX, shootY)}")

                            Toast.makeText(this, "Disparo incorrecto en ${BinaryConversionHelper.coordsToString(shootX, shootY)}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Ya has disparado en esa coordenada", Toast.LENGTH_SHORT).show()
                }

                // Reiniciar la última celda revelada
                if (isCorrectConversion) {
                    lastRevealedCell = null

                    // Revelar una nueva celda para el próximo turno
                    if (!reuseLastCoordinate) {
                        sendRevealedCell()
                    }
                }
                // Si la conversión fue incorrecta, mantener la misma coordenada revelada
            },
            reuseLastCoordinate = reuseLastCoordinate
        )
    }

    // Encontrar una celda vacía para disparos fallidos, para evitar acertar por casualidad
    private fun findSafeEmptyCell(): Pair<Int, Int>? {
        val emptyCells = mutableListOf<Pair<Int, Int>>()

        for (y in 0 until 7) {
            for (x in 0 until 7) {
                val cell = enemyBoard.getCell(x, y)
                if (cell.ship == null && !cell.wasShot) {
                    emptyCells.add(Pair(x, y))
                }
            }
        }

        return if (emptyCells.isNotEmpty()) {
            emptyCells.random()
        } else {
            null
        }
    }

    private fun findEnemyShipCell(): Pair<Int, Int>? {
        // Recolectar SOLO celdas con barcos no disparados
        val shipCells = mutableListOf<Pair<Int, Int>>()

        for (y in 0 until 7) {
            for (x in 0 until 7) {
                val cell = enemyBoard.getCell(x, y)
                if (cell.ship != null && !cell.wasShot) {
                    shipCells.add(Pair(x, y))
                }
            }
        }

        // Si hay celdas disponibles, seleccionar una al azar
        return if (shipCells.isNotEmpty()) {
            Log.d("BluetoothActivity", "Encontradas ${shipCells.size} celdas con barcos sin disparar")
            val selected = shipCells.random()
            Log.d("BluetoothActivity", "Revelando celda con barco en: (${selected.first},${selected.second}) = ${BinaryConversionHelper.coordsToString(selected.first, selected.second)}")
            selected
        } else {
            Log.d("BluetoothActivity", "No se encontraron celdas con barcos sin disparar")
            null
        }
    }

    private fun sendRevealedCell() {
        val cellToReveal = findEnemyShipCell()
        if (cellToReveal != null) {
            // Solo enviar coordenadas de celdas con barcos
            bluetoothGameManager.sendMessage("REVEAL,${cellToReveal.first},${cellToReveal.second}")
            Log.d("BluetoothActivity", "Enviando coordenada revelada: ${BinaryConversionHelper.coordsToString(cellToReveal.first, cellToReveal.second)}")
        } else {
            Log.d("BluetoothActivity", "No hay celdas con barcos para revelar")
        }
    }

    private fun handleIncomingReveal(parts: List<String>) {
        val x = parts[1].toInt()
        val y = parts[2].toInt()
        lastRevealedCell = Pair(x, y)

        // Notificar al usuario que tiene una nueva coordenada para convertir
        runOnUiThread {
            Toast.makeText(this, "¡Nueva coordenada binaria revelada!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendShootMessage(x: Int, y: Int) {
        bluetoothGameManager.sendMessage("SHOOT,$x,$y")
    }

    private fun placePlayerShips() {
        val shipData = listOf(
            Pair(4, Color.GREEN),
            Pair(3, Color.RED),
            Pair(2, Color.GRAY)
        )

        for ((size, color) in shipData) {
            while (true) {
                val x = Random.nextInt(0, 7)
                val y = Random.nextInt(0, 7)
                val vertical = Random.nextBoolean()

                if (playerBoard.placeShip(Ship(size, vertical, color), x, y)) break
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
            cell.ship = Ship(1, false) // Ship dummy para mostrar el hit
        }
        cell.invalidate()

        if (enemyBoard.ships == 0) {
            Toast.makeText(this, "¡GANASTE!", Toast.LENGTH_LONG).show()
            running = false
        }
    }

    private fun updateServerStatus(state: BluetoothGameManager.State) {
        when (state) {
            BluetoothGameManager.State.LISTEN -> {
                serverStatusTextView.text = "Estado: Esperando Conexiones..."
                isConnected = false
            }
            BluetoothGameManager.State.CONNECTING -> {
                serverStatusTextView.text = "Estado: Conectando..."
                isConnected = false
            }
            BluetoothGameManager.State.CONNECTED -> {
                serverStatusTextView.text = "Estado: Conectado"
                isConnected = true
                startNewGame()
            }
            BluetoothGameManager.State.NONE -> {
                serverStatusTextView.text = "Estado: Desconectado"
                isConnected = false
            }
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
                try {
                    // Obtener el dispositivo utilizando el adaptador Bluetooth directamente
                    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
                        if (device != null) {
                            bluetoothGameManager.connect(device)
                        } else {
                            Toast.makeText(this, "Dispositivo no encontrado", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "No se tienen permisos de Bluetooth", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al conectar: ${e.message}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
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