package br.com.emanueldias.bluetoothchat.ui.presentation

import android.bluetooth.BluetoothSocket
import br.com.emanueldias.bluetoothchat.domain.ChatMessage
import br.com.emanueldias.bluetoothchat.domain.Device

data class DeviceListUiState(
    val pairedDevices: List<Device> = emptyList(),
    val scannedDevices: List<Device> = emptyList(),
    val isScanning: Boolean = false,
    val isConnecting: Boolean = false,
    val connectedSocket: BluetoothSocket? = null,
    val connectedDevice: Device? = null,
    val activeChatDevice: Device? = null,
    val messages: List<ChatMessage> = emptyList(),
    val connectionError: String? = null,
) {
    val devices: List<Device>
        get() = (pairedDevices + scannedDevices)
            .distinctBy { it.address }
            .map { device ->
                if (connectedSocket != null && (device.address == connectedDevice?.address || device.address == connectedSocket.remoteDevice?.address)) {
                    device.copy(isConnected = true)
                } else {
                    device
                }
            }

    val isLoading: Boolean
        get() = isScanning || isConnecting
}
