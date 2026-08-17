package com.keen4e.kf20

import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.os.Bundle
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import java.time.LocalDate
import java.util.Calendar
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

private data class ChatMessage(val role: String, val content: String)
private data class AgentTask(val title: String, val done: Boolean)
private data class DailyLogEntry(val date: String, val type: String, val title: String, val calories: Int, val protein: Double, val fat: Double, val carbs: Double)
private data class DailyRoutine(val title: String, val calories: Int, val protein: Double, val fat: Double, val carbs: Double)
private data class WeightEntry(val date: String, val kilograms: Double)
private data class ProgressPhoto(val date: String, val uri: String)
private data class ApiSettings(val baseUrl: String, val token: String)
private data class ReminderConfig(val enabled: Boolean, val hour: Int, val minute: Int)
private data class UserMemory(val text: String)
private data class ProjectEntry(val name: String, val notes: String, val status: String)
private data class PrivateFile(val date: String, val uri: String, val name: String, val mimeType: String)
private data class NutritionEstimate(val name: String, val calories: Int, val protein: Double, val fat: Double, val carbs: Double, val confidence: String, val note: String)
private data class NutritionTargets(val calories: Int, val protein: Double, val fat: Double, val carbs: Double)
private enum class Workspace { CHAT, DAILY_LOG, PROGRESS, PHOTOS, TASKS, PROJECTS, FILES }

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
    val photoStorage = remember { PhotoStorage(context) }
    val apiSettingsStorage = remember { ApiSettingsStorage(context) }
    val reminderStorage = remember { ReminderStorage(context) }
    val memoryStorage = remember { MemoryStorage(context) }
    val projectStorage = remember { ProjectStorage(context) }
    val fileStorage = remember { FileStorage(context) }
    val targetsStorage = remember { TargetsStorage(context) }
    var messages by remember { mutableStateOf(storage.read()) }
    var tasks by remember { mutableStateOf(taskStorage.read()) }
    var dailyEntries by remember { mutableStateOf(dailyLogStorage.read()) }
    var routines by remember { mutableStateOf(routineStorage.read()) }
    var weightEntries by remember { mutableStateOf(weightStorage.read()) }
    var photos by remember { mutableStateOf(photoStorage.read()) }
    var apiSettings by remember { mutableStateOf(apiSettingsStorage.read()) }
    var reminder by remember { mutableStateOf(reminderStorage.read()) }
    var memories by remember { mutableStateOf(memoryStorage.read()) }
    var projects by remember { mutableStateOf(projectStorage.read()) }
    var privateFiles by remember { mutableStateOf(fileStorage.read()) }
    var targets by remember { mutableStateOf(targetsStorage.read()) }
    var draft by remember { mutableStateOf("") }
    var taskDraft by remember { mutableStateOf("") }
    var projectNameDraft by remember { mutableStateOf("") }
    var projectNotesDraft by remember { mutableStateOf("") }
    var logType by remember { mutableStateOf("Mahlzeit") }
    var logTitle by remember { mutableStateOf("") }
    var logCalories by remember { mutableStateOf("") }
    var logProtein by remember { mutableStateOf("") }
    var logFat by remember { mutableStateOf("") }
    var logCarbs by remember { mutableStateOf("") }
    var weightDraft by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(todayKey()) }
    var workspace by remember { mutableStateOf(Workspace.CHAT) }
    var isSending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showServerSettings by remember { mutableStateOf(false) }
    var serverUrlDraft by remember { mutableStateOf(apiSettings.baseUrl) }
    var serverTokenDraft by remember { mutableStateOf(apiSettings.token) }
    var showReminderSettings by remember { mutableStateOf(false) }
    var reminderHourDraft by remember { mutableStateOf(reminder.hour.toString()) }
    var reminderMinuteDraft by remember { mutableStateOf(reminder.minute.toString().padStart(2, '0')) }
    var showMemories by remember { mutableStateOf(false) }
    var memoryDraft by remember { mutableStateOf("") }
    var mealPhotoUri by remember { mutableStateOf<String?>(null) }
    var isEstimatingNutrition by remember { mutableStateOf(false) }
    var nutritionHint by remember { mutableStateOf<String?>(null) }
    var showTargetSettings by remember { mutableStateOf(false) }
    var targetCaloriesDraft by remember { mutableStateOf(targets.calories.toString()) }
    var targetProteinDraft by remember { mutableStateOf(targets.protein.toString()) }
    var targetFatDraft by remember { mutableStateOf(targets.fat.toString()) }
    var targetCarbsDraft by remember { mutableStateOf(targets.carbs.toString()) }
    val notificationPermission = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
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
                        if (workspace == Workspace.CHAT) TextButton(onClick = { showDeleteDialog = true }, enabled = messages.isNotEmpty() && !isSending) { Text("Löschen") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(selected = workspace == Workspace.CHAT, onClick = { workspace = Workspace.CHAT }, icon = { Text("◉") }, label = { Text("Chat") })
                    NavigationBarItem(selected = workspace == Workspace.DAILY_LOG, onClick = { workspace = Workspace.DAILY_LOG }, icon = { Text("▣") }, label = { Text("Tag") })
                    NavigationBarItem(selected = workspace == Workspace.PROGRESS || workspace == Workspace.PHOTOS, onClick = { workspace = Workspace.PROGRESS }, icon = { Text("▥") }, label = { Text("Verlauf") })
                    NavigationBarItem(selected = workspace == Workspace.TASKS || workspace == Workspace.PROJECTS || workspace == Workspace.FILES, onClick = { workspace = Workspace.TASKS }, icon = { Text("✓") }, label = { Text("Aufgaben") })
                }
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
                                val result = runCatching { Kf20Api.send(updated.takeLast(30), apiSettings, memories.map { it.text }) }
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
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = { showMemories = true }) { Text("Erinnerungen") }
                    TextButton(onClick = { showServerSettings = true }) { Text("Serververbindung") }
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
                },
                onOpenProjects = { workspace = Workspace.PROJECTS }
            ) else if (workspace == Workspace.PROJECTS) ProjectsScreen(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                projects = projects,
                nameDraft = projectNameDraft,
                notesDraft = projectNotesDraft,
                onNameChange = { projectNameDraft = it },
                onNotesChange = { projectNotesDraft = it },
                onAdd = {
                    val name = projectNameDraft.trim()
                    if (name.isNotEmpty()) {
                        projects = projects + ProjectEntry(name, projectNotesDraft.trim(), "Aktiv")
                        projectStorage.write(projects)
                        projectNameDraft = ""
                        projectNotesDraft = ""
                    }
                },
                onArchive = { index ->
                    projects = projects.mapIndexed { current, project -> if (current == index) project.copy(status = "Archiviert") else project }
                    projectStorage.write(projects)
                },
                onOpenFiles = { workspace = Workspace.FILES }
            ) else if (workspace == Workspace.FILES) FilesScreen(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                context = context,
                files = privateFiles,
                onAdd = { uri ->
                    runCatching { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                    val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                    privateFiles = (privateFiles + PrivateFile(todayKey(), uri.toString(), displayName(context, uri), mimeType)).takeLast(200)
                    fileStorage.write(privateFiles)
                },
                onRemove = { index ->
                    privateFiles = privateFiles.filterIndexed { current, _ -> current != index }
                    fileStorage.write(privateFiles)
                }
            ) else if (workspace == Workspace.PROGRESS) ProgressScreen(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                entries = weightEntries,
                draft = weightDraft,
                onDraftChange = { weightDraft = it },
                onOpenPhotos = { workspace = Workspace.PHOTOS },
                onAdd = {
                    val kilograms = weightDraft.replace(',', '.').toDoubleOrNull()
                    if (kilograms != null && kilograms in 25.0..500.0) {
                        weightEntries = (weightEntries.filterNot { it.date == todayKey() } + WeightEntry(todayKey(), kilograms)).sortedBy { it.date }
                        weightStorage.write(weightEntries)
                        weightDraft = ""
                    }
                }
            ) else if (workspace == Workspace.PHOTOS) PhotosScreen(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                context = context,
                photos = photos,
                onAdd = { uri ->
                    runCatching { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                    photos = (photos + ProgressPhoto(todayKey(), uri.toString())).takeLast(200)
                    photoStorage.write(photos)
                }
            ) else DailyLogScreen(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                entries = dailyEntries.filter { it.date == selectedDate },
                routines = routines,
                targets = targets,
                selectedDate = selectedDate,
                type = logType,
                title = logTitle,
                calories = logCalories,
                protein = logProtein,
                fat = logFat,
                carbs = logCarbs,
                onTypeChange = { logType = it },
                onTitleChange = { logTitle = it },
                onCaloriesChange = { logCalories = it },
                onProteinChange = { logProtein = it },
                onFatChange = { logFat = it },
                onCarbsChange = { logCarbs = it },
                photoUri = mealPhotoUri,
                isEstimating = isEstimatingNutrition,
                nutritionHint = nutritionHint,
                onPhotoChange = { mealPhotoUri = it },
                onEstimate = {
                    val description = logTitle.trim()
                    if (description.isNotEmpty() || mealPhotoUri != null) {
                        isEstimatingNutrition = true
                        nutritionHint = null
                        Thread {
                            val result = runCatching { NutritionApi.estimate(description, mealPhotoUri, context, apiSettings) }
                            Handler(Looper.getMainLooper()).post {
                                isEstimatingNutrition = false
                                result.onSuccess { estimate ->
                                    logTitle = estimate.name
                                    logCalories = estimate.calories.toString()
                                    logProtein = "%.1f".format(estimate.protein)
                                    logFat = "%.1f".format(estimate.fat)
                                    logCarbs = "%.1f".format(estimate.carbs)
                                    nutritionHint = "KI-Schätzung (${estimate.confidence}): ${estimate.note} Bitte vor dem Loggen prüfen."
                                }.onFailure { failure -> nutritionHint = failure.message ?: "Die Nährwert-Schätzung konnte nicht geladen werden." }
                            }
                        }.start()
                    }
                },
                onReminderSettings = { showReminderSettings = true },
                onTargetSettings = { showTargetSettings = true },
                onPreviousDay = { selectedDate = LocalDate.parse(selectedDate).minusDays(1).toString() },
                onToday = { selectedDate = todayKey() },
                onNextDay = { selectedDate = LocalDate.parse(selectedDate).plusDays(1).toString() },
                onUseRoutine = { routine ->
                    val entry = DailyLogEntry(selectedDate, "Mahlzeit", routine.title, routine.calories, routine.protein, routine.fat, routine.carbs)
                    dailyEntries = dailyEntries + entry
                    dailyLogStorage.write(dailyEntries)
                },
                onSaveRoutine = {
                    val title = logTitle.trim()
                    if (logType == "Mahlzeit" && title.isNotEmpty()) {
                        val routine = DailyRoutine(title, logCalories.toIntOrNull() ?: 0, logProtein.replace(',', '.').toDoubleOrNull() ?: 0.0, logFat.replace(',', '.').toDoubleOrNull() ?: 0.0, logCarbs.replace(',', '.').toDoubleOrNull() ?: 0.0)
                        routines = (routines.filterNot { it.title.equals(routine.title, ignoreCase = true) } + routine).takeLast(20)
                        routineStorage.write(routines)
                    }
                },
                onAdd = {
                    val title = logTitle.trim()
                    if (title.isNotEmpty()) {
                        val entry = DailyLogEntry(selectedDate, logType, title, logCalories.toIntOrNull() ?: 0, logProtein.replace(',', '.').toDoubleOrNull() ?: 0.0, logFat.replace(',', '.').toDoubleOrNull() ?: 0.0, logCarbs.replace(',', '.').toDoubleOrNull() ?: 0.0)
                        dailyEntries = dailyEntries + entry
                        dailyLogStorage.write(dailyEntries)
                        logTitle = ""; logCalories = ""; logProtein = ""; logFat = ""; logCarbs = ""; mealPhotoUri = null; nutritionHint = null
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
        if (showServerSettings) {
            AlertDialog(
                onDismissRequest = { showServerSettings = false },
                title = { Text("Serververbindung") },
                text = {
                    Column {
                        Text("Die Zugangsdaten bleiben verschlüsselt auf diesem Gerät. Für eine Veröffentlichung wird diese Testverbindung durch eine echte Anmeldung ersetzt.")
                        OutlinedTextField(value = serverUrlDraft, onValueChange = { serverUrlDraft = it }, label = { Text("HTTPS-Serveradresse") }, singleLine = true)
                        OutlinedTextField(value = serverTokenDraft, onValueChange = { serverTokenDraft = it }, label = { Text("Zugangstoken") }, singleLine = true)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        apiSettings = ApiSettings(serverUrlDraft.trim().trimEnd('/'), serverTokenDraft.trim())
                        apiSettingsStorage.write(apiSettings)
                        showServerSettings = false
                    }, enabled = serverUrlDraft.startsWith("http") && serverTokenDraft.isNotBlank()) { Text("Speichern") }
                },
                dismissButton = { TextButton(onClick = { showServerSettings = false }) { Text("Abbrechen") } }
            )
        }
        if (showMemories) {
            AlertDialog(
                onDismissRequest = { showMemories = false },
                title = { Text("Langzeit-Erinnerungen") },
                text = {
                    Column {
                        Text("Nur Dinge eintragen, die KF20 bei künftigen Gesprächen berücksichtigen soll. Du kannst sie jederzeit entfernen.")
                        OutlinedTextField(value = memoryDraft, onValueChange = { memoryDraft = it }, label = { Text("Neue Erinnerung") }, modifier = Modifier.fillMaxWidth())
                        memories.forEachIndexed { index, memory ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(memory.text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                TextButton(onClick = {
                                    memories = memories.filterIndexed { current, _ -> current != index }
                                    memoryStorage.write(memories)
                                }) { Text("Entfernen") }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val text = memoryDraft.trim()
                        if (text.isNotEmpty()) {
                            memories = (memories + UserMemory(text)).takeLast(20)
                            memoryStorage.write(memories)
                        }
                        memoryDraft = ""
                        showMemories = false
                    }, enabled = memoryDraft.isNotBlank()) { Text("Speichern") }
                },
                dismissButton = { TextButton(onClick = { showMemories = false }) { Text("Schließen") } }
            )
        }
        if (showTargetSettings) {
            AlertDialog(
                onDismissRequest = { showTargetSettings = false },
                title = { Text("Tagesziele") },
                text = {
                    Column {
                        Text("Passe die Tagesziele an. Sie dienen als Orientierung, nicht als medizinische Empfehlung.")
                        OutlinedTextField(value = targetCaloriesDraft, onValueChange = { targetCaloriesDraft = it }, label = { Text("Kalorien (kcal)") }, singleLine = true)
                        Row {
                            OutlinedTextField(value = targetProteinDraft, onValueChange = { targetProteinDraft = it }, label = { Text("Protein (g)") }, modifier = Modifier.weight(1f), singleLine = true)
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(value = targetFatDraft, onValueChange = { targetFatDraft = it }, label = { Text("Fett (g)") }, modifier = Modifier.weight(1f), singleLine = true)
                        }
                        OutlinedTextField(value = targetCarbsDraft, onValueChange = { targetCarbsDraft = it }, label = { Text("Carbs (g)") }, singleLine = true)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val calories = targetCaloriesDraft.toIntOrNull()
                        val protein = targetProteinDraft.replace(',', '.').toDoubleOrNull()
                        val fat = targetFatDraft.replace(',', '.').toDoubleOrNull()
                        val carbs = targetCarbsDraft.replace(',', '.').toDoubleOrNull()
                        if (calories != null && protein != null && fat != null && carbs != null) {
                            targets = NutritionTargets(calories, protein, fat, carbs)
                            targetsStorage.write(targets)
                            showTargetSettings = false
                        }
                    }) { Text("Speichern") }
                },
                dismissButton = { TextButton(onClick = { showTargetSettings = false }) { Text("Abbrechen") } }
            )
        }
        if (showReminderSettings) {
            AlertDialog(
                onDismissRequest = { showReminderSettings = false },
                title = { Text("Tägliche Erinnerung") },
                text = {
                    Column {
                        Text("KF20 erinnert dich täglich an dein Tageslog. Android fragt gegebenenfalls nach der Benachrichtigungsfreigabe.")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(value = reminderHourDraft, onValueChange = { reminderHourDraft = it }, label = { Text("Stunde") }, modifier = Modifier.weight(1f), singleLine = true)
                            Text(":", modifier = Modifier.padding(horizontal = 8.dp))
                            OutlinedTextField(value = reminderMinuteDraft, onValueChange = { reminderMinuteDraft = it }, label = { Text("Minute") }, modifier = Modifier.weight(1f), singleLine = true)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val hour = reminderHourDraft.toIntOrNull()
                        val minute = reminderMinuteDraft.toIntOrNull()
                        if (hour != null && minute != null && hour in 0..23 && minute in 0..59) {
                            reminder = ReminderConfig(true, hour, minute)
                            reminderStorage.write(reminder)
                            DailyReminder.schedule(context, reminder)
                            if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission("android.permission.POST_NOTIFICATIONS") != android.content.pm.PackageManager.PERMISSION_GRANTED) notificationPermission.launch("android.permission.POST_NOTIFICATIONS")
                            showReminderSettings = false
                        }
                    }, enabled = (reminderHourDraft.toIntOrNull()?.let { it in 0..23 } == true) && (reminderMinuteDraft.toIntOrNull()?.let { it in 0..59 } == true)) { Text("Aktivieren") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        reminder = reminder.copy(enabled = false)
                        reminderStorage.write(reminder)
                        DailyReminder.cancel(context)
                        showReminderSettings = false
                    }) { Text("Deaktivieren") }
                }
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
    onToggle: (Int) -> Unit,
    onOpenProjects: () -> Unit
) = Column(modifier = modifier) {
    Text("Aufgaben", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp, bottom = 4.dp))
    Text("Was soll der Agent im Blick behalten?", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
    TextButton(onClick = onOpenProjects) { Text("Projekt-Arbeitsbereich öffnen") }
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

@Composable private fun ProjectsScreen(
    modifier: Modifier,
    projects: List<ProjectEntry>,
    nameDraft: String,
    notesDraft: String,
    onNameChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onAdd: () -> Unit,
    onArchive: (Int) -> Unit,
    onOpenFiles: () -> Unit
) = Column(modifier = modifier) {
    Text("Projekte", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp, bottom = 4.dp))
    Text("Halte laufende Vorhaben, Kontext und Entscheidungen an einem Ort fest.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
    TextButton(onClick = onOpenFiles) { Text("Private Dateien öffnen") }
    OutlinedTextField(value = nameDraft, onValueChange = onNameChange, modifier = Modifier.fillMaxWidth(), label = { Text("Projektname") }, singleLine = true)
    OutlinedTextField(value = notesDraft, onValueChange = onNotesChange, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), label = { Text("Kontext oder nächste Schritte") }, minLines = 2, maxLines = 4)
    Button(onClick = onAdd, enabled = nameDraft.isNotBlank(), modifier = Modifier.padding(top = 8.dp)) { Text("Projekt anlegen") }
    LazyColumn(modifier = Modifier.weight(1f).padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(projects.indices.toList()) { index ->
            val project = projects[index]
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(project.status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(project.name, fontWeight = FontWeight.Medium)
                    if (project.notes.isNotBlank()) Text(project.notes, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(top = 4.dp))
                    if (project.status == "Aktiv") TextButton(onClick = { onArchive(index) }) { Text("Archivieren") }
                }
            }
        }
    }
}

@Composable private fun FilesScreen(
    modifier: Modifier,
    context: Context,
    files: List<PrivateFile>,
    onAdd: (Uri) -> Unit,
    onRemove: (Int) -> Unit
) {
    val picker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> if (uri != null) onAdd(uri) }
    )
    Column(modifier = modifier) {
        Text("Private Dateien", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp, bottom = 4.dp))
        Text("Die Datei bleibt auf deinem Gerät bzw. in deinem gewählten Speicher. KF20 merkt sich nur den privaten Android-Zugriff.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
        Button(onClick = { picker.launch(arrayOf("*/*")) }) { Text("Datei auswählen") }
        LazyColumn(modifier = Modifier.weight(1f).padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(files.indices.toList()) { index ->
                val file = files[index]
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(file.name, fontWeight = FontWeight.Medium)
                            Text("${file.date} · ${file.mimeType}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { onRemove(index) }) { Text("Entfernen") }
                    }
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
    onOpenPhotos: () -> Unit,
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
    TextButton(onClick = onOpenPhotos) { Text("Fortschrittsfotos öffnen") }
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

@Composable private fun PhotosScreen(
    modifier: Modifier,
    context: Context,
    photos: List<ProgressPhoto>,
    onAdd: (Uri) -> Unit
) {
    val picker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> if (uri != null) onAdd(uri) }
    )
    Column(modifier = modifier) {
        Text("Fortschrittsfotos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp, bottom = 4.dp))
        Text("Wähle ein Bild vom Gerät. Es bleibt über den Android-Systemzugriff privat verknüpft.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
        Button(onClick = { picker.launch(arrayOf("image/*")) }) { Text("Foto auswählen") }
        LazyColumn(modifier = Modifier.weight(1f).padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(photos.reversed()) { photo ->
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(photo.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val bitmap = remember(photo.uri) {
                            runCatching { context.contentResolver.openInputStream(Uri.parse(photo.uri))?.use(BitmapFactory::decodeStream) }.getOrNull()
                        }
                        if (bitmap != null) Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Fortschrittsfoto vom ${photo.date}", modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                        else Text("Bild nicht mehr verfügbar. Bitte erneut auswählen.", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable private fun DailyLogScreen(
    modifier: Modifier,
    entries: List<DailyLogEntry>,
    routines: List<DailyRoutine>,
    targets: NutritionTargets,
    selectedDate: String,
    type: String,
    title: String,
    calories: String,
    protein: String,
    fat: String,
    carbs: String,
    onTypeChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onCarbsChange: (String) -> Unit,
    photoUri: String?,
    isEstimating: Boolean,
    nutritionHint: String?,
    onPhotoChange: (String?) -> Unit,
    onEstimate: () -> Unit,
    onReminderSettings: () -> Unit,
    onTargetSettings: () -> Unit,
    onPreviousDay: () -> Unit,
    onToday: () -> Unit,
    onNextDay: () -> Unit,
    onUseRoutine: (DailyRoutine) -> Unit,
    onSaveRoutine: () -> Unit,
    onAdd: () -> Unit
) = Column(modifier = modifier) {
    val mealPhotoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> onPhotoChange(uri?.toString()) }
    )
    val intake = entries.filter { it.type == "Mahlzeit" }.sumOf { it.calories }
    val burned = entries.filter { it.type == "Sport" }.sumOf { it.calories }
    val proteinTotal = entries.sumOf { it.protein }
    val fatTotal = entries.sumOf { it.fat }
    val carbsTotal = entries.sumOf { it.carbs }
    Text("Tageslog", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp, bottom = 4.dp))
    Row {
        TextButton(onClick = onTargetSettings) { Text("Ziele anpassen") }
        TextButton(onClick = onReminderSettings) { Text("Erinnerung") }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onPreviousDay) { Text("‹") }
        Text(selectedDate, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        TextButton(onClick = onToday, enabled = selectedDate != todayKey()) { Text("Heute") }
        TextButton(onClick = onNextDay) { Text("›") }
    }
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Heute noch ${maxOf(0, targets.calories - (intake - burned))} kcal", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Aufnahme $intake · Sport $burned · Bilanz ${intake - burned} kcal", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        MacroCard("Kal", intake.toDouble(), targets.calories.toDouble(), "kcal", Modifier.weight(1f))
        MacroCard("Protein", proteinTotal, targets.protein, "g", Modifier.weight(1f))
        MacroCard("Fett", fatTotal, targets.fat, "g", Modifier.weight(1f))
        MacroCard("Carbs", carbsTotal, targets.carbs, "g", Modifier.weight(1f))
    }
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
    OutlinedTextField(value = title, onValueChange = onTitleChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text(if (type == "Mahlzeit") "z. B. zwei Brötchen mit Ei und Käse" else "${type}: Bezeichnung") }, minLines = if (type == "Mahlzeit") 2 else 1, maxLines = 3)
    if (type == "Mahlzeit") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { mealPhotoPicker.launch(arrayOf("image/*")) }) { Text(if (photoUri == null) "Foto hinzufügen" else "Foto gewählt") }
            Button(onClick = onEstimate, enabled = !isEstimating && (title.isNotBlank() || photoUri != null)) { Text(if (isEstimating) "Schätze …" else "KI-Nährwerte schätzen") }
        }
        if (photoUri != null) Text("Das Foto wird nur für diese Schätzung an deinen eingerichteten KF20-Server gesendet und nicht im Tageslog gespeichert.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        nutritionHint?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
    }
    Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(value = calories, onValueChange = onCaloriesChange, modifier = Modifier.weight(1f), placeholder = { Text(if (type == "Sport") "Verbrauch kcal" else "Kalorien") }, singleLine = true)
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(value = protein, onValueChange = onProteinChange, modifier = Modifier.weight(1f), placeholder = { Text("Protein g") }, singleLine = true)
    }
    Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(value = fat, onValueChange = onFatChange, modifier = Modifier.weight(1f), placeholder = { Text("Fett g") }, singleLine = true)
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(value = carbs, onValueChange = onCarbsChange, modifier = Modifier.weight(1f), placeholder = { Text("Carbs g") }, singleLine = true)
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
                    if (entry.calories != 0 || entry.protein != 0.0 || entry.fat != 0.0 || entry.carbs != 0.0) Text("${entry.calories} kcal · P ${"%.0f".format(entry.protein)} g · F ${"%.0f".format(entry.fat)} g · C ${"%.0f".format(entry.carbs)} g", color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    }
}

