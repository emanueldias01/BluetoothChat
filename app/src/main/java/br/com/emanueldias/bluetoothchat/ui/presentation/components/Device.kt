package br.com.emanueldias.bluetoothchat.ui.presentation.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import br.com.emanueldias.bluetoothchat.ui.theme.BluetoothChatTheme

@Composable
fun DeviceComponent(
    modifier: Modifier = Modifier,
    deviceName: String,
    deviceAddress: String,
    isPair: Boolean,
    isConnected: Boolean,
    onClickPairOrConnectDevice: () -> Unit,
    onOpenChat: () -> Unit = {},
    isLoading: Boolean
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable {
                if (isConnected) {
                    onOpenChat()
                } else {
                    isExpanded = true
                }
            }
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(deviceName, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                if (isConnected) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Connected",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Connected",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(deviceAddress, fontSize = 12.sp)
                if (isPair) {
                    Text("Paired", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (isExpanded) {
            if (!isConnected) {
                Dialog(onDismissRequest = { isExpanded = false }) {
                    ModalConnectDevice(
                        deviceName = deviceName,
                        deviceAddress = deviceAddress,
                        onClickCancel = { isExpanded = false },
                        isPair = isPair,
                        onClickPairOrConnectDevice = onClickPairOrConnectDevice,
                        isLoading = isLoading
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun DeviceComponentPreview() {
    BluetoothChatTheme {
        DeviceComponent(
            deviceName = "Android de Emanuel",
            deviceAddress = "123123123123",
            isPair = true,
            isConnected = true,
            onClickPairOrConnectDevice = {},
            onOpenChat = {},
            isLoading = false
        )
    }
}
