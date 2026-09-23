package br.com.emanueldias.bluetoothchat.data.bluetooth

import android.bluetooth.BluetoothDevice

data class BluetoothDeviceComplete(
    val device: BluetoothDevice,
    val isConnected: Boolean
) {
}