@Composable private fun MacroCard(label: String, value: Double, target: Double, unit: String, modifier: Modifier = Modifier) {
    val progress = if (target > 0) (value / target).coerceIn(0.0, 1.0).toFloat() else 0f
    val percentage = if (target > 0) (value / target * 100).toInt() else 0
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = modifier) {
        Column(modifier = Modifier.padding(9.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${"%.0f".format(value)}", fontWeight = FontWeight.Bold)
            Text("/ ${"%.0f".format(target)} $unit · $percentage%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp), color = MaterialTheme.colorScheme.primary)
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
    primary = Color(0xFF0E4B36),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7F1D2),
    onPrimaryContainer = Color(0xFF002116),
    secondaryContainer = Color(0xFFE4EFE5),
    onSecondaryContainer = Color(0xFF16382B),
    surface = Color(0xFFFFFBF5),
    background = Color(0xFFFFFBF5)
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
                DailyLogEntry(it.optString("date", todayKey()), it.getString("type"), it.getString("title"), it.getInt("calories"), it.getDouble("protein"), it.optDouble("fat", 0.0), it.optDouble("carbs", 0.0))
            }
        }
    }.getOrDefault(emptyList())
    fun write(entries: List<DailyLogEntry>) {
        val array = JSONArray(); entries.takeLast(500).forEach { entry ->
            array.put(JSONObject().put("date", entry.date).put("type", entry.type).put("title", entry.title).put("calories", entry.calories).put("protein", entry.protein).put("fat", entry.fat).put("carbs", entry.carbs))
        }
        preferences.edit().putString("daily_entries", SecureStore.encrypt(array.toString())).apply()
    }
}

