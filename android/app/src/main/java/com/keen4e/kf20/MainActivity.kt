package com.keen4e.kf20

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import java.security.KeyStore
import java.time.LocalDate
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

private data class ChatMessage(val role: String, val content: String)
private data class AgentTask(val title: String, val done: Boolean)
private data class DailyLogEntry(val date: String, val type: String, val title: String, val calories: Int, val protein: Double)
private data class DailyRoutine(val title: String, val calories: Int, val protein: Double)
private data class WeightEntry(val date: String, val kilograms: Double)
private enum class Workspace { CHAT, DAILY_LOG, PROGRESS, TASKS }

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
    val taskStorage = remember { TaskStorage(context) }
    val dailyLogStorage = remember { DailyLogStorage(context) }
    val routineStorage = remember { RoutineStorage(context) }
    val weightStorage = remember { WeightStorage(context) }
    var messages by remember { mutableStateOf(storage.read()) }
    var tasks by remember { mutableStateOf(taskStorage.read()) }
    var dailyEntries by remember { mutableStateOf(dailyLogStorage.read()) }
    var routines by remember { mutableStateOf(routineStorage.read()) }
    var weightEntries by remember { mutableStateOf(weightStorage.read()) }
    var draft by remember { mutableStateOf("") }
    var taskDraft by remember { mutableStateOf("") }
    var logType by remember { mutableStateOf("Mahlzeit") }
    var logTitle by remember { mutableStateOf("") }
    var logCalories by remember { mutableStateOf("") }
    var logProtein by remember { mutableStateOf("") }
    var weightDraft by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(todayKey()) }
    var workspace by remember { mutableStateOf(Workspace.CHAT) }
    var isSending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    MaterialTheme(colorScheme = kf20Colors()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Column { Text("KF20", fontWeight = FontWeight.Bold); Text("Dein täglicher Agent", style = MaterialTheme.typography.labelSmall) } },
                    actions = {
                        TextButton(onClick = { workspace = Workspace.CHAT }) { Text("Chat") }
                        TextButton(onClick = { workspace = Workspace.DAILY_LOG }) { Text("Tag") }
                        TextButton(onClick = { workspace = Workspace.PROGRESS }) { Text("Verlauf") }
                        TextButton(onClick = { workspace = Workspace.TASKS }) { Text("Aufgaben") }
                        if (workspace == Workspace.CHAT) TextButton(onClick = { showDeleteDialog = true }, enabled = messages.isNotEmpty() && !isSending) { Text("Löschen") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { padding ->
            if (workspace == Workspace.CHAT) Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
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
            } else if (workspace == Workspace.TASKS) TasksScreen(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                tasks = tasks,
                draft = taskDraft,
                onDraftChange = { taskDraft = it },
                onAdd = {
                    val title = taskDraft.trim()
                    if (title.isNotEmpty()) {
                        tasks = tasks + AgentTask(title, false)
                        taskStorage.write(tasks)
                        taskDraft = ""
                    }
                },
                onToggle = { index ->
                    tasks = tasks.mapIndexed { current, task -> if (current == index) task.copy(done = !task.done) else task }
                    taskStorage.write(tasks)
                }
            ) else if (workspace == Workspace.PROGRESS) ProgressScreen(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                entries = weightEntries,
                draft = weightDraft,
                onDraftChange = { weightDraft = it },
                onAdd = {
                    val kilograms = weightDraft.replace(',', '.').toDoubleOrNull()
                    if (kilograms != null && kilograms in 25.0..500.0) {
                        weightEntries = (weightEntries.filterNot { it.date == todayKey() } + WeightEntry(todayKey(), kilograms)).sortedBy { it.date }
                        weightStorage.write(weightEntries)
                        weightDraft = ""
                    }
                }
            ) else DailyLogScreen(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                entries = dailyEntries.filter { it.date == selectedDate },
                routines = routines,
                selectedDate = selectedDate,
                type = logType,
                title = logTitle,
                calories = logCalories,
                protein = logProtein,
                onTypeChange = { logType = it },
                onTitleChange = { logTitle = it },
                onCaloriesChange = { logCalories = it },
                onProteinChange = { logProtein = it },
                onPreviousDay = { selectedDate = LocalDate.parse(selectedDate).minusDays(1).toString() },
                onToday = { selectedDate = todayKey() },
                onNextDay = { selectedDate = LocalDate.parse(selectedDate).plusDays(1).toString() },
                onUseRoutine = { routine ->
                    val entry = DailyLogEntry(selectedDate, "Mahlzeit", routine.title, routine.calories, routine.protein)
                    dailyEntries = dailyEntries + entry
                    dailyLogStorage.write(dailyEntries)
                },
                onSaveRoutine = {
                    val title = logTitle.trim()
                    if (logType == "Mahlzeit" && title.isNotEmpty()) {
                        val routine = DailyRoutine(title, logCalories.toIntOrNull() ?: 0, logProtein.replace(',', '.').toDoubleOrNull() ?: 0.0)
                        routines = (routines.filterNot { it.title.equals(routine.title, ignoreCase = true) } + routine).takeLast(20)
                        routineStorage.write(routines)
                    }
                },
                onAdd = {
                    val title = logTitle.trim()
                    if (title.isNotEmpty()) {
                        val entry = DailyLogEntry(selectedDate, logType, title, logCalories.toIntOrNull() ?: 0, logProtein.replace(',', '.').toDoubleOrNull() ?: 0.0)
                        dailyEntries = dailyEntries + entry
                        dailyLogStorage.write(dailyEntries)
                        logTitle = ""; logCalories = ""; logProtein = ""
                    }
                }
            )
        }
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Verlauf löschen?") },
                text = { Text("Alle lokal gespeicherten Nachrichten auf diesem Gerät werden entfernt.") },
                confirmButton = {
                    TextButton(onClick = {
                        messages = emptyList()
                        storage.clear()
                        showDeleteDialog = false
                    }) { Text("Endgültig löschen") }
                },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Abbrechen") } }
            )
        }
    }
}

