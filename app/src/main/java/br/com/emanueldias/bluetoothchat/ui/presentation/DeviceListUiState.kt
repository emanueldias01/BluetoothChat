package br.com.emanueldias.bluetoothchat.ui.presentation

import br.com.emanueldias.bluetoothchat.domain.Device

data class DeviceListUiState(
    val pairedDevices: List<Device> = emptyList(),
    val scannedDevices: List<Device> = emptyList(),
    val isScanning: Boolean = false
) {
    val devices: List<Device>
        get() = (pairedDevices + scannedDevices).distinctBy { it.address }

    val isLoading: Boolean
        get() = isScanning
}