private fun todayKey(): String = LocalDate.now().toString()

private fun displayName(context: Context, uri: Uri): String = runCatching {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)) else uri.lastPathSegment
    }
}.getOrNull() ?: uri.lastPathSegment ?: "Datei"

private class RoutineStorage(context: Context) {
    private val preferences = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)
    fun read(): List<DailyRoutine> = runCatching {
        val array = JSONArray(SecureStore.decrypt(preferences.getString("routines", null)) ?: "[]")
        List(array.length()) { index ->
            array.getJSONObject(index).let { DailyRoutine(it.getString("title"), it.getInt("calories"), it.getDouble("protein"), it.optDouble("fat", 0.0), it.optDouble("carbs", 0.0)) }
        }
    }.getOrDefault(emptyList())
    fun write(routines: List<DailyRoutine>) {
        val array = JSONArray(); routines.forEach { routine ->
            array.put(JSONObject().put("title", routine.title).put("calories", routine.calories).put("protein", routine.protein).put("fat", routine.fat).put("carbs", routine.carbs))
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

private class PhotoStorage(context: Context) {
    private val preferences = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)
    fun read(): List<ProgressPhoto> = runCatching {
        val array = JSONArray(SecureStore.decrypt(preferences.getString("progress_photos", null)) ?: "[]")
        List(array.length()) { index ->
            array.getJSONObject(index).let { ProgressPhoto(it.getString("date"), it.getString("uri")) }
        }
    }.getOrDefault(emptyList())
    fun write(photos: List<ProgressPhoto>) {
        val array = JSONArray(); photos.forEach { photo ->
            array.put(JSONObject().put("date", photo.date).put("uri", photo.uri))
        }
        preferences.edit().putString("progress_photos", SecureStore.encrypt(array.toString())).apply()
    }
}

private class ApiSettingsStorage(context: Context) {
    private val preferences = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)
    fun read(): ApiSettings = runCatching {
        val data = JSONObject(SecureStore.decrypt(preferences.getString("api_settings", null)) ?: "{}")
        ApiSettings(data.optString("baseUrl"), data.optString("token"))
    }.getOrDefault(ApiSettings("", ""))
    fun write(settings: ApiSettings) {
        val data = JSONObject().put("baseUrl", settings.baseUrl).put("token", settings.token)
        preferences.edit().putString("api_settings", SecureStore.encrypt(data.toString())).apply()
    }
}

