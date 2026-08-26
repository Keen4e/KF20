package com.keen4e.kf20

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

internal object Kf20Api {
    fun send(messages: List<ChatMessage>, settings: ApiSettings, memories: List<String>, webSearch: Boolean): String {
        val baseUrl = settings.baseUrl.ifBlank { BuildConfig.KF20_API_BASE_URL }
        require(!baseUrl.contains("REPLACE_WITH") && settings.token.isNotBlank()) { "Bitte richte zuerst die Serververbindung ein." }
        val request = JSONObject()
            .put("messages", JSONArray().apply { messages.forEach { put(JSONObject().put("role", it.role).put("content", it.content)) } })
            .put("memories", JSONArray().apply { memories.take(20).forEach { put(it) } })
            .put("webSearch", webSearch)
        val connection = (URL("$baseUrl/v1/chat").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"; connectTimeout = 20_000; readTimeout = 90_000; doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer ${settings.token}")
        }
        connection.outputStream.use { OutputStreamWriter(it).use { writer -> writer.write(request.toString()) } }
        val body = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream).bufferedReader().use(BufferedReader::readText)
        if (connection.responseCode !in 200..299) throw IllegalStateException(JSONObject(body).optString("error", "Der Server hat die Anfrage abgelehnt."))
        val payload = JSONObject(body)
        val text = payload.getString("text").ifBlank { "Ich konnte gerade keine Textantwort erzeugen." }
        val sources = payload.optJSONArray("sources") ?: JSONArray()
        if (sources.length() == 0) return text
        val sourceText = buildString {
            append("\n\nQuellen:")
            for (index in 0 until sources.length()) {
                val source = sources.getJSONObject(index)
                append("\n• ").append(source.optString("title", "Quelle")).append("\n").append(source.getString("url"))
            }
        }
        return text + sourceText
    }
}

internal object NutritionApi {
    fun estimate(description: String, imageDataUrl: String?, settings: ApiSettings): NutritionEstimate {
        val baseUrl = settings.baseUrl.ifBlank { BuildConfig.KF20_API_BASE_URL }
        require(!baseUrl.contains("REPLACE_WITH") && settings.token.isNotBlank()) { "Bitte richte zuerst die Serververbindung ein." }
        val request = JSONObject().put("description", description)
        imageDataUrl?.let { request.put("imageDataUrl", it) }
        val connection = (URL("$baseUrl/v1/nutrition/analyze").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"; connectTimeout = 20_000; readTimeout = 90_000; doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer ${settings.token}")
        }
        connection.outputStream.use { OutputStreamWriter(it).use { writer -> writer.write(request.toString()) } }
        val body = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream).bufferedReader().use(BufferedReader::readText)
        if (connection.responseCode !in 200..299) throw IllegalStateException(JSONObject(body).optString("error", "Die Nährwert-Schätzung wurde abgelehnt."))
        val estimate = JSONObject(body).getJSONObject("estimate")
        return NutritionEstimate(estimate.getString("name"), estimate.getInt("calories"), estimate.getDouble("protein"), estimate.getDouble("fat"), estimate.getDouble("carbs"), estimate.getString("confidence"), estimate.getString("note"))
    }
}


