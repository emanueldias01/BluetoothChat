package br.com.emanueldias.bluetoothchat.ui.presentation

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.emanueldias.bluetoothchat.data.bluetooth.BluetoothScanner
import br.com.emanueldias.bluetoothchat.ui.presentation.components.AppBarListDevices
import br.com.emanueldias.bluetoothchat.ui.presentation.components.DeviceComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceList(
    modifier: Modifier = Modifier,
    viewModel: DeviceViewModel = viewModel(
        factory = DeviceViewModel.Factory(
            BluetoothScanner(LocalContext.current.applicationContext)
        )
    )
) {
    val context = LocalContext.current
    val bluetoothScanner = remember { BluetoothScanner(context.applicationContext) }
    val uiState by viewModel.uiState.collectAsState()
    val devices = uiState.devices

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.scanDevices()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            if (!bluetoothScanner.isBluetoothEnable()) {
                enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            } else {
                viewModel.scanDevices()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (BluetoothScanner.hasPermissions(context)) {
            if (!bluetoothScanner.isBluetoothEnable()) {
                enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            } else {
                viewModel.scanDevices()
            }
        } else {
            permissionLauncher.launch(BluetoothScanner.getRequiredPermissions())
        }
    }

    LaunchedEffect(uiState.connectionError) {
        uiState.connectionError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearConnectionError()
        }
    }

    Scaffold(
        topBar = {
            AppBarListDevices(onScanClick = { viewModel.scanDevices() })
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (devices.isEmpty()) {
                Text(
                    text = "No devices found",
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(devices) { device ->
                        DeviceComponent(
                            deviceName = device.name,
                            deviceAddress = device.address,
                            isPair = device.isPair,
                            isConnected = device.isConnected,
                            onClickPairOrConnectDevice = {
                                if (device.isPair) {
                                    viewModel.connectDevice(deviceAddress = device.address)
                                } else {
                                    viewModel.pairDevice(deviceAddress = device.address)
                                }
                            },
                            isLoading = uiState.isConnecting
                        )


                    }

                    if (uiState.isScanning) {
                        item {
                            CircularProgressIndicator()
                        }
                    }
                }
            }


        }
    }
}
