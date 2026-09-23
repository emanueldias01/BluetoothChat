package br.com.emanueldias.bluetoothchat.ui.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.emanueldias.bluetoothchat.ui.theme.BluetoothChatTheme
import br.com.emanueldias.bluetoothchat.ui.theme.Grey
import br.com.emanueldias.bluetoothchat.ui.theme.Purple40

@Composable
fun DeviceComponent(
    modifier: Modifier = Modifier,
    deviceName: String,
    deviceAddress: String,
    isPair: Boolean
) {
    Card(
        modifier.fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(
            modifier.padding(12.dp).fillMaxWidth()
        ) {
            Text(deviceName, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Text(deviceAddress, fontSize = 12.sp)
                if(isPair) {
                    Icon(Icons.Default.Check, contentDescription = null)
                }
            }

        }
    }
}

@Preview
@Composable
private fun DeviceComponentPreview() {
    BluetoothChatTheme() {
        DeviceComponent(
            deviceName = "Android de Emanuel",
            deviceAddress = "123123123123",
            isPair = true
        )
    }
}
