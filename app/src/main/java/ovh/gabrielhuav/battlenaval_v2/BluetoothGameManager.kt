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
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothGameManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var connectThread: ConnectThread? = null
    private var acceptThread: AcceptThread? = null
    private var connectedThread: ConnectedThread? = null
    private var currentState: BluetoothState = BluetoothState.NONE
    private var isStopping = false

    enum class BluetoothState {
        NONE,
        LISTENING,
        CONNECTING,
        CONNECTED
    }

    companion object {
        private const val APP_NAME = "BattleNavalGame"
        private val MY_UUID: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
        private const val TAG = "BluetoothGameManager"
    }

    var onMessageReceived: ((String) -> Unit)? = null
    var onStateChanged: ((BluetoothState) -> Unit)? = null

    private val handler = Handler(Looper.getMainLooper())

    private fun checkBluetoothPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun ensureBluetoothPermission(): Boolean {
        if (!checkBluetoothPermission()) {
            showToast("Permisos de Bluetooth no concedidos.")
            return false
        }
        return true
    }

    @Synchronized
    fun start() {
        if (!ensureBluetoothPermission()) return

        isStopping = false
        cancelConnectThread()
        cancelConnectedThread()

        if (acceptThread == null) {
            acceptThread = AcceptThread()
            acceptThread?.start()
            updateState(BluetoothState.LISTENING)
            showToast("Servidor esperando conexiones")
        }
    }

    @Synchronized
    fun connect(device: BluetoothDevice) {
        if (!ensureBluetoothPermission()) return

        cancelConnectThread()
        cancelConnectedThread()
        cancelAcceptThread()

        connectThread = ConnectThread(device)
        connectThread?.start()

        updateState(BluetoothState.CONNECTING)
    }

    @Synchronized
    fun connected(socket: BluetoothSocket) {
        cancelConnectThread()
        cancelConnectedThread()
        cancelAcceptThread()

        connectedThread = ConnectedThread(socket)
        connectedThread?.start()

        updateState(BluetoothState.CONNECTED)
        showToast("Dispositivo conectado")
    }

    @Synchronized
    fun stop() {
        isStopping = true
        cancelConnectThread()
        cancelConnectedThread()
        cancelAcceptThread()
        updateState(BluetoothState.NONE)
    }

    fun sendMessage(message: String) {
        if (currentState == BluetoothState.CONNECTED) {
            connectedThread?.write(message.toByteArray())
        } else {
            Log.w(TAG, "No se puede enviar el mensaje, no hay conexión activa.")
        }
    }


    private fun updateState(newState: BluetoothState) {
        currentState = newState
        onStateChanged?.invoke(newState)
    }

    private inner class AcceptThread : Thread() {
        private val serverSocket: BluetoothServerSocket? = try {
            bluetoothAdapter?.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID)
        } catch (e: IOException) {
            Log.e(TAG, "Error al crear el socket del servidor", e)
            null
        }

        override fun run() {
            while (currentState != BluetoothState.CONNECTED) {
                try {
                    val socket = serverSocket?.accept()
                    socket?.let {
                        synchronized(this@BluetoothGameManager) {
                            when (currentState) {
                                BluetoothState.LISTENING, BluetoothState.CONNECTING -> connected(it)
                                BluetoothState.NONE, BluetoothState.CONNECTED -> it.close()
                            }
                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Error aceptando conexión", e)
                    break
                }
            }
        }

        fun cancel() {
            try {
                serverSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error cerrando el socket del servidor", e)
            }
        }
    }

    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {
        private val socket: BluetoothSocket? = try {
            device.createRfcommSocketToServiceRecord(MY_UUID)
        } catch (e: IOException) {
            Log.e(TAG, "Error al crear el socket del cliente", e)
            null
        }

        override fun run() {
            bluetoothAdapter?.cancelDiscovery()
            try {
                socket?.connect()
                connected(socket!!)
            } catch (e: IOException) {
                Log.e(TAG, "Error conectando al dispositivo: ${e.message}")
                closeSocket()
                connectionFailed()
            }
        }

        private fun closeSocket() {
            try {
                socket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error cerrando el socket del cliente", e)
            }
        }

        fun cancel() {
            closeSocket()
        }
    }

    private inner class ConnectedThread(private val socket: BluetoothSocket) : Thread() {
        private val inputStream: InputStream = socket.inputStream
        private val outputStream: OutputStream = socket.outputStream

        override fun run() {
            val buffer = ByteArray(1024)
            while (!isInterrupted) {
                try {
                    val bytes = inputStream.read(buffer)
                    if (bytes > 0) {
                        val message = String(buffer, 0, bytes)
                        handler.post { onMessageReceived?.invoke(message) }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Conexión perdida en el hilo de lectura", e)
                    connectionLost()
                    break
                }
            }
        }

        fun write(buffer: ByteArray) {
            try {
                outputStream.write(buffer)
            } catch (e: IOException) {
                Log.e(TAG, "Error escribiendo datos", e)
                connectionLost()
            }
        }

        fun cancel() {
            try {
                inputStream.close()
                outputStream.close()
                socket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error cerrando el socket", e)
            }
        }
    }

    private fun connectionLost() {
        if (!isStopping) {
            Log.e(TAG, "Conexión perdida")
            updateState(BluetoothState.LISTENING)
            handler.postDelayed({ start() }, 2000) // Intentar reconectar tras 2 segundos
        }
    }

    private fun connectionFailed() {
        if (!isStopping) {
            Log.e(TAG, "Conexión fallida")
            updateState(BluetoothState.LISTENING)
            handler.postDelayed({ start() }, 3000) // Intentar reconectar tras 3 segundos
        }
    }

    fun getDeviceByAddress(address: String): BluetoothDevice? {
        return if (ensureBluetoothPermission()) {
            bluetoothAdapter?.bondedDevices?.find { it.address == address }
        } else {
            null
        }
    }

    private fun cancelConnectThread() {
        connectThread?.cancel()
        connectThread = null
    }

    private fun cancelConnectedThread() {
        connectedThread?.cancel()
        connectedThread = null
    }

    private fun cancelAcceptThread() {
        acceptThread?.cancel()
        acceptThread = null
    }

    private fun showToast(message: String) {
        handler.post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
