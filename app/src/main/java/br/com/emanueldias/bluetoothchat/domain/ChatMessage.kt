package br.com.emanueldias.bluetoothchat.domain

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val senderName: String,
    val isFromLocalUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
