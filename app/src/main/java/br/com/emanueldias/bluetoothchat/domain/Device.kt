package br.com.emanueldias.bluetoothchat.domain

data class Device(
    val name: String,
    val address: String,
    val isPair: Boolean,
)
