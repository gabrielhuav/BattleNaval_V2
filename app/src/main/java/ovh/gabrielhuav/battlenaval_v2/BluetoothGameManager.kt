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
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private var connectThread: ConnectThread? = null
    private var acceptThread: AcceptThread? = null
    private var connectedThread: ConnectedThread? = null
    private var state: State = State.NONE

    enum class State {
        NONE,
        LISTEN,
        CONNECTING,
        CONNECTED
    }

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_STATE_CHANGE -> {
                    // Maneja los cambios de estado
                }
                MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    val readMessage = String(readBuf, 0, msg.arg1)
                    onMessageReceived?.invoke(readMessage)
                }
            }
        }
    }

    var onMessageReceived: ((String) -> Unit)? = null
    var onStateChanged: ((State) -> Unit)? = null

    private fun checkBluetoothPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    @Synchronized
    fun start() {
        if (!checkBluetoothPermission()) {
            return
        }

        cancelConnectThread()
        cancelConnectedThread()

        if (acceptThread == null) {
            acceptThread = AcceptThread()
            acceptThread?.start()
            setState(State.LISTEN) // Cambia el estado a LISTEN aquí
            Toast.makeText(context, "Servidor esperando conexiones", Toast.LENGTH_SHORT).show()

        }
    }


    @Synchronized
    fun connect(device: BluetoothDevice) {
        if (!checkBluetoothPermission()) {
            return
        }

        if (state == State.CONNECTING) {
            cancelConnectThread()
        }

        // Cancelar cualquier descubrimiento en curso
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter?.cancelDiscovery()
        }

        try {
            connectThread = ConnectThread(device)
            connectThread?.start()
            setState(State.CONNECTING)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun connected(socket: BluetoothSocket) {
        cancelConnectThread()
        cancelConnectedThread()
        cancelAcceptThread()

        connectedThread = ConnectedThread(socket)
        connectedThread?.start()

        setState(State.CONNECTED)
    }

    @Synchronized
    open fun stop() {
        cancelConnectThread()
        cancelConnectedThread()
        cancelAcceptThread()
        setState(State.NONE)
    }

    open fun sendMove(x: Int, y: Int) {
        val message = "$x,$y"
        write(message.toByteArray())
    }

    private fun write(out: ByteArray) {
        val r: ConnectedThread
        synchronized(this) {
            if (state != State.CONNECTED) return
            r = connectedThread!!
        }
        r.write(out)
    }

    @Synchronized
    private fun setState(newState: State) {
        state = newState
        onStateChanged?.invoke(newState)
    }

    private inner class AcceptThread : Thread() {
        private var serverSocket: BluetoothServerSocket? = null

        init {
            if (checkBluetoothPermission()) {
                try {
                    serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        override fun run() {
            name = "AcceptThread"
            var socket: BluetoothSocket?

            if (!checkBluetoothPermission()) {
                return
            }

            while (this@BluetoothGameManager.state != BluetoothGameManager.State.CONNECTED) {
                socket = try {
                    serverSocket?.accept()
                } catch (e: IOException) {
                    break
                }

                if (socket != null) {
                    synchronized(this@BluetoothGameManager) {
                        when (this@BluetoothGameManager.state) {
                            BluetoothGameManager.State.LISTEN,
                            BluetoothGameManager.State.CONNECTING -> connected(socket)
                            BluetoothGameManager.State.NONE,
                            BluetoothGameManager.State.CONNECTED -> {
                                try {
                                    socket.close()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
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

    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {
        private var socket: BluetoothSocket? = null

        init {
            if (checkBluetoothPermission()) {
                try {
                    socket = device.createRfcommSocketToServiceRecord(MY_UUID)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        override fun run() {
            name = "ConnectThread"
            if (checkBluetoothPermission()) {
                bluetoothAdapter?.cancelDiscovery()
            } else {
                return
            }

            try {
                socket?.connect()
            } catch (e: IOException) {
                try {
                    socket?.close()
                } catch (e2: IOException) {
                    e2.printStackTrace()
                }
                connectionFailed()
                return
            }

            synchronized(this@BluetoothGameManager) {
                connectThread = null
            }

            if (socket != null) {
                connected(socket!!)
            }
        }

        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private inner class ConnectedThread(private val socket: BluetoothSocket) : Thread() {
        private val inputStream: InputStream?
        private val outputStream: OutputStream?

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            try {
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }

            inputStream = tmpIn
            outputStream = tmpOut
        }

        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int

            while (true) {
                try {
                    bytes = inputStream?.read(buffer) ?: -1
                    if (bytes == -1) break
                    handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget()
                } catch (e: IOException) {
                    connectionLost()
                    break
                }
            }
        }

        fun write(buffer: ByteArray) {
            try {
                outputStream?.write(buffer)
                handler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer).sendToTarget()
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

    private fun connectionFailed() {
        setState(State.LISTEN)
    }

    private fun connectionLost() {
        setState(State.LISTEN)
    }

    fun sendMessage(message: String) {
        write(message.toByteArray())
    }
}