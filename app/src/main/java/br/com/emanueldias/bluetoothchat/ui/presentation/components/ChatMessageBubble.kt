package br.com.emanueldias.bluetoothchat.ui.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.emanueldias.bluetoothchat.domain.ChatMessage
import br.com.emanueldias.bluetoothchat.ui.theme.BluetoothChatTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val alignment = if (message.isFromLocalUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (message.isFromLocalUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val textColor = if (message.isFromLocalUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    val bubbleShape = if (message.isFromLocalUser) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }

    val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(backgroundColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (!message.isFromLocalUser) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.8f)
                )
            }

            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontSize = 15.sp
            )

            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.6f),
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatMessageBubblePreview() {
    BluetoothChatTheme {
        Column {
            ChatMessageBubble(
                message = ChatMessage(
                    text = "Hello! How are you?",
                    senderName = "Friend",
                    isFromLocalUser = false
                )
            )
            ChatMessageBubble(
                message = ChatMessage(
                    text = "I'm doing great! Bluetooth chat works!",
                    senderName = "Me",
                    isFromLocalUser = true
                )
            )
        }
    }
}
