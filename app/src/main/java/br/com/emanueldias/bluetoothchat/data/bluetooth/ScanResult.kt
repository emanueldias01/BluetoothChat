package br.com.emanueldias.bluetoothchat.data.bluetooth

import android.bluetooth.BluetoothDevice

data class ScanResult(
    val isScanning: Boolean,
    val devices: List<BluetoothDevice>
)
