package br.com.emanueldias.bluetoothchat.ui.presentation

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.emanueldias.bluetoothchat.data.bluetooth.BluetoothScanner
import br.com.emanueldias.bluetoothchat.data.bluetooth.ChatCache
import br.com.emanueldias.bluetoothchat.data.bluetooth.ConnectionState
import br.com.emanueldias.bluetoothchat.data.bluetooth.isConnected
import br.com.emanueldias.bluetoothchat.domain.ChatMessage
import br.com.emanueldias.bluetoothchat.domain.Device
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceViewModel(
    private val bluetoothScanner: BluetoothScanner,
    private val chatCache: ChatCache
) : ViewModel() {
    private val _uiState = MutableStateFlow(DeviceListUiState())
    val uiState: StateFlow<DeviceListUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null
    private var connectionJob: Job? = null
    private var serverJob: Job? = null
    private var messageJob: Job? = null

    init {
        loadPairedDevices()
        startServerListening()
    }

    @SuppressLint("MissingPermission")
    fun loadPairedDevices() {
        if (!bluetoothScanner.hasRequiredPermissions() || !bluetoothScanner.isBluetoothEnable()) {
            return
        }
        val currentConnectedAddress = _uiState.value.connectedSocket?.remoteDevice?.address
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
                isConnected = (device.address == currentConnectedAddress) || device.isConnected()
            )
        }
        _uiState.value = _uiState.value.copy(pairedDevices = paired)
    }

    fun startServerListening() {
        if (!bluetoothScanner.hasRequiredPermissions() || !bluetoothScanner.isBluetoothEnable()) return
        if (_uiState.value.connectedSocket != null) return

        serverJob?.cancel()
        serverJob = viewModelScope.launch {
            bluetoothScanner.listenForIncomingConnections().collect { state ->
                if (state is ConnectionState.Connected) {
                    onSocketConnected(state.socket)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun onSocketConnected(socket: BluetoothSocket) {
        val remoteDevice = socket.remoteDevice
        val name = try {
            remoteDevice.name ?: "Unnamed device"
        } catch (_: SecurityException) {
            "Unnamed device"
        }
        val device = Device(
            name = name,
            address = remoteDevice.address,
            isPair = true,
            isConnected = true
        )
        val cachedMessages = chatCache.getMessages(device.address)
        _uiState.value = _uiState.value.copy(
            isConnecting = false,
            connectedSocket = socket,
            connectedDevice = device,
            activeChatDevice = device,
            messages = cachedMessages
        )
        loadPairedDevices()
        startMessageReading(socket)
    }

    @SuppressLint("MissingPermission")
    private fun startMessageReading(socket: BluetoothSocket) {
        messageJob?.cancel()
        messageJob = viewModelScope.launch {
            bluetoothScanner.readIncomingMessages(socket).collect { messageText ->
                val address = _uiState.value.connectedDevice?.address ?: socket.remoteDevice?.address
                val senderName = _uiState.value.connectedDevice?.name ?: "Remote Device"
                val incomingMessage = ChatMessage(
                    text = messageText,
                    senderName = senderName,
                    isFromLocalUser = false
                )
                if (address != null) {
                    chatCache.saveMessage(address, incomingMessage)
                    val updatedMessages = chatCache.getMessages(address)
                    if (_uiState.value.activeChatDevice?.address == address || _uiState.value.connectedDevice?.address == address) {
                        _uiState.value = _uiState.value.copy(
                            messages = updatedMessages
                        )
                    }
                }
            }
            // Stream closed / disconnected
            _uiState.value = _uiState.value.copy(
                connectedSocket = null,
                connectedDevice = null,
                activeChatDevice = null,
                connectionError = "Device disconnected"
            )
            loadPairedDevices()
            startServerListening()
        }
    }

    @SuppressLint("MissingPermission")
    fun scanDevices() {
        if (!bluetoothScanner.isBluetoothEnable()) {
            _uiState.value = _uiState.value.copy(isScanning = false)
            return
        }

        loadPairedDevices()
        startServerListening()

        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            bluetoothScanner.startScan().collect { scanResult ->
                val currentConnectedAddress = _uiState.value.connectedSocket?.remoteDevice?.address
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
                        isConnected = (device.address == currentConnectedAddress) || device.isConnected()
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
        serverJob?.cancel()
        connectionJob?.cancel()
        connectionJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConnecting = true, connectionError = null)
            bluetoothScanner.connectToDevice(deviceAddress).collect { state ->
                when (state) {
                    is ConnectionState.Connected -> {
                        onSocketConnected(state.socket)
                    }
                    is ConnectionState.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isConnecting = false,
                            connectionError = state.message
                        )
                        startServerListening()
                    }
                    ConnectionState.Connecting -> {
                        _uiState.value = _uiState.value.copy(isConnecting = true)
                    }
                    ConnectionState.Idle -> {}
                }
            }
        }
    }

    fun sendMessage(text: String) {
        val device = _uiState.value.activeChatDevice ?: _uiState.value.connectedDevice
        val address = device?.address ?: return
        val messageText = text.trim()
        if (messageText.isEmpty()) return

        val sentMessage = ChatMessage(
            text = messageText,
            senderName = "Me",
            isFromLocalUser = true
        )

        // Save to cache immediately
        chatCache.saveMessage(address, sentMessage)

        // Update UI state with updated cached list
        val updatedMessages = chatCache.getMessages(address)
        _uiState.value = _uiState.value.copy(
            messages = updatedMessages
        )

        // Send over Bluetooth socket if available
        val socket = _uiState.value.connectedSocket
        if (socket != null) {
            viewModelScope.launch {
                val success = bluetoothScanner.sendMessage(socket, messageText)
                if (!success) {
                    _uiState.value = _uiState.value.copy(
                        connectionError = "Failed to send message over Bluetooth"
                    )
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(
                connectionError = "Not connected to device"
            )
        }
    }

    fun openChat(device: Device) {
        val cachedMessages = chatCache.getMessages(device.address)
        _uiState.value = _uiState.value.copy(
            activeChatDevice = device,
            messages = cachedMessages
        )
    }

    fun closeChat() {
        _uiState.value = _uiState.value.copy(activeChatDevice = null)
    }

    fun disconnect() {
        connectionJob?.cancel()
        messageJob?.cancel()
        bluetoothScanner.closeSocket(_uiState.value.connectedSocket)
        _uiState.value = _uiState.value.copy(
            connectedSocket = null,
            connectedDevice = null,
            activeChatDevice = null,
            messages = emptyList()
        )
        loadPairedDevices()
        startServerListening()
    }

    fun clearConnectionError() {
        _uiState.value = _uiState.value.copy(connectionError = null)
    }

    override fun onCleared() {
        serverJob?.cancel()
        scanJob?.cancel()
        connectionJob?.cancel()
        messageJob?.cancel()
        bluetoothScanner.closeSocket(_uiState.value.connectedSocket)
    }

    companion object {
        fun Factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appContext = context.applicationContext
                DeviceViewModel(
                    bluetoothScanner = BluetoothScanner(appContext),
                    chatCache = ChatCache(appContext)
                )
            }
        }

        fun Factory(bluetoothScanner: BluetoothScanner, chatCache: ChatCache): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DeviceViewModel(bluetoothScanner, chatCache)
            }
        }
    }
}
