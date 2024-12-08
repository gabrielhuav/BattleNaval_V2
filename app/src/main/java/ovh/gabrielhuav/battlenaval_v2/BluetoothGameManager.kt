package ovh.gabrielhuav.battlenaval_v2

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothGameManager(private val context: Context) {

    companion object {
        private const val APP_NAME = "BattleNavalGame"
        private val MY_UUID: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")

        const val MESSAGE_STATE_CHANGE = 1
        const val MESSAGE_READ = 2
        const val MESSAGE_WRITE = 3
        const val MESSAGE_DEVICE_NAME = 4
        const val MESSAGE_TOAST = 5
        const val MESSAGE_GAME_SHOT = 6
        const val MESSAGE_GAME_RESULT = 7
        const val MESSAGE_SPECIAL_ROUND = 8
    }

    enum class State {
        NONE,
        LISTEN,
        CONNECTING,
        CONNECTED,
        GAME_SETUP,
        SPECIAL_ROUND,
        REVEAL_WAIT,
        PLAYER_1_TURN,
        PLAYER_2_TURN,
        GAME_OVER
    }

    enum class ShotResult {
        MISS, HIT, SUNK
    }

    data class Ship(
        val name: String,
        val color: Int,
        val coordinates: MutableList<String>
    )

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var connectThread: ConnectThread? = null
    private var acceptThread: AcceptThread? = null
    private var connectedThread: ConnectedThread? = null

    private var currentState: BluetoothGameManager.State = BluetoothGameManager.State.NONE
    private var playerShips: MutableList<Ship> = mutableListOf()
    private var opponentShips: MutableList<Ship> = mutableListOf()
    private var isPlayer1: Boolean = false
    private var playerScore: Int = 0
    private var opponentScore: Int = 0

    private var revealedCoordinate: String? = null
    private var revealedPlayerCoordinate: String? = null

    var onMessageReceived: ((String) -> Unit)? = null
    var onStateChanged: ((BluetoothGameManager.State) -> Unit)? = null
    var onGameShotReceived: ((String, ShotResult) -> Unit)? = null
    var onUpdateColors: ((String, Int) -> Unit)? = null

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_STATE_CHANGE -> onStateChanged?.invoke(currentState)
                MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    val readMessage = String(readBuf, 0, msg.arg1)
                    processGameMessage(readMessage)
                }
            }
        }
    }

    private fun processGameMessage(message: String) {
        val parts = message.split("|")
        when (parts[0]) {
            "SETUP" -> handleGameSetup(parts.getOrNull(1) ?: "")
            "SHOT" -> handleGameShot(parts.getOrNull(1) ?: "")
            "RESULT" -> handleShotResult(parts.getOrNull(1) ?: "")
            "SPECIAL" -> handleSpecialRound(parts.getOrNull(1) ?: "")
        }
    }

    private fun handleGameSetup(shipConfig: String) {
        opponentShips = parseShipConfiguration(shipConfig)
        currentState = if (isPlayer1) {
            BluetoothGameManager.State.PLAYER_1_TURN
        } else {
            BluetoothGameManager.State.PLAYER_2_TURN
        }
        onStateChanged?.invoke(currentState)
    }

    private fun handleGameShot(coordinate: String) {
        val result = checkShotOnOpponentBoard(coordinate)
        onUpdateColors?.invoke(coordinate, getColorForResult(result, true))
        sendMessage("RESULT|${result.name}")
    }

    fun getColorForResult(result: ShotResult?, isEnemyBoard: Boolean): Int {
        return when (result) {
            ShotResult.HIT -> if (isEnemyBoard) 0xFFFF0000.toInt() else 0xFF0000FF.toInt() // Rojo para impacto en enemigo, azul para jugador
            ShotResult.SUNK -> 0xFFFFA500.toInt() // Naranja para hundido
            ShotResult.MISS -> 0xFF808080.toInt() // Gris para fallo
            null -> if (isEnemyBoard) 0xFFFF0000.toInt() else 0xFF0000FF.toInt() // Valor predeterminado según el tablero
        }
    }

    fun makeShot(coordinate: String) {
        if ((isPlayer1 && currentState == BluetoothGameManager.State.PLAYER_1_TURN) ||
            (!isPlayer1 && currentState == BluetoothGameManager.State.PLAYER_2_TURN)
        ) {
            sendMessage("SHOT|$coordinate")
            onUpdateColors?.invoke(coordinate, getColorForResult(null, true)) // Actualiza el tablero enemigo con el disparo
        } else {
            Toast.makeText(context, "No es tu turno!", Toast.LENGTH_SHORT).show()
        }
    }

    fun setPlayerShips(ships: List<Ship>, isServer: Boolean) {
        playerShips = ships.toMutableList()
        isPlayer1 = isServer
        val shipConfig = ships.joinToString(";") {
            "${getColorName(it.color)}:${it.coordinates.joinToString(",")}"
        }
        sendMessage("SETUP|$shipConfig")
        currentState = BluetoothGameManager.State.GAME_SETUP
        onStateChanged?.invoke(currentState)
    }
    
    private fun getColorName(color: Int): String {
        return when (color) {
            0xFF00FF00.toInt() -> "green"
            0xFFFF0000.toInt() -> "red"
            0xFF808080.toInt() -> "grey"
            else -> "black"
        }
    }

    private fun handleShotResult(resultStr: String) {
        val result = ShotResult.valueOf(resultStr)
        onGameShotReceived?.invoke(resultStr, result)
        currentState = if (currentState == BluetoothGameManager.State.PLAYER_1_TURN)
            BluetoothGameManager.State.PLAYER_2_TURN
        else
            BluetoothGameManager.State.PLAYER_1_TURN
        onStateChanged?.invoke(currentState)
    }

    private fun handleSpecialRound(revealedCoord: String) {
        revealedCoordinate = revealedCoord
        onUpdateColors?.invoke(revealedCoord, 0xFFFF0000.toInt()) // Rojo para coordenada enemiga
        onStateChanged?.invoke(BluetoothGameManager.State.SPECIAL_ROUND)
    }

    private fun checkShotOnOpponentBoard(coordinate: String): ShotResult {
        opponentShips.forEach { ship ->
            if (coordinate in ship.coordinates) {
                ship.coordinates.remove(coordinate)
                return if (ship.coordinates.isEmpty()) ShotResult.SUNK else ShotResult.HIT
            }
        }
        return ShotResult.MISS
    }

    private fun parseShipConfiguration(config: String): MutableList<Ship> {
        return config.split(";").mapNotNull { shipConfig ->
            val parts = shipConfig.split(":")
            if (parts.size == 2) {
                val colorName = parts[0]
                val coords = parts[1].split(",").toMutableList()
                Ship(
                    name = colorName,
                    color = getColorFromName(colorName),
                    coordinates = coords
                )
            } else null
        }.toMutableList()
    }

    private fun getColorFromName(colorName: String): Int {
        return when (colorName.lowercase()) {
            "green" -> 0xFF00FF00.toInt()
            "red" -> 0xFFFF0000.toInt()
            "grey" -> 0xFF808080.toInt()
            else -> 0xFF000000.toInt()
        }
    }

    fun initiateSpecialRound() {
        revealedPlayerCoordinate = selectRandomShipCoordinate(playerShips)
        revealedCoordinate = selectRandomShipCoordinate(opponentShips)

        sendMessage("SPECIAL|$revealedCoordinate")

        currentState = BluetoothGameManager.State.SPECIAL_ROUND
        onStateChanged?.invoke(currentState)

        startSpecialRoundTimer()
    }

    private fun selectRandomShipCoordinate(ships: List<Ship>): String? {
        return ships.flatMap { it.coordinates }.randomOrNull()
    }

    private fun startSpecialRoundTimer() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (currentState == BluetoothGameManager.State.SPECIAL_ROUND) {
                currentState = BluetoothGameManager.State.REVEAL_WAIT
                onStateChanged?.invoke(currentState)
            }
        }, 30000) // 30 segundos
    }

    fun validateSpecialRoundAnswer(playerAnswer: String): Boolean {
        return if (currentState == BluetoothGameManager.State.SPECIAL_ROUND) {
            val isCorrect = playerAnswer == revealedCoordinate
            currentState = if (isCorrect) {
                if (isPlayer1) BluetoothGameManager.State.PLAYER_1_TURN else BluetoothGameManager.State.PLAYER_2_TURN
            } else {
                if (isPlayer1) BluetoothGameManager.State.PLAYER_2_TURN else BluetoothGameManager.State.PLAYER_1_TURN
            }
            onStateChanged?.invoke(currentState)
            isCorrect
        } else {
            false
        }
    }

    // Conexión Bluetooth: mismos métodos, ajustados para cambios en `State`
    @Synchronized
    fun start() {
        cancelConnectThread()
        cancelConnectedThread()

        if (acceptThread == null) {
            acceptThread = AcceptThread()
            acceptThread?.start()
            currentState = BluetoothGameManager.State.LISTEN
            onStateChanged?.invoke(currentState)
        }
    }

    @Synchronized
    fun connect(device: BluetoothDevice) {
        cancelConnectThread()
        connectThread = ConnectThread(device)
        connectThread?.start()
        currentState = BluetoothGameManager.State.CONNECTING
        onStateChanged?.invoke(currentState)
    }

    @Synchronized
    fun connected(socket: BluetoothSocket) {
        cancelConnectThread()
        cancelAcceptThread()
        cancelConnectedThread()
        connectedThread = ConnectedThread(socket)
        connectedThread?.start()
        currentState = BluetoothGameManager.State.CONNECTED
        onStateChanged?.invoke(currentState)
    }

    @Synchronized
    fun stop() {
        cancelConnectThread()
        cancelAcceptThread()
        cancelConnectedThread()
        currentState = BluetoothGameManager.State.NONE
        onStateChanged?.invoke(currentState)
    }
    private fun sendMessage(message: String) {
        connectedThread?.write(message.toByteArray())
    }

    private fun cancelConnectThread() {
        connectThread?.cancel()
        connectThread = null
    }

    private fun cancelAcceptThread() {
        acceptThread?.cancel()
        acceptThread = null
    }

    private fun cancelConnectedThread() {
        connectedThread?.cancel()
        connectedThread = null
    }

    private fun checkBluetoothPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    }

    private fun setState(newState: State) {
        currentState = newState
        onStateChanged?.invoke(newState)
    }

    private inner class ConnectedThread(private val socket: BluetoothSocket) : Thread() {
        private val inputStream: InputStream? = socket.inputStream
        private val outputStream: OutputStream? = socket.outputStream

        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int

            while (currentState == BluetoothGameManager.State.CONNECTED) {
                try {
                    bytes = inputStream?.read(buffer) ?: -1
                    if (bytes > 0) {
                        val readMessage = String(buffer, 0, bytes)
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer.clone()).sendToTarget()
                    }
                } catch (e: IOException) {
                    setState(BluetoothGameManager.State.LISTEN)
                    break
                }
            }
        }

        fun write(buffer: ByteArray) {
            try {
                outputStream?.write(buffer)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun cancel() {
            try {
                socket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {
        private val socket: BluetoothSocket? = device.createRfcommSocketToServiceRecord(MY_UUID)

        override fun run() {
            if (!checkBluetoothPermission()) {
                return
            }

            bluetoothAdapter?.cancelDiscovery()

            try {
                socket?.connect()
            } catch (e: IOException) {
                try {
                    socket?.close()
                } catch (e2: IOException) {
                    e2.printStackTrace()
                }
                setState(BluetoothGameManager.State.LISTEN)
                return
            }

            synchronized(this@BluetoothGameManager) {
                connectThread = null
            }

            socket?.let { connected(it) }
        }

        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private inner class AcceptThread : Thread() {
        private val serverSocket: BluetoothServerSocket? = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID)

        override fun run() {
            var socket: BluetoothSocket?

            while (currentState == BluetoothGameManager.State.LISTEN || currentState == BluetoothGameManager.State.CONNECTING) {
                socket = try {
                    serverSocket?.accept()
                } catch (e: IOException) {
                    break
                }

                socket?.let {
                    synchronized(this@BluetoothGameManager) {
                        when (currentState) {
                            BluetoothGameManager.State.LISTEN,
                            BluetoothGameManager.State.CONNECTING -> connected(it)
                            else -> try {
                                it.close()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }

        fun cancel() {
            try {
                serverSocket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
