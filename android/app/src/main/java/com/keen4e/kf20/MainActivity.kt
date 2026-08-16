package com.keen4e.kf20

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

private data class ChatMessage(val role: String, val content: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Kf20App(this) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Kf20App(context: Context) {
    val storage = remember { ChatStorage(context) }
    var messages by remember { mutableStateOf(storage.read()) }
    var draft by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    MaterialTheme(colorScheme = kf20Colors()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Column { Text("KF20", fontWeight = FontWeight.Bold); Text("Dein täglicher Agent", style = MaterialTheme.typography.labelSmall) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
                if (messages.isEmpty()) Welcome()
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { message -> MessageBubble(message) }
                    if (isSending) item { TypingIndicator() }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp)) }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.Bottom) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        modifier = Modifier.weight(1f),
                        enabled = !isSending,
                        placeholder = { Text("Wobei kann ich dir helfen?") },
                        maxLines = 5
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val input = draft.trim()
                            if (input.isEmpty()) return@Button
                            draft = ""
                            error = null
                            val updated = messages + ChatMessage("user", input)
                            messages = updated
                            storage.write(updated)
                            isSending = true
                            Thread {
                                val result = runCatching { Kf20Api.send(updated.takeLast(30)) }
                                Handler(Looper.getMainLooper()).post {
                                    isSending = false
                                    result.onSuccess { reply ->
                                        messages = messages + ChatMessage("assistant", reply)
                                        storage.write(messages)
                                    }.onFailure { failure -> error = failure.message ?: "Die Antwort konnte nicht geladen werden." }
                                }
                            }.start()
                        },
                        enabled = !isSending && draft.isNotBlank(),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Senden") }
                }
            }
        }
    }
}

@Composable private fun Welcome() = Column(modifier = Modifier.fillMaxWidth().padding(top = 80.dp, bottom = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
    Text("Guten Tag.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    Text("Schreib, was heute wichtig ist. Deine Gespräche bleiben auf deinem Gerät.", color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
            shape = RoundedCornerShape(18.dp)
        ) { Text(message.content, modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodyLarge) }
    }
}

@Composable private fun TypingIndicator() = Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
    CircularProgressIndicator(modifier = Modifier.width(18.dp).height(18.dp), strokeWidth = 2.dp)
    Spacer(Modifier.width(8.dp)); Text("KF20 denkt nach …", color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private fun kf20Colors(): ColorScheme = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF315D4B), onPrimary = Color.White, secondaryContainer = Color(0xFFE5F1E8), surface = Color(0xFFFDFBF7)
)

private class ChatStorage(context: Context) {
    private val preferences = context.getSharedPreferences("kf20", Context.MODE_PRIVATE)
    fun read(): List<ChatMessage> = runCatching {
        val array = JSONArray(preferences.getString("messages", "[]"))
        List(array.length()) { index -> array.getJSONObject(index).let { ChatMessage(it.getString("role"), it.getString("content")) } }
    }.getOrDefault(emptyList())
    fun write(messages: List<ChatMessage>) {
        val array = JSONArray(); messages.takeLast(500).forEach { array.put(JSONObject().put("role", it.role).put("content", it.content)) }
        preferences.edit().putString("messages", array.toString()).apply()
    }
}

private object Kf20Api {
    fun send(messages: List<ChatMessage>): String {
        require(!BuildConfig.KF20_API_BASE_URL.contains("REPLACE_WITH")) { "Die KF20-Serveradresse wurde noch nicht eingerichtet." }
        val request = JSONObject().put("messages", JSONArray().apply { messages.forEach { put(JSONObject().put("role", it.role).put("content", it.content)) } })
        val connection = (URL("${BuildConfig.KF20_API_BASE_URL}/v1/chat").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"; connectTimeout = 20_000; readTimeout = 90_000; doOutput = true; setRequestProperty("Content-Type", "application/json")
        }
        connection.outputStream.use { OutputStreamWriter(it).use { writer -> writer.write(request.toString()) } }
        val body = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream).bufferedReader().use(BufferedReader::readText)
        if (connection.responseCode !in 200..299) throw IllegalStateException(JSONObject(body).optString("error", "Der Server hat die Anfrage abgelehnt."))
        return JSONObject(body).getString("text").ifBlank { "Ich konnte gerade keine Textantwort erzeugen." }
    }
}

