package br.com.emanueldias.bluetoothchat.ui.presentation

import android.annotation.SuppressLint
import android.bluetooth.BondStatus
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.emanueldias.bluetoothchat.data.bluetooth.BluetoothScanner
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
        val paired = bluetoothScanner.getPairedDevices().map { d ->
            val name = try {
                d.name ?: "No name device"
            } catch (e: SecurityException) {
                "Dispositivo sem nome"
            }
            Device(name = name, address = d.address, isPair = true)
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
                val scanned = scanResult.devices.map { d ->
                    val name = try {
                        d.name ?: "No name device"
                    } catch (e: SecurityException) {
                        "Dispositivo sem nome"
                    }
                    Device(name = name, address = d.address, d.bondState == 1)
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
