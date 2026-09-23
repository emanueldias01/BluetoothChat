package br.com.emanueldias.bluetoothchat.ui.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.emanueldias.bluetoothchat.ui.theme.BluetoothChatTheme
import br.com.emanueldias.bluetoothchat.ui.theme.PurpleGrey80
import br.com.emanueldias.bluetoothchat.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarListDevices(
    isScanning: Boolean = false,
    onScanClick: () -> Unit = {},
    onMakeDiscoverableClick: () -> Unit = {}
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PurpleGrey80,
            titleContentColor = White,
            navigationIconContentColor = White,
            actionIconContentColor = White
        ),
        title = {
            Text("Device List")
        },
        actions = {
            IconButton(
                onClick = onScanClick,
                enabled = !isScanning
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = White
                    )
                }
            }
        }
    )
}

@Preview
@Composable
private fun AppBarListDevicesPreview() {
    BluetoothChatTheme {
        AppBarListDevices()
    }
}
