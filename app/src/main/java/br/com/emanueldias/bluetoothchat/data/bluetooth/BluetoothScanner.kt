package br.com.emanueldias.bluetoothchat.data.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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
    fun getPairedDevices(): List<BluetoothDeviceComplete> {
        if (!hasRequiredPermissions()) return emptyList()
        return bluetoothAdapter?.bondedDevices?.toList()
            ?.map { BluetoothDeviceComplete(device = it, isConnected = isDeviceConnected(it)) }
            ?: emptyList()
    }

    @SuppressLint("MissingPermission")
    fun startScan(): Flow<ScanResult> = callbackFlow {
        if (!hasRequiredPermissions() || !isBluetoothEnable()) {
            trySend(ScanResult(isScanning = false, devices = emptyList()))
            close()
            return@callbackFlow
        }

        val discoveredDevices = mutableSetOf<BluetoothDeviceComplete>()

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
                        device?.let { device ->
                            val deviceComplete = BluetoothDeviceComplete(device = device, isConnected = isDeviceConnected(device))
                            discoveredDevices.add(deviceComplete)
                            trySend(ScanResult(isScanning = true, devices = discoveredDevices.toList()))
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
    fun isDeviceConnected(device: BluetoothDevice): Boolean {
        if (!hasRequiredPermissions()) return false

        // 1. Reflection no metodo isConnected da classe BluetoothDevice (suporta Smartwatches, fones A2DP, etc.)
        try {
            val isConnectedMethod = device.javaClass.getMethod("isConnected")
            val isConnected = isConnectedMethod.invoke(device) as? Boolean
            if (isConnected == true) {
                return true
            }
        } catch (_: Exception) {
        }

        // 2. Fallback via BluetoothManager (para perfis GATT)
        if (bluetoothManager != null) {
            val profiles = intArrayOf(
                BluetoothProfile.GATT,
                BluetoothProfile.GATT_SERVER
            )
            return profiles.any { profile ->
                try {
                    bluetoothManager.getConnectionState(device, profile) == BluetoothProfile.STATE_CONNECTED
                } catch (_: Exception) {
                    false
                }
            }
        }

        return false
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
