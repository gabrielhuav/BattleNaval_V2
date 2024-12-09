package ovh.gabrielhuav.battlenaval_v2

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlin.concurrent.thread

class BluetoothGameManager(
    private val context: Context,
    private val playerBoard: Board,
    private val enemyBoard: Board,
    private val bluetoothService: BluetoothService
) {

    private val gson = Gson()
    private val gameStateFileName = "game_state.json"
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Bluetooth SPP UUID
    private var serverSocket: BluetoothServerSocket? = null
    private var clientSocket: BluetoothSocket? = null

    enum class State {
        NONE, LISTEN, CONNECTING, CONNECTED
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var state: State = State.NONE

    data class GameState(
        val playerShips: List<ShipData>,
        val enemyShips: List<ShipData>,
        val playerShots: List<Pair<Int, Int>>,
        val enemyShots: List<Pair<Int, Int>>
    )

    data class ShipData(
        val size: Int,
        val x: Int,
        val y: Int,
        val vertical: Boolean
    )

    // Callback para manejar cambios de estado
    var onStateChanged: ((State) -> Unit)? = null

    // Callback para manejar mensajes recibidos
    var onMessageReceived: ((String) -> Unit)? = null

    // Callback para dibujar barcos después de cargar el estado
    var onShipsRestored: (() -> Unit)? = null

    init {
        bluetoothService.onMessageReceived = { message ->
            onMessageReceived?.invoke(message) // Pasa el mensaje al nivel superior
        }
    }

    fun start() {
        if (bluetoothAdapter == null) {
            Log.e("BluetoothGameManager", "Bluetooth no está disponible en este dispositivo.")
            return
        }

        setState(State.LISTEN)
        thread {
            try {
                serverSocket = if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    bluetoothAdapter.listenUsingRfcommWithServiceRecord("BattleNaval", uuid)
                } else {
                    throw SecurityException("Permission BLUETOOTH_CONNECT not granted")
                }
                Log.d("BluetoothGameManager", "Esperando conexiones entrantes...")
                val socket = serverSocket?.accept()
                socket?.let {
                    clientSocket = it
                    bluetoothService.initializeSocket(it)
                    setState(State.CONNECTED)
                    Log.d("BluetoothGameManager", "Cliente conectado.")
                    bluetoothService.write("SHIPS,${serializeNumericShipPositions(playerBoard.getShipCoordinates())}".toByteArray())
                }
            } catch (e: IOException) {
                Log.e("BluetoothGameManager", "Error al iniciar el servidor Bluetooth", e)
                setState(State.NONE)
            } finally {
                serverSocket?.close()
            }
        }
    }

    fun connect(deviceAddress: String) {
        if (bluetoothAdapter == null) {
            Log.e("BluetoothGameManager", "Bluetooth no está disponible en este dispositivo.")
            return
        }

        val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress)
        setState(State.CONNECTING)
        thread {
            try {
                val socket = device.createRfcommSocketToServiceRecord(uuid)
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    bluetoothAdapter.cancelDiscovery()
                }
                socket.connect()
                clientSocket = socket
                bluetoothService.initializeSocket(socket)
                setState(State.CONNECTED)
                Log.d("BluetoothGameManager", "Conectado al dispositivo: $deviceAddress")
                bluetoothService.write("SHIPS,${serializeNumericShipPositions(playerBoard.getShipCoordinates())}".toByteArray())

            } catch (e: IOException) {
                Log.e("BluetoothGameManager", "Error al conectar al dispositivo: $deviceAddress", e)
                setState(State.NONE)
                try {
                    clientSocket?.close()
                } catch (closeException: IOException) {
                    Log.e("BluetoothGameManager", "Error al cerrar el socket después de fallar la conexión", closeException)
                }
            }
        }
    }
    
    private fun serializeNumericShipPositions(ships: List<Pair<String, String>>): String {
        return ships.joinToString(";") {
            val (row, col) = it
            val x = col.toInt() - 1 // Columna en índice numérico
            val y = row[0] - 'A'   // Fila en índice numérico
            "$x,$y" // Formato "x,y"
        }
    }

    fun stop() {
        try {
            clientSocket?.close()
            serverSocket?.close()
            bluetoothService.close()
        } catch (e: IOException) {
            Log.e("BluetoothGameManager", "Error al detener Bluetooth", e)
        }
        setState(State.NONE)
        Log.d("BluetoothGameManager", "Conexión Bluetooth detenida.")
    }

    fun sendMessage(message: String) {
        try {
            bluetoothService.write(message.toByteArray())
            Log.d("BluetoothGameManager", "Mensaje enviado: $message")
        } catch (e: IOException) {
            Log.e("BluetoothGameManager", "Error al enviar mensaje por Bluetooth", e)
        }
    }

    fun saveGameState() {
        val gameState = GameState(
            playerShips = getShipData(playerBoard),
            enemyShips = getShipData(enemyBoard),
            playerShots = getShots(playerBoard),
            enemyShots = getShots(enemyBoard)
        )

        val json = gson.toJson(gameState)
        val file = File(context.filesDir, gameStateFileName)
        file.writeText(json)
        Log.d("BluetoothGameManager", "Estado del juego guardado: $json")
    }

    fun loadGameState() {
        val file = File(context.filesDir, gameStateFileName)
        if (!file.exists()) {
            Log.w("BluetoothGameManager", "Archivo de estado no encontrado.")
            return
        }

        val json = file.readText()
        val gameState = gson.fromJson(json, GameState::class.java)

        // Evita sobrescribir si ya hay barcos colocados
        if (playerBoard.getShipCoordinates().isEmpty()) {
            playerBoard.clearShips()
            restoreShips(playerBoard, gameState.playerShips)
        }

        if (enemyBoard.getShipCoordinates().isEmpty()) {
            enemyBoard.clearShips()
            restoreShips(enemyBoard, gameState.enemyShips)
        }

        restoreShots(playerBoard, gameState.playerShots)
        restoreShots(enemyBoard, gameState.enemyShots)

        onShipsRestored?.invoke()
    }

    private fun getShots(board: Board): List<Pair<Int, Int>> {
        return board.cells.flatMapIndexed { y, row ->
            row.mapIndexedNotNull { x, cell -> if (cell.wasShot) Pair(x, y) else null }
        }
    }

    private fun getShipData(board: Board): List<ShipData> {
        return board.getShipCoordinates().map { (row, col) ->
            val x = col.toInt() - 1
            val y = row[0] - 'A'
            val ship = board.getCell(x, y).ship
            ShipData(
                size = ship?.type ?: 0,
                x = x,
                y = y,
                vertical = ship?.vertical ?: true
            )
        }
    }

    private fun restoreShips(board: Board, ships: List<ShipData>) {
        ships.forEach { shipData ->
            val ship = Ship(shipData.size, shipData.vertical)
            board.placeShip(ship, shipData.x, shipData.y)
        }
    }

    private fun restoreShots(board: Board, shots: List<Pair<Int, Int>>) {
        shots.forEach { (x, y) ->
            board.getCell(x, y).shoot()
        }
    }
    
    @Synchronized
    private fun setState(newState: State) {
        state = newState
        onStateChanged?.invoke(state)
    }


    fun getState(): State {
        return state
    }
}
