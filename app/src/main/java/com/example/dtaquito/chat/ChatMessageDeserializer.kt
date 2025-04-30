package com.example.dtaquito.chat
import Beans.chat.ChatMessage
import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatMessageDeserializer : JsonDeserializer<ChatMessage> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ChatMessage {
        val jsonObject = json.asJsonObject

        val createdAtElement = jsonObject["createdAt"]
        val createdAt = if (createdAtElement.isJsonArray) {
            // Convertir array de [year, month, day, hour, minute, second, nanosecond] a ISO 8601
            val dateArray = createdAtElement.asJsonArray.map { it.asInt }
            LocalDateTime.of(
                dateArray[0], dateArray[1], dateArray[2],
                dateArray[3], dateArray[4], dateArray[5], dateArray[6]
            ).format(DateTimeFormatter.ISO_DATE_TIME)
        } else {
            createdAtElement.asString
        }

        return ChatMessage(
            content = jsonObject["content"].asString,
            userId = jsonObject["userId"].asInt,
            userName = jsonObject["userName"].asString,
            createdAt = createdAt
        )
    }
}