@Composable private fun TasksScreen(
    modifier: Modifier,
    tasks: List<AgentTask>,
    draft: String,
    onDraftChange: (String) -> Unit,
    onAdd: () -> Unit,
    onToggle: (Int) -> Unit
) = Column(modifier = modifier) {
    Text("Aufgaben", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp, bottom = 4.dp))
    Text("Was soll der Agent im Blick behalten?", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(value = draft, onValueChange = onDraftChange, modifier = Modifier.weight(1f), placeholder = { Text("Neue Aufgabe") }, singleLine = true)
        Spacer(Modifier.width(8.dp))
        Button(onClick = onAdd, enabled = draft.isNotBlank()) { Text("Hinzufügen") }
    }
    LazyColumn(modifier = Modifier.weight(1f).padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(tasks.indices.toList()) { index ->
            val task = tasks[index]
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Checkbox(checked = task.done, onCheckedChange = { onToggle(index) })
                    Text(task.title, modifier = Modifier.padding(end = 8.dp), color = if (task.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    }
}

@Composable private fun ProgressScreen(
    modifier: Modifier,
    entries: List<WeightEntry>,
    draft: String,
    onDraftChange: (String) -> Unit,
    onAdd: () -> Unit
) = Column(modifier = modifier) {
    val ordered = entries.sortedBy { it.date }
    val latest = ordered.lastOrNull()
    val difference = latest?.let { it.kilograms - (ordered.firstOrNull()?.kilograms ?: it.kilograms) } ?: 0.0
    Text("Fortschritt", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp, bottom = 4.dp))
    if (latest == null) {
        Text("Erfasse dein Gewicht, um deinen Verlauf zu sehen.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
    } else {
        Text("Aktuell ${"%.1f".format(latest.kilograms)} kg · seit Start ${if (difference > 0) "+" else ""}${"%.1f".format(difference)} kg", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(value = draft, onValueChange = onDraftChange, modifier = Modifier.weight(1f), placeholder = { Text("Heutiges Gewicht in kg") }, singleLine = true)
        Spacer(Modifier.width(8.dp))
        Button(onClick = onAdd, enabled = draft.replace(',', '.').toDoubleOrNull() != null) { Text("Speichern") }
    }
    LazyColumn(modifier = Modifier.weight(1f).padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(ordered.reversed()) { entry ->
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(entry.date, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%.1f".format(entry.kilograms)} kg", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable private fun DailyLogScreen(
    modifier: Modifier,
    entries: List<DailyLogEntry>,
    routines: List<DailyRoutine>,
    selectedDate: String,
    type: String,
    title: String,
    calories: String,
    protein: String,
    onTypeChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onPreviousDay: () -> Unit,
    onToday: () -> Unit,
    onNextDay: () -> Unit,
    onUseRoutine: (DailyRoutine) -> Unit,
    onSaveRoutine: () -> Unit,
    onAdd: () -> Unit
) = Column(modifier = modifier) {
    val intake = entries.filter { it.type == "Mahlzeit" }.sumOf { it.calories }
    val burned = entries.filter { it.type == "Sport" }.sumOf { it.calories }
    val proteinTotal = entries.sumOf { it.protein }
    Text("Tageslog", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp, bottom = 4.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onPreviousDay) { Text("‹") }
        Text(selectedDate, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        TextButton(onClick = onToday, enabled = selectedDate != todayKey()) { Text("Heute") }
        TextButton(onClick = onNextDay) { Text("›") }
    }
    Text("Aufnahme $intake kcal · Sport $burned kcal · Bilanz ${intake - burned} kcal · Protein ${"%.0f".format(proteinTotal)} g", color = MaterialTheme.colorScheme.onSurfaceVariant)
    Row(modifier = Modifier.padding(top = 12.dp)) {
        listOf("Mahlzeit", "Sport", "Messung").forEach { choice ->
            TextButton(onClick = { onTypeChange(choice) }, enabled = type != choice) { Text(choice) }
        }
    }
    if (routines.isNotEmpty()) {
        Text("Routinen", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
        Row {
            routines.takeLast(3).forEach { routine ->
                TextButton(onClick = { onUseRoutine(routine) }) { Text(routine.title) }
            }
        }
    }
    OutlinedTextField(value = title, onValueChange = onTitleChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text("${type}: Bezeichnung") }, singleLine = true)
    Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(value = calories, onValueChange = onCaloriesChange, modifier = Modifier.weight(1f), placeholder = { Text(if (type == "Sport") "Verbrauch kcal" else "Kalorien") }, singleLine = true)
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(value = protein, onValueChange = onProteinChange, modifier = Modifier.weight(1f), placeholder = { Text("Protein g") }, singleLine = true)
        Spacer(Modifier.width(8.dp))
        Button(onClick = onAdd, enabled = title.isNotBlank()) { Text("Loggen") }
    }
    if (type == "Mahlzeit") TextButton(onClick = onSaveRoutine, enabled = title.isNotBlank()) { Text("Als Routine speichern") }
    LazyColumn(modifier = Modifier.weight(1f).padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(entries.reversed()) { entry ->
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(entry.type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(entry.title, fontWeight = FontWeight.Medium)
                    if (entry.calories != 0 || entry.protein != 0.0) Text("${entry.calories} kcal · ${"%.0f".format(entry.protein)} g Protein", color = MaterialTheme.colorScheme.onSecondaryContainer)
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
    private val preferences = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)
    fun read(): List<ChatMessage> = runCatching {
        val array = JSONArray(SecureStore.decrypt(preferences.getString("messages", null)) ?: "[]")
        List(array.length()) { index -> array.getJSONObject(index).let { ChatMessage(it.getString("role"), it.getString("content")) } }
    }.getOrDefault(emptyList())
    fun write(messages: List<ChatMessage>) {
        val array = JSONArray(); messages.takeLast(500).forEach { array.put(JSONObject().put("role", it.role).put("content", it.content)) }
        preferences.edit().putString("messages", SecureStore.encrypt(array.toString())).apply()
    }
    fun clear() = preferences.edit().remove("messages").apply()
}

private class DailyLogStorage(context: Context) {
    private val preferences = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)
    fun read(): List<DailyLogEntry> = runCatching {
        val array = JSONArray(SecureStore.decrypt(preferences.getString("daily_entries", null)) ?: "[]")
        List(array.length()) { index ->
            array.getJSONObject(index).let {
                DailyLogEntry(it.optString("date", todayKey()), it.getString("type"), it.getString("title"), it.getInt("calories"), it.getDouble("protein"))
            }
        }
    }.getOrDefault(emptyList())
    fun write(entries: List<DailyLogEntry>) {
        val array = JSONArray(); entries.takeLast(500).forEach { entry ->
            array.put(JSONObject().put("date", entry.date).put("type", entry.type).put("title", entry.title).put("calories", entry.calories).put("protein", entry.protein))
        }
        preferences.edit().putString("daily_entries", SecureStore.encrypt(array.toString())).apply()
    }
}

private fun todayKey(): String = LocalDate.now().toString()

private class RoutineStorage(context: Context) {
    private val preferences = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)
    fun read(): List<DailyRoutine> = runCatching {
        val array = JSONArray(SecureStore.decrypt(preferences.getString("routines", null)) ?: "[]")
        List(array.length()) { index ->
            array.getJSONObject(index).let { DailyRoutine(it.getString("title"), it.getInt("calories"), it.getDouble("protein")) }
        }
    }.getOrDefault(emptyList())
    fun write(routines: List<DailyRoutine>) {
        val array = JSONArray(); routines.forEach { routine ->
            array.put(JSONObject().put("title", routine.title).put("calories", routine.calories).put("protein", routine.protein))
        }
        preferences.edit().putString("routines", SecureStore.encrypt(array.toString())).apply()
    }
}

private class WeightStorage(context: Context) {
    private val preferences = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)
    fun read(): List<WeightEntry> = runCatching {
        val array = JSONArray(SecureStore.decrypt(preferences.getString("weight_entries", null)) ?: "[]")
        List(array.length()) { index ->
            array.getJSONObject(index).let { WeightEntry(it.getString("date"), it.getDouble("kilograms")) }
        }.sortedBy { it.date }
    }.getOrDefault(emptyList())
    fun write(entries: List<WeightEntry>) {
        val array = JSONArray(); entries.takeLast(1_000).forEach { entry ->
            array.put(JSONObject().put("date", entry.date).put("kilograms", entry.kilograms))
        }
        preferences.edit().putString("weight_entries", SecureStore.encrypt(array.toString())).apply()
    }
}

private class TaskStorage(context: Context) {
    private val preferences = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)
    fun read(): List<AgentTask> = runCatching {
        val array = JSONArray(SecureStore.decrypt(preferences.getString("tasks", null)) ?: "[]")
        List(array.length()) { index ->
            array.getJSONObject(index).let { AgentTask(it.getString("title"), it.getBoolean("done")) }
        }
    }.getOrDefault(emptyList())
    fun write(tasks: List<AgentTask>) {
        val array = JSONArray(); tasks.takeLast(500).forEach { task ->
            array.put(JSONObject().put("title", task.title).put("done", task.done))
        }
        preferences.edit().putString("tasks", SecureStore.encrypt(array.toString())).apply()
    }
}

private object SecureStore {
    private const val KEYSTORE = "AndroidKeyStore"
    private const val ALIAS = "kf20-chat-history-key"

    fun encrypt(value: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return "${Base64.encodeToString(cipher.iv, Base64.NO_WRAP)}:${Base64.encodeToString(encrypted, Base64.NO_WRAP)}"
    }

    fun decrypt(value: String?): String? {
        if (value == null) return null
        val parts = value.split(":", limit = 2)
        if (parts.size != 2) return null
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), javax.crypto.spec.GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)))
        return String(cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)), Charsets.UTF_8)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (keyStore.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
        }.generateKey()
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

