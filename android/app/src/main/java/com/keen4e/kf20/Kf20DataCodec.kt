package com.keen4e.kf20

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

internal data class ConversationMigration(
    val conversations: List<ChatConversation>,
    val activeConversationId: String,
    val needsWrite: Boolean,
)

/** Pure JSON boundary used by encrypted storage and migration tests. */
internal object Kf20DataCodec {
    fun decodeMessages(json: String?): List<ChatMessage> = runCatching {
        decodeMessages(JSONArray(json ?: "[]"))
    }.getOrDefault(emptyList())

    fun encodeMessages(messages: List<ChatMessage>): JSONArray = JSONArray().apply {
        messages.takeLast(500).forEach { message ->
            put(JSONObject().put("role", message.role).put("content", message.content))
        }
    }

    fun decodeConversations(json: String?): List<ChatConversation> = runCatching {
        val array = JSONArray(json ?: "[]")
        buildList {
            repeat(array.length()) { index ->
                runCatching {
                    array.getJSONObject(index).let { item ->
                        ChatConversation(
                            id = item.getString("id"),
                            title = item.optString("title", "Gespräch"),
                            messages = decodeMessages(item.optJSONArray("messages") ?: JSONArray()),
                            updatedAt = item.optLong("updatedAt", 0L),
                        )
                    }
                }.getOrNull()?.let(::add)
            }
        }.sortedByDescending { it.updatedAt }
    }.getOrDefault(emptyList())

    fun encodeConversations(conversations: List<ChatConversation>): JSONArray = JSONArray().apply {
        conversations.sortedByDescending { it.updatedAt }.take(50).forEach { conversation ->
            put(
                JSONObject()
                    .put("id", conversation.id)
                    .put("title", conversation.title)
                    .put("updatedAt", conversation.updatedAt)
                    .put("messages", encodeMessages(conversation.messages))
            )
        }
    }

    fun planConversationMigration(currentJson: String?, legacyMessagesJson: String?, now: Long): ConversationMigration {
        val current = decodeConversations(currentJson)
        if (current.isNotEmpty()) return ConversationMigration(current, current.first().id, false)

        val migrated = ChatConversation(
            id = "chat-$now",
            title = "Hauptchat",
            messages = decodeMessages(legacyMessagesJson),
            updatedAt = now,
        )
        return ConversationMigration(listOf(migrated), migrated.id, true)
    }

    fun decodeDailyEntries(json: String?): List<DailyLogEntry> = runCatching {
        val array = JSONArray(json ?: "[]")
        buildList {
            repeat(array.length()) { index ->
                runCatching {
                    array.getJSONObject(index).let { item ->
                        DailyLogEntry(
                            date = item.optString("date", LocalDate.now().toString()),
                            type = item.getString("type"),
                            title = item.getString("title"),
                            calories = item.getInt("calories"),
                            protein = item.getDouble("protein"),
                            fat = item.optDouble("fat", 0.0),
                            carbs = item.optDouble("carbs", 0.0),
                            planned = item.optBoolean("planned", false),
                            portion = decodeFoodPortion(item.optJSONObject("portion")),
                        )
                    }
                }.getOrNull()?.let(::add)
            }
        }
    }.getOrDefault(emptyList())

    fun encodeDailyEntries(entries: List<DailyLogEntry>): JSONArray = JSONArray().apply {
        entries.takeLast(500).forEach { entry ->
            put(
                JSONObject()
                    .put("date", entry.date)
                    .put("type", entry.type)
                    .put("title", entry.title)
                    .put("calories", entry.calories)
                    .put("protein", entry.protein)
                    .put("fat", entry.fat)
                    .put("carbs", entry.carbs)
                    .put("planned", entry.planned)
                    .put("portion", encodeFoodPortion(entry.portion))
            )
        }
    }

    fun decodeMeasurements(json: String?): List<BodyMeasurement> = runCatching {
        val array = JSONArray(json ?: "[]")
        buildList {
            repeat(array.length()) { index ->
                runCatching {
                    array.getJSONObject(index).let { item ->
                        BodyMeasurement(
                            date = item.getString("date"),
                            weight = item.optionalDouble("weight"),
                            scaleBodyFat = item.optionalDouble("scaleBodyFat") ?: item.optionalDouble("bodyFat"),
                            neck = item.optionalDouble("neck"),
                            abdomen = item.optionalDouble("abdomen") ?: item.optionalDouble("waist"),
                            hunger = item.optionalInt("hunger"),
                            energy = item.optionalInt("energy"),
                        )
                    }
                }.getOrNull()?.let(::add)
            }
        }
    }.getOrDefault(emptyList())

    fun encodeMeasurements(measurements: List<BodyMeasurement>): JSONArray = JSONArray().apply {
        measurements.takeLast(1_000).forEach { item ->
            put(
                JSONObject()
                    .put("date", item.date)
                    .put("weight", item.weight ?: JSONObject.NULL)
                    .put("scaleBodyFat", item.scaleBodyFat ?: JSONObject.NULL)
                    .put("neck", item.neck ?: JSONObject.NULL)
                    .put("abdomen", item.abdomen ?: JSONObject.NULL)
                    .put("hunger", item.hunger ?: JSONObject.NULL)
                    .put("energy", item.energy ?: JSONObject.NULL)
            )
        }
    }

    private fun decodeMessages(array: JSONArray): List<ChatMessage> = buildList {
        repeat(array.length()) { index ->
            runCatching {
                array.getJSONObject(index).let { ChatMessage(it.getString("role"), it.getString("content")) }
            }.getOrNull()?.let(::add)
        }
    }
}
