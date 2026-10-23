package br.com.emanueldias.bluetoothchat.ui.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import br.com.emanueldias.bluetoothchat.ui.theme.BluetoothChatTheme
import br.com.emanueldias.bluetoothchat.ui.theme.PurpleGrey80
import br.com.emanueldias.bluetoothchat.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBar(
    deviceName: String,
    deviceAddress: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PurpleGrey80,
            titleContentColor = White,
            navigationIconContentColor = White,
            actionIconContentColor = White
        ),
        title = {
            Column {
                Text(
                    text = deviceName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Connected • $deviceAddress",
                    fontSize = 11.sp,
                    color = White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {

        }
    )
}

@Preview
@Composable
private fun ChatTopAppBarPreview() {
    BluetoothChatTheme {
        ChatTopAppBar(
            deviceName = "Android de Emanuel",
            deviceAddress = "00:11:22:33:44:55",
            onBackClick = {},
        )
    }
}
