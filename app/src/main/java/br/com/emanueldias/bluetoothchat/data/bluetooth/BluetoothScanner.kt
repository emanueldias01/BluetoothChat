package br.com.emanueldias.bluetoothchat.data.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

sealed interface ConnectionState {
    object Idle : ConnectionState
    object Connecting : ConnectionState
    data class Connected(val socket: BluetoothSocket) : ConnectionState
    data class Error(val message: String) : ConnectionState
}

class BluetoothScanner(
    private val context: Context
) {
    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    fun hasRequiredPermissions(): Boolean {
        return hasPermissions(context)
    }

    fun isBluetoothEnable(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        if (!hasRequiredPermissions()) return emptyList()
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }

    @SuppressLint("MissingPermission")
    fun startScan(): Flow<ScanResult> = callbackFlow {
        if (!hasRequiredPermissions() || !isBluetoothEnable()) {
            trySend(ScanResult(isScanning = false, devices = emptyList()))
            close()
            return@callbackFlow
        }

        val discoveredDevices = mutableSetOf<BluetoothDevice>()

        trySend(ScanResult(isScanning = true, devices = emptyList()))

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device = IntentCompat.getParcelableExtra(
                            intent,
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java,
                        )
                        if (device != null) {
                            if (device.type == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
                                discoveredDevices.add(device)
                                trySend(ScanResult(isScanning = true, devices = discoveredDevices.toList()))
                            }
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        trySend(ScanResult(isScanning = false, devices = discoveredDevices.toList()))
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED,
        )

        try {
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter.cancelDiscovery()
            }
            val started = bluetoothAdapter?.startDiscovery() == true
            if (!started) {
                trySend(ScanResult(isScanning = false, devices = discoveredDevices.toList()))
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            trySend(ScanResult(isScanning = false, devices = discoveredDevices.toList()))
        }

        awaitClose {
            try {
                if (bluetoothAdapter?.isDiscovering == true) {
                    bluetoothAdapter.cancelDiscovery()
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
            try {
                context.unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (hasRequiredPermissions()) {
            try {
                if (bluetoothAdapter?.isDiscovering == true) {
                    bluetoothAdapter.cancelDiscovery()
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun pairDevice(deviceAddress: String): Boolean {
        if (!hasRequiredPermissions() || !isBluetoothEnable()) return false
        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress) ?: return false
        return device.createBond()
    }

    @SuppressLint("MissingPermission")
    fun pairDevice(device: BluetoothDevice): Boolean {
        if (!hasRequiredPermissions() || !isBluetoothEnable()) return false
        return device.createBond()
    }

    @SuppressLint("MissingPermission")
    fun pairDeviceFlow(deviceAddress: String): Flow<Int> = callbackFlow {
        if (!hasRequiredPermissions() || !isBluetoothEnable()) {
            close()
            return@callbackFlow
        }

        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        if (device == null) {
            close()
            return@callbackFlow
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                    val targetDevice = IntentCompat.getParcelableExtra(
                        intent,
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java,
                    )
                    if (targetDevice?.address == device.address) {
                        val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                        trySend(bondState)
                        if (bondState == BluetoothDevice.BOND_BONDED || bondState == BluetoothDevice.BOND_NONE) {
                            close()
                        }
                    }
                }
            }
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED,
        )

        val initiated = device.createBond()
        if (!initiated) {
            trySend(BluetoothDevice.ERROR)
            close()
        }

        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(deviceAddress: String): Flow<ConnectionState> = callbackFlow {
        if (!hasRequiredPermissions() || !isBluetoothEnable()) {
            trySend(ConnectionState.Error("Bluetooth disabled or missing permissions"))
            close()
            return@callbackFlow
        }

        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        if (device == null) {
            trySend(ConnectionState.Error("Device not found"))
            close()
            return@callbackFlow
        }

        trySend(ConnectionState.Connecting)

        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }

        val socket = try {
            device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
        } catch (e: Exception) {
            trySend(ConnectionState.Error(e.message ?: "Failed to create socket"))
            close()
            return@callbackFlow
        }

        try {
            withContext(Dispatchers.IO) {
                socket.connect()
            }
            trySend(ConnectionState.Connected(socket))
        } catch (e: Exception) {
            try {
                socket.close()
            } catch (_: Exception) {}
            trySend(ConnectionState.Error(e.message ?: "Failed to connect"))
            close()
        }

        awaitClose {
            // Keep socket open unless explicitly disconnected or flow cancelled
        }
    }

    companion object {
        fun getRequiredPermissions(): Array<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                )
            } else {
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }
        }

        fun hasPermissions(context: Context): Boolean {
            return getRequiredPermissions().all { permission ->
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun BluetoothDevice.isConnected(): Boolean {
    return try {
        val method = javaClass.getMethod("isConnected")
        method.invoke(this) as? Boolean == true
    } catch (_: Exception) {
        false
    }
}
