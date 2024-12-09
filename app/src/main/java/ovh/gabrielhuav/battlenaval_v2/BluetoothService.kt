package ovh.gabrielhuav.battlenaval_v2

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.concurrent.thread

class BluetoothService(private val context: Context) {
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    var onMessageReceived: ((String) -> Unit)? = null // Callback para recibir mensajes

    /**
     * Conecta al dispositivo Bluetooth proporcionado.
     */
    fun connectToDevice(device: BluetoothDevice): Boolean {
        try {
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // UUID estándar para Bluetooth SPP
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()
            initializeStreams()
            showToast("Conexión establecida con el dispositivo: ${device.name}")
            Log.d("BluetoothService", "Conexión establecida con el dispositivo: ${device.name}")
            return true
        } catch (e: IOException) {
            Log.e("BluetoothService", "Error al conectar al dispositivo ${device.name}", e)
            close()
            return false
        }
    }

    /**
     * Inicializa el socket desde el BluetoothGameManager.
     */
    fun initializeSocket(socket: BluetoothSocket) {
        thread {
            try {
                bluetoothSocket = socket
                initializeStreams()
                val input = socket.inputStream
                val buffer = ByteArray(1024)
                while (true) {
                    val bytes = input.read(buffer)
                    val message = String(buffer, 0, bytes)
                    runOnMainThread {
                        showToast("Mensaje leído: $message")
                    }
                    onMessageReceived?.invoke(message) // Propaga el mensaje
                }
            } catch (e: IOException) {
                runOnMainThread {
                    showToast("Error al leer del socket: ${e.message}")
                }
            }
        }
    }



    /**
     * Escribe datos a través del socket Bluetooth.
     */
    fun write(data: ByteArray) {
        try {
            outputStream?.write(data)
            Log.d("BluetoothService", "Datos enviados: ${String(data)}")
        } catch (e: IOException) {
            Log.e("BluetoothService", "Error al enviar datos", e)
        }
    }

    /**
     * Lee datos desde el socket Bluetooth.
     */
    fun read(): ByteArray? {
        return try {
            val buffer = ByteArray(1024)
            val bytesRead = inputStream?.read(buffer) ?: -1
            if (bytesRead > 0) buffer.copyOf(bytesRead) else null
        } catch (e: IOException) {
            Log.e("BluetoothService", "Error al leer datos", e)
            null
        }
    }

    /**
     * Cierra el socket Bluetooth y sus flujos.
     */
    fun close() {
        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
            Log.d("BluetoothService", "Conexión Bluetooth cerrada.")
        } catch (e: IOException) {
            Log.e("BluetoothService", "Error al cerrar la conexión", e)
        }
    }

    /**
     * Inicializa los flujos de entrada y salida del socket.
     */
    private fun initializeStreams() {
        try {
            inputStream = bluetoothSocket?.inputStream
            outputStream = bluetoothSocket?.outputStream
            Log.d("BluetoothService", "Flujos de entrada y salida inicializados.")
        } catch (e: IOException) {
            Log.e("BluetoothService", "Error al inicializar los flujos del socket", e)
            close()
        }
    }

    /**
     * Muestra un mensaje Toast en el hilo principal.
     */
    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Ejecuta un bloque de código en el hilo principal.
     */
    private fun runOnMainThread(action: () -> Unit) {
        Handler(Looper.getMainLooper()).post(action)
    }
}
