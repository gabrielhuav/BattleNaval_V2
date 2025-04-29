package ovh.gabrielhuav.battlenaval_v2.battlenavalbinario

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import ovh.gabrielhuav.battlenaval_v2.R

// DeviceListActivity.kt
class DeviceListActivity : AppCompatActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var deviceListView: ListView
    private val deviceList = mutableListOf<String>()
    private val deviceMap = mutableMapOf<String, BluetoothDevice>()

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        deviceListView = findViewById(R.id.deviceListView)
        val backToMenuButton = findViewById<Button>(R.id.btnBackToMenu)
        val scanButton = findViewById<Button>(R.id.btnScanDevices)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        checkAndRequestPermissions()

        // Mostrar dispositivos vinculados
        showPairedDevices()

        scanButton.setOnClickListener {
            if (hasRequiredPermissions()) {
                startDiscovery()
            } else {
                checkAndRequestPermissions()
            }
        }

        backToMenuButton.setOnClickListener {
            finish()
        }

        deviceListView.setOnItemClickListener { _, _, position, _ ->
            val deviceInfo = deviceList[position]
            val address = deviceInfo.split("\n")[1]
            val intent = Intent()
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address)
            setResult(RESULT_OK, intent)
            finish()
        }

        // Registrar receptores de broadcast
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        registerReceiver(receiver, filter)
    }

    private fun hasRequiredPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = REQUIRED_PERMISSIONS.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, PERMISSION_REQUEST_CODE)
        }
    }

    private fun showPairedDevices() {
        if (!hasRequiredPermissions()) return

        val pairedDevices = bluetoothAdapter.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name ?: "Unknown Device"
            val deviceAddress = device.address
            val deviceInfo = "$deviceName\n$deviceAddress"
            if (!deviceList.contains(deviceInfo)) {
                deviceList.add(deviceInfo)
                deviceMap[deviceInfo] = device
            }
        }
        updateDeviceList()
    }

    private fun startDiscovery() {
        if (!hasRequiredPermissions()) return

        deviceList.clear()
        showPairedDevices()

        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }

        bluetoothAdapter.startDiscovery()
        Toast.makeText(this, "Buscando dispositivos...", Toast.LENGTH_SHORT).show()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        if (!hasRequiredPermissions()) return
                        val deviceName = it.name ?: "Unknown Device"
                        val deviceAddress = it.address
                        val deviceInfo = "$deviceName\n$deviceAddress"
                        if (!deviceList.contains(deviceInfo)) {
                            deviceList.add(deviceInfo)
                            deviceMap[deviceInfo] = it
                            updateDeviceList()
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Toast.makeText(context, "Búsqueda finalizada", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateDeviceList() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
        deviceListView.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        if (hasRequiredPermissions() && bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        unregisterReceiver(receiver)
    }

    companion object {
        const val EXTRA_DEVICE_ADDRESS = "device_address"
        private const val REQUEST_ENABLE_BT = 1
        private const val PERMISSION_REQUEST_CODE = 2
    }
}