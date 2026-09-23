package br.com.emanueldias.bluetoothchat.ui.presentation.components

import android.graphics.Color
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import br.com.emanueldias.bluetoothchat.ui.theme.BluetoothChatTheme
import br.com.emanueldias.bluetoothchat.ui.theme.PurpleGrey40
import br.com.emanueldias.bluetoothchat.ui.theme.PurpleGrey80
import br.com.emanueldias.bluetoothchat.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarListDevices() {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PurpleGrey80,
            titleContentColor = White,
            navigationIconContentColor = White,
            actionIconContentColor = White
        ),
        title = {
            Text("Devices List")
        }
        
    )
}

@Preview
@Composable
private fun AppBarListDevicesPreview() {
    BluetoothChatTheme() {
        AppBarListDevices()
    }
}