private class ReminderStorage(context: Context) {
    private val preferences = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)
    fun read(): ReminderConfig = runCatching {
        val data = JSONObject(SecureStore.decrypt(preferences.getString("daily_reminder", null)) ?: "{}")
        ReminderConfig(data.optBoolean("enabled", false), data.optInt("hour", 20), data.optInt("minute", 0))
    }.getOrDefault(ReminderConfig(false, 20, 0))
    fun write(config: ReminderConfig) {
        val data = JSONObject().put("enabled", config.enabled).put("hour", config.hour).put("minute", config.minute)
        preferences.edit().putString("daily_reminder", SecureStore.encrypt(data.toString())).apply()
    }
}

private class MemoryStorage(context: Context) {
    private val preferences = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)
    fun read(): List<UserMemory> = runCatching {
        val array = JSONArray(SecureStore.decrypt(preferences.getString("memories", null)) ?: "[]")
        List(array.length()) { index -> UserMemory(array.getString(index)) }
    }.getOrDefault(emptyList())
    fun write(memories: List<UserMemory>) {
        val array = JSONArray(); memories.forEach { array.put(it.text) }
        preferences.edit().putString("memories", SecureStore.encrypt(array.toString())).apply()
    }
}

private class ProjectStorage(context: Context) {
    private val preferences = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)
    fun read(): List<ProjectEntry> = runCatching {
        val array = JSONArray(SecureStore.decrypt(preferences.getString("projects", null)) ?: "[]")
        List(array.length()) { index ->
            array.getJSONObject(index).let { ProjectEntry(it.getString("name"), it.optString("notes"), it.optString("status", "Aktiv")) }
        }
    }.getOrDefault(emptyList())
    fun write(projects: List<ProjectEntry>) {
        val array = JSONArray(); projects.takeLast(100).forEach { project ->
            array.put(JSONObject().put("name", project.name).put("notes", project.notes).put("status", project.status))
        }
        preferences.edit().putString("projects", SecureStore.encrypt(array.toString())).apply()
    }
}

