package br.com.emanueldias.bluetoothchat.ui.presentation

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.emanueldias.bluetoothchat.data.bluetooth.BluetoothScanner
import br.com.emanueldias.bluetoothchat.data.bluetooth.isConnected
import br.com.emanueldias.bluetoothchat.domain.Device
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceViewModel(
    private val bluetoothScanner: BluetoothScanner
): ViewModel() {
    private val _uiState = MutableStateFlow(DeviceListUiState())
    val uiState: StateFlow<DeviceListUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    init {
        loadPairedDevices()
    }

    @SuppressLint("MissingPermission")
    fun loadPairedDevices() {
        if (!bluetoothScanner.hasRequiredPermissions() || !bluetoothScanner.isBluetoothEnable()) {
            return
        }
        val paired = bluetoothScanner.getPairedDevices().map { device ->
            val name = try {
                device.name ?: "Unnamed device"
            } catch (_: SecurityException) {
                "Unnamed device"
            }
            Device(
                name = name,
                address = device.address,
                isPair = true,
                isConnected = device.isConnected()
            )
        }
        _uiState.value = _uiState.value.copy(pairedDevices = paired)
    }

    @SuppressLint("MissingPermission")
    fun scanDevices() {
        if (!bluetoothScanner.isBluetoothEnable()) {
            _uiState.value = _uiState.value.copy(isScanning = false)
            return
        }

        loadPairedDevices()

        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            bluetoothScanner.startScan().collect { scanResult ->
                val scanned = scanResult.devices.map { device ->
                    val name = try {
                        device.name ?: "Unnamed device"
                    } catch (_: SecurityException) {
                        "Unnamed device"
                    }
                    Device(
                        name = name,
                        address = device.address,
                        isPair = device.bondState == BluetoothDevice.BOND_BONDED,
                        isConnected = device.isConnected()
                    )
                }
                _uiState.value = _uiState.value.copy(
                    scannedDevices = scanned,
                    isScanning = scanResult.isScanning
                )
            }
        }
    }

    fun stopScan() {
        bluetoothScanner.stopScan()
        scanJob?.cancel()
        _uiState.value = _uiState.value.copy(isScanning = false)
    }

    companion object {
        fun Factory(bluetoothScanner: BluetoothScanner): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DeviceViewModel(bluetoothScanner)
            }
        }
    }
}
