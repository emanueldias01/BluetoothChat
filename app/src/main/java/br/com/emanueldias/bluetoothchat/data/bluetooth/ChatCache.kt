package br.com.emanueldias.bluetoothchat.data.bluetooth

import android.content.Context
import br.com.emanueldias.bluetoothchat.domain.ChatMessage
import org.json.JSONArray
import org.json.JSONObject

class ChatCache(context: Context) {
    private val prefs = context.getSharedPreferences("bluetooth_chat_messages_cache", Context.MODE_PRIVATE)

    fun getMessages(address: String): List<ChatMessage> {
        val jsonString = prefs.getString(address, null) ?: return emptyList()
        val list = mutableListOf<ChatMessage>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ChatMessage(
                        id = obj.optString("id"),
                        text = obj.optString("text"),
                        senderName = obj.optString("senderName"),
                        isFromLocalUser = obj.optBoolean("isFromLocalUser"),
                        timestamp = obj.optLong("timestamp")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveMessage(address: String, message: ChatMessage) {
        val current = getMessages(address).toMutableList()
        current.add(message)
        val jsonArray = JSONArray()
        for (item in current) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("text", item.text)
                put("senderName", item.senderName)
                put("isFromLocalUser", item.isFromLocalUser)
                put("timestamp", item.timestamp)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(address, jsonArray.toString()).apply()
    }

    fun clearMessages(address: String) {
        prefs.edit().remove(address).apply()
    }
}
