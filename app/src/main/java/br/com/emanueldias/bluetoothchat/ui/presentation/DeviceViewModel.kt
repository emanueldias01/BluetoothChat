package br.com.emanueldias.bluetoothchat.ui.presentation

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.emanueldias.bluetoothchat.data.bluetooth.BluetoothScanner
import br.com.emanueldias.bluetoothchat.data.bluetooth.ConnectionState
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
    private var connectionJob: Job? = null

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

    fun pairDevice(deviceAddress: String) {
        Log.i("DEVICEVIEWMODEL", "pairDevice: $deviceAddress")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConnecting = true, connectionError = null)
            bluetoothScanner.pairDeviceFlow(deviceAddress).collect { bondState ->
                when (bondState) {
                    BluetoothDevice.BOND_BONDED -> {
                        _uiState.value = _uiState.value.copy(isConnecting = false)
                        loadPairedDevices()
                    }
                    BluetoothDevice.BOND_NONE -> {
                        _uiState.value = _uiState.value.copy(
                            connectionError = "Failed to pair with device",
                            isConnecting = false
                        )
                    }
                    BluetoothDevice.BOND_BONDING -> {
                        _uiState.value = _uiState.value.copy(isConnecting = true)
                    }
                }
            }
        }
    }

    fun connectDevice(deviceAddress: String) {
        Log.i("DEVICEVIEWMODEL", "connectDevice: $deviceAddress")
        _uiState.value = _uiState.value.copy(isConnecting = true, connectionError = null)
        connectionJob?.cancel()
        connectionJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConnecting = true, connectionError = null)
            bluetoothScanner.connectToDevice(deviceAddress).collect { state ->
                when (state) {
                    is ConnectionState.Connected -> {
                        _uiState.value = _uiState.value.copy(
                            isConnecting = false,
                            connectedSocket = state.socket
                        )
                        loadPairedDevices()
                    }
                    is ConnectionState.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isConnecting = false,
                            connectionError = state.message
                        )
                    }
                    ConnectionState.Connecting -> {
                        _uiState.value = _uiState.value.copy(isConnecting = true)
                    }
                    ConnectionState.Idle -> {}
                }
            }
        }
    }

    fun clearConnectionError() {
        _uiState.value = _uiState.value.copy(connectionError = null)
    }

    companion object {
        fun Factory(bluetoothScanner: BluetoothScanner): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DeviceViewModel(bluetoothScanner)
            }
        }
    }
}
