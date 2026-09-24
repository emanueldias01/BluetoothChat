package br.com.emanueldias.bluetoothchat.ui.presentation

import android.bluetooth.BluetoothSocket
import br.com.emanueldias.bluetoothchat.domain.Device

data class DeviceListUiState(
    val pairedDevices: List<Device> = emptyList(),
    val scannedDevices: List<Device> = emptyList(),
    val isScanning: Boolean = false,
    val isConnecting: Boolean = false,
    val connectedSocket: BluetoothSocket? = null,
    val connectionError: String? = null,
) {
    val devices: List<Device>
        get() = (pairedDevices + scannedDevices).distinctBy { it.address }

    val isLoading: Boolean
        get() = isScanning || isConnecting
}
