package ovh.gabrielhuav.battlenaval_v2.battlenavalbinario

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import ovh.gabrielhuav.battlenaval_v2.R
import ovh.gabrielhuav.battlenaval_v2.ThemeManager
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
    private var isGameInitialized = false
    private val TAG = "BluetoothActivity"

    // Para el desafío de conversión binaria
    private lateinit var binaryConversionDialog: BinaryConversionDialog
    private var lastRevealedCell: Pair<Int, Int>? = null
    private var enemyShipCells = mutableListOf<Pair<Int, Int>>()

    // Límites de zoom
    private val minZoom = 0.5f
    private val maxZoom = 2.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
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
                Log.d(TAG, "Mensaje recibido: $message")
                try {
                    val parts = message.split(",")
                    when (parts[0]) {
                        "SHOOT" -> handleIncomingShoot(parts)
                        "RESULT" -> handleIncomingResult(parts)
                        "INIT_GAME" -> handleGameInitialization(parts)
                        "SHIP_DATA" -> handleShipData(message.substring(10)) // Quitar "SHIP_DATA," del inicio
                        "REVEAL" -> handleIncomingReveal(parts)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al procesar mensaje: ${e.message}")
                    e.printStackTrace()
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
        isGameInitialized = false
        lastRevealedCell = null
        enemyShipCells.clear()

        // Limpia los tableros existentes
        playerBoardFrame.removeAllViews()
        enemyBoardFrame.removeAllViews()

        // Crear tablero del jugador
        playerBoard = Board(this, false) { }
        playerBoardFrame.addView(playerBoard)
        placePlayerShips()

        // Crear tablero del enemigo
        enemyBoard = Board(this, true) { cell ->
            if (!running || cell.wasShot || !isConnected || !isGameInitialized) return@Board

            // Mostrar el desafío de conversión binaria antes de disparar
            showBinaryConversionChallenge(cell.x, cell.y)
        }
        enemyBoardFrame.addView(enemyBoard)

        // Enviar un mensaje para inicializar el juego
        sendGameInitMessage()
    }

    // Nuevo método para enviar datos de inicialización del juego
    private fun sendGameInitMessage() {
        // Enviar mensaje para inicializar el juego
        bluetoothGameManager.sendMessage("INIT_GAME")

        // Enviar datos de nuestros barcos
        sendShipData()
    }

    // Nuevo método para enviar los datos de los barcos al oponente
    private fun sendShipData() {
        // Recolectar datos de barcos en formato serializable
        val shipDataList = mutableListOf<ShipData>()

        // Para cada celda, verificar si tiene un barco
        for (y in 0 until 7) {
            for (x in 0 until 7) {
                val cell = playerBoard.getCell(x, y)
                if (cell.ship != null) {
                    // Verificar si ya hemos agregado este barco (por su posición y tamaño)
                    val shipExists = shipDataList.any {
                        (it.x == x && it.y == y) ||
                                (it.vertical && x == it.x && y >= it.y && y < it.y + it.size) ||
                                (!it.vertical && y == it.y && x >= it.x && x < it.x + it.size)
                    }

                    if (!shipExists) {
                        val ship = cell.ship!!
                        shipDataList.add(ShipData(
                            size = ship.type,
                            x = x,
                            y = y,
                            vertical = ship.vertical
                        ))
                    }
                }
            }
        }

        // Convertir a JSON y enviar
        try {
            val gson = Gson()
            val shipDataJson = gson.toJson(shipDataList)
            Log.d(TAG, "Enviando datos de barcos: $shipDataJson")
            bluetoothGameManager.sendMessage("SHIP_DATA,$shipDataJson")
        } catch (e: Exception) {
            Log.e(TAG, "Error al serializar datos de barcos: ${e.message}")
            e.printStackTrace()
        }
    }

    // Nuevo método para manejar la inicialización del juego
    private fun handleGameInitialization(parts: List<String>) {
        Log.d(TAG, "Iniciando nuevo juego a solicitud del oponente")

        // Si no hemos inicializado aún nuestro juego, hacerlo ahora
        if (!isGameInitialized) {
            startNewGame()
        } else {
            // Si ya hemos inicializado, enviar datos de nuestros barcos otra vez
            sendShipData()
        }
    }

    // Nuevo método para manejar los datos de barcos recibidos
    private fun handleShipData(shipDataJson: String) {
        try {
            val gson = Gson()
            val shipDataList = gson.fromJson(shipDataJson, Array<ShipData>::class.java).toList()

            Log.d(TAG, "Datos de barcos recibidos: ${shipDataList.size} barcos")

            // Limpiar barcos enemigos existentes si los hay
            enemyBoard.clearShips()
            enemyShipCells.clear()

            // Colocar los barcos enemigos en nuestro tablero
            for (shipData in shipDataList) {
                val ship = Ship(shipData.size, shipData.vertical, Color.TRANSPARENT)
                val success = enemyBoard.placeShip(ship, shipData.x, shipData.y)

                if (success) {
                    Log.d(TAG, "Barco enemigo colocado: tamaño ${shipData.size}, en (${shipData.x},${shipData.y}), vertical: ${shipData.vertical}")

                    // Almacenar las coordenadas de cada celda con barco
                    if (shipData.vertical) {
                        for (i in 0 until shipData.size) {
                            if (shipData.y + i < 7) {
                                enemyShipCells.add(Pair(shipData.x, shipData.y + i))
                            }
                        }
                    } else {
                        for (i in 0 until shipData.size) {
                            if (shipData.x + i < 7) {
                                enemyShipCells.add(Pair(shipData.x + i, shipData.y))
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "No se pudo colocar barco enemigo: tamaño ${shipData.size}, en (${shipData.x},${shipData.y}), vertical: ${shipData.vertical}")
                }
            }

            // Verificar que se colocaron correctamente
            Log.d(TAG, "Total de celdas con barcos enemigos: ${enemyShipCells.size}")
            Log.d(TAG, "Total de barcos enemigos según propiedad: ${enemyBoard.ships}")

            // Marcar el juego como inicializado
            isGameInitialized = true

            // Revelar una celda aleatoria para empezar
            if (running) {
                revealEnemyShipCell()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar datos de barcos: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showBinaryConversionChallenge(clickedX: Int, clickedY: Int) {
        if (enemyShipCells.isEmpty()) {
            Toast.makeText(this, "No hay información de barcos enemigos disponible", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "No hay celdas de barcos enemigos disponibles para el desafío")
            return
        }

        // Si no tenemos una coordenada revelada, obtener una al azar
        if (lastRevealedCell == null) {
            revealEnemyShipCell()
            if (lastRevealedCell == null) {
                Toast.makeText(this, "No se pudo revelar una coordenada de barco", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val x = lastRevealedCell!!.first
        val y = lastRevealedCell!!.second

        Log.d(TAG, "Mostrando desafío de conversión para coordenada (${x},${y}) => ${BinaryConversionHelper.coordsToString(x, y)}")

        binaryConversionDialog.showConversionChallenge(
            x, y,
            onCorrectConversion = { shootX, shootY, isCorrectConversion ->
                // Si la conversión fue correcta, disparar en la coordenada revelada
                if (isCorrectConversion) {
                    // Usar exactamente la coordenada revelada
                    val revealedX = lastRevealedCell!!.first
                    val revealedY = lastRevealedCell!!.second

                    // Verificar que la celda no haya sido disparada ya
                    val cell = enemyBoard.getCell(revealedX, revealedY)
                    if (cell.wasShot) {
                        Toast.makeText(this, "Ya has disparado en esa coordenada. Revelando una nueva.", Toast.LENGTH_SHORT).show()
                        lastRevealedCell = null
                        revealEnemyShipCell()
                        return@showConversionChallenge
                    }

                    Log.d(TAG, "Conversión correcta. Disparando en coordenada revelada: (${revealedX},${revealedY}) => ${BinaryConversionHelper.coordsToString(revealedX, revealedY)}")
                    sendShootMessage(revealedX, revealedY)

                    // Reiniciar la coordenada revelada
                    lastRevealedCell = null

                    // Verificar si quedan barcos sin descubrir
                    val remainingShipCells = enemyShipCells.count { (cellX, cellY) ->
                        !enemyBoard.getCell(cellX, cellY).wasShot
                    }

                    // Revelar una nueva coordenada solo si quedan barcos
                    if (remainingShipCells > 0) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (running && isGameInitialized) {
                                revealEnemyShipCell()
                            }
                        }, 1500) // Esperar 1.5 segundos antes de revelar una nueva coordenada
                    }
                } else {
                    // Conversión incorrecta: asegurarse de no acertar a un barco por casualidad
                    val cell = enemyBoard.getCell(shootX, shootY)

                    if (!cell.wasShot) {
                        if (cell.ship != null) {
                            // Si hay un barco, no permitir acertar - buscar otra celda vacía
                            val emptyCell = findSafeEmptyCell()
                            if (emptyCell != null) {
                                val emptyCellX = emptyCell.first
                                val emptyCellY = emptyCell.second
                                sendShootMessage(emptyCellX, emptyCellY)
                                Log.d(TAG, "Conversión incorrecta, redirigiendo disparo a celda vacía: ${BinaryConversionHelper.coordsToString(emptyCellX, emptyCellY)}")
                                Toast.makeText(this, "Disparo incorrecto en ${BinaryConversionHelper.coordsToString(emptyCellX, emptyCellY)}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // No hay barco, disparar normalmente
                            sendShootMessage(shootX, shootY)
                            Log.d(TAG, "Disparo incorrecto enviado para ${BinaryConversionHelper.coordsToString(shootX, shootY)}")
                            Toast.makeText(this, "Disparo incorrecto en ${BinaryConversionHelper.coordsToString(shootX, shootY)}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Ya has disparado en esa coordenada", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            reuseLastCoordinate = true // Siempre usar la coordenada almacenada
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

    // Método mejorado para revelar celdas de barcos enemigos
    private fun revealEnemyShipCell() {
        // Filtrar celdas de barcos que no han sido disparadas
        val availableShipCells = enemyShipCells.filter { (x, y) ->
            !enemyBoard.getCell(x, y).wasShot
        }

        if (availableShipCells.isEmpty()) {
            Log.d(TAG, "No hay celdas de barcos sin disparar disponibles")
            Toast.makeText(this, "¡Has descubierto todos los barcos enemigos!", Toast.LENGTH_LONG).show()
            lastRevealedCell = null

            // Verificar si hay alguna celda sin disparar para confirmar victoria
            val allCellsShot = enemyShipCells.all { (x, y) ->
                enemyBoard.getCell(x, y).wasShot
            }

            if (allCellsShot) {
                // Todos los barcos están hundidos
                Toast.makeText(this, "¡GANASTE! Todos los barcos enemigos han sido hundidos", Toast.LENGTH_LONG).show()
                running = false
            }

            return
        }

        // Seleccionar una celda aleatoria
        val selectedCell = availableShipCells.random()
        lastRevealedCell = selectedCell

        val coordsStr = BinaryConversionHelper.coordsToString(selectedCell.first, selectedCell.second)
        Log.d(TAG, "Nueva celda de barco revelada: (${selectedCell.first},${selectedCell.second}) => $coordsStr")

        // No enviar mensaje de revelación - ahora usamos coordenadas locales
        Toast.makeText(this, "¡Nueva coordenada binaria revelada: $coordsStr en binario!", Toast.LENGTH_SHORT).show()
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
        if (parts.size < 3) {
            Log.e(TAG, "Mensaje SHOOT mal formado: ${parts.joinToString(",")}")
            return
        }

        val x = parts[1].toInt()
        val y = parts[2].toInt()

        if (x !in 0..6 || y !in 0..6) {
            Log.e(TAG, "Coordenadas de disparo fuera de rango: ($x,$y)")
            return
        }

        val cell = playerBoard.getCell(x, y)
        val hit = cell.shoot()

        Log.d(TAG, "Disparo recibido en (${x},${y}) => ${BinaryConversionHelper.coordsToString(x, y)} - Acertó: $hit")

        bluetoothGameManager.sendMessage("RESULT,$x,$y,$hit")

        if (playerBoard.ships == 0) {
            Toast.makeText(this, "¡PERDISTE!", Toast.LENGTH_LONG).show()
            running = false
        }
    }

    private fun handleIncomingResult(parts: List<String>) {
        if (parts.size < 4) {
            Log.e(TAG, "Mensaje RESULT mal formado: ${parts.joinToString(",")}")
            return
        }

        val x = parts[1].toInt()
        val y = parts[2].toInt()
        val hit = parts[3].toBoolean()

        if (x !in 0..6 || y !in 0..6) {
            Log.e(TAG, "Coordenadas de resultado fuera de rango: ($x,$y)")
            return
        }

        val cell = enemyBoard.getCell(x, y)
        cell.wasShot = true

        Log.d(TAG, "Resultado de disparo en (${x},${y}) => ${BinaryConversionHelper.coordsToString(x, y)} - Acertó: $hit")

        if (hit) {
            // Si acertamos, asegurarse de que la celda tiene un barco
            if (cell.ship == null) {
                cell.ship = Ship(1, false) // Ship dummy para mostrar el hit
            }

            // Verificar cuántas celdas con barcos quedan sin disparar
            val remainingShipCells = enemyShipCells.count { (cellX, cellY) ->
                !enemyBoard.getCell(cellX, cellY).wasShot
            }

            Log.d(TAG, "Quedan $remainingShipCells celdas con barcos sin disparar")

            if (remainingShipCells == 0) {
                // Victoria genuina cuando todos los barcos están hundidos
                Toast.makeText(this, "¡GANASTE! Todos los barcos enemigos han sido hundidos", Toast.LENGTH_LONG).show()
                running = false
            } else {
                // Mensaje positivo pero el juego continúa
                Toast.makeText(this, "¡Buen disparo! Has acertado un barco", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Si fallamos, asegurarse de que la celda no tiene barco
            cell.ship = null
            Toast.makeText(this, "Disparo al agua", Toast.LENGTH_SHORT).show()
        }

        cell.invalidate()
    }

    private fun handleIncomingReveal(parts: List<String>) {
        // Ya no usamos este mensaje ya que revelamos localmente
        Log.d(TAG, "Método handleIncomingReveal() deprecado - Usando revelación local")
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