private class FileStorage(context: Context) {
    private val preferences = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)
    fun read(): List<PrivateFile> = runCatching {
        val array = JSONArray(SecureStore.decrypt(preferences.getString("private_files", null)) ?: "[]")
        List(array.length()) { index ->
            array.getJSONObject(index).let {
                PrivateFile(it.getString("date"), it.getString("uri"), it.getString("name"), it.optString("mimeType", "application/octet-stream"))
            }
        }
    }.getOrDefault(emptyList())
    fun write(files: List<PrivateFile>) {
        val array = JSONArray(); files.takeLast(200).forEach { file ->
            array.put(JSONObject().put("date", file.date).put("uri", file.uri).put("name", file.name).put("mimeType", file.mimeType))
        }
        preferences.edit().putString("private_files", SecureStore.encrypt(array.toString())).apply()
    }
}

private class TargetsStorage(context: Context) {
    private val preferences = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)
    fun read(): NutritionTargets = runCatching {
        val data = JSONObject(SecureStore.decrypt(preferences.getString("nutrition_targets", null)) ?: "{}")
        NutritionTargets(data.optInt("calories", 2_000), data.optDouble("protein", 150.0), data.optDouble("fat", 70.0), data.optDouble("carbs", 200.0))
    }.getOrDefault(NutritionTargets(2_000, 150.0, 70.0, 200.0))
    fun write(targets: NutritionTargets) {
        val data = JSONObject().put("calories", targets.calories).put("protein", targets.protein).put("fat", targets.fat).put("carbs", targets.carbs)
        preferences.edit().putString("nutrition_targets", SecureStore.encrypt(data.toString())).apply()
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

private object DailyReminder {
    private const val REQUEST_CODE = 20
    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    fun schedule(context: Context, config: ReminderConfig) {
        val time = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, config.hour)
            set(Calendar.MINUTE, config.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            time.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent(context)
        )
    }
    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context))
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_reminder"
        if (Build.VERSION.SDK_INT >= 26) {
            notificationManager.createNotificationChannel(NotificationChannel(channelId, "Tägliche Erinnerung", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val openApp = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.keen4e.kf20.R.drawable.ic_kf20)
            .setContentTitle("KF20 Tagescheck")
            .setContentText("Zeit für dein Tageslog: Mahlzeiten, Bewegung und Fortschritt.")
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(20, notification)
    }
}

