package br.com.emanueldias.bluetoothchat.ui.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import br.com.emanueldias.bluetoothchat.ui.presentation.components.AppBarListDevices
import br.com.emanueldias.bluetoothchat.ui.presentation.components.DeviceComponent
import br.com.emanueldias.bluetoothchat.ui.theme.BluetoothChatTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceList(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            AppBarListDevices()
        }
    ) { innerPadding ->
        Column(modifier = modifier.padding(innerPadding)) {
            DeviceComponent(
                deviceName = "Android de Emanuel",
                deviceAddress = "123123123123"
            )
            DeviceComponent(
                deviceName = "Android de Emanuel",
                deviceAddress = "123123123123"
            )
            DeviceComponent(
                deviceName = "Android de Emanuel",
                deviceAddress = "123123123123"
            )

        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun DeviceListPreview() {
    BluetoothChatTheme() {
        DeviceList()
    }
}