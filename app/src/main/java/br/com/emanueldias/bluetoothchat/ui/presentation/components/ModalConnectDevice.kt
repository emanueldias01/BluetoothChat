package br.com.emanueldias.bluetoothchat.ui.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.emanueldias.bluetoothchat.ui.theme.BluetoothChatTheme

@Composable
fun ModalConnectDevice(
    modifier: Modifier = Modifier,
    deviceName: String,
    deviceAddress: String,
    onClickCancel: () -> Unit,
    onClickConnect: () -> Unit
) {
    Card() {
        Column(
            modifier = modifier.padding(36.dp)
        ) {
            Text("Do you want to connect to the device?", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row {
                Text("Name: ")
                Text(deviceName, fontWeight = FontWeight.Bold)
            }
            Row {
                Text("Address: ")
                Text(deviceAddress, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onClickConnect
                ) {
                    Text("Connect")
                }

                OutlinedButton(
                    onClick = onClickCancel
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Preview
@Composable
private fun ModalConnectDevicePreview() {
    BluetoothChatTheme() {
        ModalConnectDevice(deviceName = "Android de Emanuel", deviceAddress = "12312323", onClickConnect = {}, onClickCancel = {})
    }
}