private object Kf20Api {
    fun send(messages: List<ChatMessage>, settings: ApiSettings, memories: List<String>): String {
        val baseUrl = settings.baseUrl.ifBlank { BuildConfig.KF20_API_BASE_URL }
        require(!baseUrl.contains("REPLACE_WITH") && settings.token.isNotBlank()) { "Bitte richte zuerst die Serververbindung ein." }
        val request = JSONObject()
            .put("messages", JSONArray().apply { messages.forEach { put(JSONObject().put("role", it.role).put("content", it.content)) } })
            .put("memories", JSONArray().apply { memories.take(20).forEach { put(it) } })
        val connection = (URL("$baseUrl/v1/chat").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"; connectTimeout = 20_000; readTimeout = 90_000; doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer ${settings.token}")
        }
        connection.outputStream.use { OutputStreamWriter(it).use { writer -> writer.write(request.toString()) } }
        val body = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream).bufferedReader().use(BufferedReader::readText)
        if (connection.responseCode !in 200..299) throw IllegalStateException(JSONObject(body).optString("error", "Der Server hat die Anfrage abgelehnt."))
        return JSONObject(body).getString("text").ifBlank { "Ich konnte gerade keine Textantwort erzeugen." }
    }
}

private object NutritionApi {
    fun estimate(description: String, photoUri: String?, context: Context, settings: ApiSettings): NutritionEstimate {
        val baseUrl = settings.baseUrl.ifBlank { BuildConfig.KF20_API_BASE_URL }
        require(!baseUrl.contains("REPLACE_WITH") && settings.token.isNotBlank()) { "Bitte richte zuerst die Serververbindung ein." }
        val request = JSONObject().put("description", description)
        photoUri?.let { request.put("imageDataUrl", resizedImageDataUrl(context, Uri.parse(it))) }
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

    private fun resizedImageDataUrl(context: Context, uri: Uri): String {
        val source = context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream) ?: throw IllegalArgumentException("Das Bild konnte nicht gelesen werden.")
        val largestSide = maxOf(source.width, source.height)
        val bitmap: Bitmap = if (largestSide > 1280) {
            val scale = 1280.0 / largestSide
            Bitmap.createScaledBitmap(source, (source.width * scale).toInt(), (source.height * scale).toInt(), true)
        } else source
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 82, output)
        if (bitmap !== source) bitmap.recycle()
        source.recycle()
        val bytes = output.toByteArray()
        require(bytes.size <= 1_000_000) { "Das Bild ist zu groß. Bitte wähle ein kleineres Foto." }
        return "data:image/jpeg;base64,${Base64.encodeToString(bytes, Base64.NO_WRAP)}"
    }
}

