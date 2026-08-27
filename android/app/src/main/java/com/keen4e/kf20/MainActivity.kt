package com.keen4e.kf20

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.os.Bundle
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.Instant
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

private enum class Workspace { CHAT, DAILY_LOG, STATISTICS, STANDARDS, PROGRESS, PHOTOS, TASKS, PROJECTS, FILES }
internal enum class AppStyle(val label: String, val description: String) {
    PERFORMANCE_DARK("Performance Dark", "Dunkel, sportlich und nah an der Morgen-Check-Referenz"),
    HEALTH_LIGHT("Health Light", "Hell, ruhig und mit viel visuellem Freiraum"),
    DATA_ATHLETE("Data Athlete", "Dunkelblau, technisch und diagrammorientiert")
}

private val CaloriesGreen = Color(0xFF0E5A42)
private val ProteinBlue = Color(0xFF4B70D6)
private val FatAmber = Color(0xFFE2A13A)
private val CarbsCoral = Color(0xFFD56E61)
private val EnergyTeal = Color(0xFF2B9C91)
private val HungerPurple = Color(0xFF8A64C7)

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
    val sportStorage = remember { SportStorage(context) }
    val measurementStorage = remember { MeasurementStorage(context) }
    val healthProfileStorage = remember { HealthProfileStorage(context) }
    val uiPreferencesStorage = remember { UiPreferencesStorage(context) }
    val initialConversations = remember { storage.read() }
    var conversations by remember { mutableStateOf(initialConversations) }
    var activeConversationId by remember {
        mutableStateOf(storage.readActiveId().takeIf { candidate -> initialConversations.any { it.id == candidate } } ?: initialConversations.first().id)
    }
    val messages = conversations.firstOrNull { it.id == activeConversationId }?.messages.orEmpty()
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
    var sportSessions by remember { mutableStateOf(sportStorage.read()) }
    var measurements by remember {
        mutableStateOf(
            measurementStorage.read().ifEmpty {
                weightStorage.read().map { BodyMeasurement(it.date, it.kilograms, null, null, null, null, null) }
            }
        )
    }
    var healthProfile by remember { mutableStateOf(healthProfileStorage.read()) }
    var appStyle by remember { mutableStateOf(uiPreferencesStorage.read()) }
    var draft by remember { mutableStateOf("") }
    var taskDraft by remember { mutableStateOf("") }
    var projectNameDraft by remember { mutableStateOf("") }
    var projectNotesDraft by remember { mutableStateOf("") }
    var mealDescription by remember { mutableStateOf("") }
    var logTitle by remember { mutableStateOf("") }
    var logCalories by remember { mutableStateOf("") }
    var logProtein by remember { mutableStateOf("") }
    var logFat by remember { mutableStateOf("") }
    var logCarbs by remember { mutableStateOf("") }
    var weightDraft by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(todayKey()) }
    var workspace by remember { mutableStateOf(Workspace.DAILY_LOG) }
    var isSending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showConversationBrowser by remember { mutableStateOf(false) }
    var conversationSearch by remember { mutableStateOf("") }
    var conversationTitleDraft by remember { mutableStateOf("") }
    var pendingConversationDeleteId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showServerSettings by remember { mutableStateOf(false) }
    var serverUrlDraft by remember { mutableStateOf(apiSettings.baseUrl) }
    var serverTokenDraft by remember { mutableStateOf(apiSettings.token) }
    var showReminderSettings by remember { mutableStateOf(false) }
    var reminderHourDraft by remember { mutableStateOf(reminder.hour.toString()) }
    var reminderMinuteDraft by remember { mutableStateOf(reminder.minute.toString().padStart(2, '0')) }
    var showMemories by remember { mutableStateOf(false) }
    var memoryDraft by remember { mutableStateOf("") }
    var mealPhotoDataUrl by remember { mutableStateOf<String?>(null) }
    var isEstimatingNutrition by remember { mutableStateOf(false) }
    var nutritionHint by remember { mutableStateOf<String?>(null) }
    var nutritionAnalysisReady by remember { mutableStateOf(false) }
    var webSearchEnabled by remember { mutableStateOf(false) }
    var showTargetSettings by remember { mutableStateOf(false) }
    var targetCaloriesDraft by remember { mutableStateOf(targets.calories.toString()) }
    var targetProteinDraft by remember { mutableStateOf(targets.protein.toString()) }
    var targetFatDraft by remember { mutableStateOf(targets.fat.toString()) }
    var targetCarbsDraft by remember { mutableStateOf(targets.carbs.toString()) }
    var measurementRangeDays by remember { mutableStateOf(7) }
    var showProfileSettings by remember { mutableStateOf(false) }
    var profileStartWeightDraft by remember { mutableStateOf(healthProfile.startWeight?.toString() ?: "") }
    var profileHeightDraft by remember { mutableStateOf(healthProfile.heightCm?.toString() ?: "") }
    var profileGoalWeightDraft by remember { mutableStateOf(healthProfile.goalWeight?.toString() ?: "") }
    var profileGoalBodyFatDraft by remember { mutableStateOf(healthProfile.goalBodyFat?.toString() ?: "") }
    var standardTitleDraft by remember { mutableStateOf("") }
    var standardCaloriesDraft by remember { mutableStateOf("") }
    var standardProteinDraft by remember { mutableStateOf("") }
    var standardFatDraft by remember { mutableStateOf("") }
    var standardCarbsDraft by remember { mutableStateOf("") }
    var dataStatus by remember { mutableStateOf<String?>(null) }
    var showDeleteAllData by remember { mutableStateOf(false) }
    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            runCatching {
                val export = LocalDataExport.createJson(
                    conversations, activeConversationId, tasks, dailyEntries, routines, weightEntries, photos, reminder,
                    memories, projects, privateFiles, targets, sportSessions, measurements, healthProfile, appStyle
                )
                context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter()?.use { it.write(export) }
                    ?: error("Die Exportdatei konnte nicht geöffnet werden.")
            }.onSuccess {
                dataStatus = "Export gespeichert. Die Datei enthält sensible Gesundheitsdaten."
            }.onFailure {
                dataStatus = it.message ?: "Der Export ist fehlgeschlagen."
            }
        }
    }
    val notificationPermission = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val listState = rememberLazyListState()

    LaunchedEffect(activeConversationId, messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    fun persistConversationMessages(conversationId: String, updatedMessages: List<ChatMessage>) {
        val now = Instant.now().toEpochMilli()
        val next = conversations.map { conversation ->
            if (conversation.id != conversationId) conversation
            else {
                val automaticTitle = if (conversation.title == "Neuer Chat") {
                    updatedMessages.firstOrNull { it.role == "user" }?.content?.trim()?.replace("\n", " ")?.take(42)?.ifBlank { null }
                } else null
                conversation.copy(
                    title = automaticTitle ?: conversation.title,
                    messages = updatedMessages.takeLast(500),
                    updatedAt = now
                )
            }
        }.sortedByDescending { it.updatedAt }
        conversations = next
        storage.write(next)
    }

    fun createConversation(title: String): String {
        val now = Instant.now().toEpochMilli()
        val conversation = ChatConversation("chat-$now", title.trim().ifBlank { "Neuer Chat" }, emptyList(), now)
        val next = listOf(conversation) + conversations
        conversations = next
        activeConversationId = conversation.id
        storage.write(next)
        storage.writeActiveId(conversation.id)
        showConversationBrowser = false
        conversationSearch = ""
        conversationTitleDraft = ""
        draft = ""
        error = null
        return conversation.id
    }

    fun requestNutritionEstimate(description: String, imageDataUrl: String?) {
        if (description.isBlank() && imageDataUrl == null) return
        isEstimatingNutrition = true
        nutritionAnalysisReady = false
        nutritionHint = null
        Thread {
            val result = runCatching { NutritionApi.estimate(description, imageDataUrl, apiSettings) }
            Handler(Looper.getMainLooper()).post {
                isEstimatingNutrition = false
                result.onSuccess { estimate ->
                    logTitle = estimate.name
                    logCalories = estimate.calories.toString()
                    logProtein = "%.1f".format(estimate.protein)
                    logFat = "%.1f".format(estimate.fat)
                    logCarbs = "%.1f".format(estimate.carbs)
                    nutritionAnalysisReady = true
                    nutritionHint = "KI-Schätzung (${estimate.confidence}): ${estimate.note} Bitte vor dem Loggen prüfen."
                }.onFailure { failure -> nutritionHint = failure.message ?: "Die Nährwert-Schätzung konnte nicht geladen werden." }
            }
        }.start()
    }

    fun loadChatDemoWeek() {
        val dates = (6 downTo 0).map { LocalDate.now().minusDays(it.toLong()).toString() }
        val totals = listOf(
            listOf(2187.0, 177.0, 105.0, 123.0),
            listOf(2186.0, 181.0, 57.0, 229.0),
            listOf(2532.0, 189.0, 85.0, 217.0),
            listOf(3049.0, 177.0, 121.0, 216.0),
            listOf(1882.0, 163.0, 85.0, 187.0),
            listOf(2672.0, 184.0, 125.0, 216.0),
            listOf(2374.0, 165.0, 68.0, 207.0)
        )
        val chatMeals = dates.mapIndexed { index, date ->
            val values = totals[index]
            DailyLogEntry(date, "Mahlzeit", "Chat-Testtag ${index + 1}", values[0].toInt(), values[1], values[2], values[3])
        }
        val chatSport = listOf(
            SportSession(dates[0], "Morgensport", 386, null, "Wert aus dem Chat-Export"),
            SportSession(dates[1], "Morgensport", 370, null, "Wert aus dem Chat-Export"),
            SportSession(dates[2], "Fahrrad", 0, null, "45 Minuten Arbeitsweg hin und zurück"),
            SportSession(dates[3], "Morgensport", 367, 2600, "Werte aus dem Chat-Export"),
            SportSession(dates[6], "Morgensport", 540, null, "Wert aus dem Chat-Export")
        )
        val chatSportEntries = chatSport.filter { it.calories > 0 }.map {
            DailyLogEntry(it.date, "Sport", it.activity, it.calories, 0.0, 0.0, 0.0)
        }
        val weights = listOf(90.4, 90.9, 90.4, 89.95, 89.6, 90.0, 89.5)
        val scaleBodyFat = listOf(24.3, 24.7, 24.2, 24.0, 23.2, 23.4, 23.8)
        val hunger = listOf(null, 3, null, 1, 1, 1, 3)
        val energy = listOf(null, 8, 7, 6, 7, 7, 6)
        val chatMeasurements = dates.mapIndexed { index, date ->
            BodyMeasurement(
                date = date,
                weight = weights[index],
                scaleBodyFat = scaleBodyFat[index],
                neck = if (index == 0) 37.0 else if (index == 6) 38.0 else null,
                abdomen = if (index == 0) 94.0 else if (index == 6) 95.0 else null,
                hunger = hunger[index],
                energy = energy[index]
            )
        }
        val demoDates = dates.toSet()
        dailyEntries = (dailyEntries.filterNot { it.date in demoDates } + chatMeals + chatSportEntries).sortedBy { it.date }
        sportSessions = (sportSessions.filterNot { it.date in demoDates } + chatSport).sortedBy { it.date }
        measurements = (measurements.filterNot { it.date in demoDates } + chatMeasurements).sortedBy { it.date }
        weightEntries = (weightEntries.filterNot { it.date in demoDates } + dates.mapIndexed { index, date -> WeightEntry(date, weights[index]) }).sortedBy { it.date }
        targets = NutritionTargets(2484, 180.0, 70.0, 290.0)
        healthProfile = HealthProfile(90.4, 194.0, null, null)
        val breakfast = DailyRoutine("Standardfrühstück + 75 g Hafer", 600, 48.5, 17.5, 61.8)
        routines = (routines.filterNot { it.title == breakfast.title } + breakfast).takeLast(50)
        dailyLogStorage.write(dailyEntries)
        sportStorage.write(sportSessions)
        measurementStorage.write(measurements)
        weightStorage.write(weightEntries)
        targetsStorage.write(targets)
        healthProfileStorage.write(healthProfile)
        routineStorage.write(routines)
        selectedDate = todayKey()
        workspace = Workspace.DAILY_LOG
    }

    MaterialTheme(colorScheme = kf20Colors(appStyle)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.kf20_wordmark),
                                contentDescription = "KF20",
                                modifier = Modifier.width(112.dp).height(48.dp).clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Dein täglicher Agent", style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    actions = {
                        if (workspace == Workspace.CHAT && !showConversationBrowser) TextButton(onClick = { showDeleteDialog = true }, enabled = messages.isNotEmpty() && !isSending) { Text("Leeren") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(selected = workspace == Workspace.DAILY_LOG, onClick = { workspace = Workspace.DAILY_LOG }, icon = { Icon(Icons.Filled.Today, contentDescription = null) }, label = { Text("Tag") })
                    NavigationBarItem(selected = workspace == Workspace.STATISTICS || workspace == Workspace.PROGRESS || workspace == Workspace.PHOTOS, onClick = { workspace = Workspace.STATISTICS }, icon = { Icon(Icons.Filled.BarChart, contentDescription = null) }, label = { Text("Statistik") })
                    NavigationBarItem(selected = workspace == Workspace.CHAT, onClick = { workspace = Workspace.CHAT }, icon = { Icon(Icons.Filled.Chat, contentDescription = null) }, label = { Text("Chat") })
                    NavigationBarItem(selected = workspace == Workspace.STANDARDS, onClick = { workspace = Workspace.STANDARDS }, icon = { Icon(Icons.Filled.Star, contentDescription = null) }, label = { Text("Einstellungen") })
                }
            }
        ) { padding ->
            if (workspace == Workspace.CHAT && showConversationBrowser) ConversationBrowser(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                conversations = conversations,
                activeConversationId = activeConversationId,
                search = conversationSearch,
                titleDraft = conversationTitleDraft,
                onSearchChange = { conversationSearch = it },
                onTitleChange = { conversationTitleDraft = it.take(60) },
                onCreate = { createConversation(conversationTitleDraft) },
                onOpen = { conversationId ->
                    activeConversationId = conversationId
                    storage.writeActiveId(conversationId)
                    showConversationBrowser = false
                    conversationSearch = ""
                    draft = ""
                    error = null
                },
                onDelete = { pendingConversationDeleteId = it },
                onBack = { showConversationBrowser = false }
            ) else if (workspace == Workspace.CHAT) Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(conversations.firstOrNull { it.id == activeConversationId }?.title ?: "Hauptchat", fontWeight = FontWeight.Bold)
                            Text("${messages.size} Nachrichten · verschlüsselt auf diesem Gerät", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { showConversationBrowser = true }, enabled = !isSending) { Text("Gespräche") }
                        TextButton(onClick = { createConversation("Neuer Chat") }, enabled = !isSending) { Text("+ Neu") }
                    }
                }
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
                            val conversationId = activeConversationId
                            draft = ""
                            error = null
                            val updated = messages + ChatMessage("user", input)
                            persistConversationMessages(conversationId, updated)
                            isSending = true
                            Thread {
                                val result = runCatching { Kf20Api.send(updated.takeLast(30), apiSettings, memories.map { it.text }, webSearchEnabled) }
                                Handler(Looper.getMainLooper()).post {
                                    isSending = false
                                    result.onSuccess { reply ->
                                        val currentMessages = conversations.firstOrNull { it.id == conversationId }?.messages.orEmpty()
                                        persistConversationMessages(conversationId, currentMessages + ChatMessage("assistant", reply))
                                    }.onFailure { failure -> error = failure.message ?: "Die Antwort konnte nicht geladen werden." }
                                }
                            }.start()
                        },
                        enabled = !isSending && draft.isNotBlank(),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Senden") }
                }
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = { webSearchEnabled = !webSearchEnabled }) { Text(if (webSearchEnabled) "Recherche an" else "Recherche aus") }
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
            ) else if (workspace == Workspace.STATISTICS) StatisticsScreen(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                dailyEntries = dailyEntries,
                sessions = sportSessions,
                measurements = measurements,
                targets = targets,
                profile = healthProfile,
                rangeDays = measurementRangeDays,
                onRangeChange = { measurementRangeDays = it },
                onOpenPhotos = { workspace = Workspace.PHOTOS }
            ) else if (workspace == Workspace.STANDARDS) StandardsScreen(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                routines = routines,
                targets = targets,
                profile = healthProfile,
                reminder = reminder,
                appStyle = appStyle,
                onAppStyleChange = { style ->
                    appStyle = style
                    uiPreferencesStorage.write(style)
                },
                title = standardTitleDraft,
                calories = standardCaloriesDraft,
                protein = standardProteinDraft,
                fat = standardFatDraft,
                carbs = standardCarbsDraft,
                onTitleChange = { standardTitleDraft = it },
                onCaloriesChange = { standardCaloriesDraft = it.filter(Char::isDigit).take(4) },
                onProteinChange = { standardProteinDraft = decimalInput(it) },
                onFatChange = { standardFatDraft = decimalInput(it) },
                onCarbsChange = { standardCarbsDraft = decimalInput(it) },
                onAdd = {
                    val title = standardTitleDraft.trim()
                    if (title.isNotEmpty()) {
                        val routine = DailyRoutine(title, standardCaloriesDraft.toIntOrNull() ?: 0, standardProteinDraft.toLocalizedDouble() ?: 0.0, standardFatDraft.toLocalizedDouble() ?: 0.0, standardCarbsDraft.toLocalizedDouble() ?: 0.0)
                        routines = (routines.filterNot { it.title.equals(title, true) } + routine).takeLast(50)
                        routineStorage.write(routines)
                        standardTitleDraft = ""; standardCaloriesDraft = ""; standardProteinDraft = ""; standardFatDraft = ""; standardCarbsDraft = ""
                    }
                },
                onDelete = { index -> routines = routines.filterIndexed { current, _ -> current != index }; routineStorage.write(routines) },
                onTargetSettings = { showTargetSettings = true },
                onProfileSettings = { showProfileSettings = true },
                onReminderSettings = { showReminderSettings = true },
                onOpenTasks = { workspace = Workspace.TASKS },
                onOpenFiles = { workspace = Workspace.FILES },
                onExportData = { exportLauncher.launch("KF20-Export-${todayKey()}.json") },
                onDeleteAllData = { showDeleteAllData = true },
                dataStatus = dataStatus,
                onLoadChatDemoData = { loadChatDemoWeek() }
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
                sportSessions = sportSessions.filter { it.date == selectedDate },
                measurements = measurements.filter { it.date == selectedDate },
                measurementDefaults = measurements.lastOrNull { it.date <= selectedDate } ?: measurements.lastOrNull(),
                startWeight = healthProfile.startWeight,
                routines = routines,
                targets = targets,
                selectedDate = selectedDate,
                description = mealDescription,
                title = logTitle,
                calories = logCalories,
                protein = logProtein,
                fat = logFat,
                carbs = logCarbs,
                onDescriptionChange = {
                    mealDescription = it
                    nutritionAnalysisReady = false
                    nutritionHint = null
                    mealPhotoDataUrl = null
                },
                onTitleChange = { logTitle = it },
                onCaloriesChange = { logCalories = it },
                onProteinChange = { logProtein = it },
                onFatChange = { logFat = it },
                onCarbsChange = { logCarbs = it },
                hasPhoto = mealPhotoDataUrl != null,
                isEstimating = isEstimatingNutrition,
                analysisReady = nutritionAnalysisReady,
                nutritionHint = nutritionHint,
                onPhotoCaptured = { dataUrl -> mealPhotoDataUrl = dataUrl; requestNutritionEstimate(mealDescription.trim(), dataUrl) },
                onVoiceCaptured = { description -> mealPhotoDataUrl = null; mealDescription = description; requestNutritionEstimate(description, null) },
                onEstimate = { requestNutritionEstimate(mealDescription.trim(), mealPhotoDataUrl) },
                onAddSport = { activity, trainingCalories, trackerCalories ->
                    val session = SportSession(selectedDate, activity, trainingCalories, trackerCalories, "")
                    sportSessions = (sportSessions.filterNot { it.date == selectedDate } + session).takeLast(500)
                    sportStorage.write(sportSessions)
                    dailyEntries = dailyEntries.filterNot { it.date == selectedDate && it.type == "Sport" }
                    if (trainingCalories > 0) {
                        dailyEntries = (dailyEntries + DailyLogEntry(selectedDate, "Sport", activity, trainingCalories, 0.0, 0.0, 0.0)).takeLast(500)
                    }
                    dailyLogStorage.write(dailyEntries)
                },
                onSaveDayClose = { trackerCalories, note ->
                    val index = sportSessions.indexOfLast { it.date == selectedDate }
                    sportSessions = if (index >= 0) {
                        sportSessions.toMutableList().apply {
                            this[index] = this[index].copy(trackerCalories = trackerCalories, note = note)
                        }
                    } else {
                        sportSessions + SportSession(selectedDate, "Tagesabschluss", 0, trackerCalories, note)
                    }
                    sportStorage.write(sportSessions)
                },
                onAddMeasurement = { entry ->
                    val existing = measurements.lastOrNull { it.date == selectedDate }
                    val merged = entry.copy(
                        weight = entry.weight ?: existing?.weight,
                        scaleBodyFat = entry.scaleBodyFat ?: existing?.scaleBodyFat,
                        neck = entry.neck ?: existing?.neck,
                        abdomen = entry.abdomen ?: existing?.abdomen,
                        hunger = entry.hunger ?: existing?.hunger,
                        energy = entry.energy ?: existing?.energy
                    )
                    measurements = (measurements.filterNot { it.date == selectedDate } + merged).sortedBy { it.date }.takeLast(1000)
                    measurementStorage.write(measurements)
                    merged.weight?.let { kilograms ->
                        weightEntries = (weightEntries.filterNot { it.date == selectedDate } + WeightEntry(selectedDate, kilograms)).sortedBy { it.date }
                        weightStorage.write(weightEntries)
                    }
                },
                onPreviousDay = { selectedDate = LocalDate.parse(selectedDate).minusDays(1).toString() },
                onToday = { selectedDate = todayKey() },
                onNextDay = { selectedDate = LocalDate.parse(selectedDate).plusDays(1).toString() },
                onUseRoutine = { routine, planned ->
                    val entry = DailyLogEntry(selectedDate, "Mahlzeit", routine.title, routine.calories, routine.protein, routine.fat, routine.carbs, planned)
                    dailyEntries = dailyEntries + entry
                    dailyLogStorage.write(dailyEntries)
                },
                onSaveRoutine = {
                    val title = logTitle.trim()
                    if (title.isNotEmpty()) {
                        val routine = DailyRoutine(title, logCalories.toIntOrNull() ?: 0, logProtein.replace(',', '.').toDoubleOrNull() ?: 0.0, logFat.replace(',', '.').toDoubleOrNull() ?: 0.0, logCarbs.replace(',', '.').toDoubleOrNull() ?: 0.0)
                        routines = (routines.filterNot { it.title.equals(routine.title, ignoreCase = true) } + routine).takeLast(20)
                        routineStorage.write(routines)
                    }
                },
                onDeleteEntry = { entry ->
                    val index = dailyEntries.indexOfLast { it == entry }
                    if (index >= 0) {
                        dailyEntries = dailyEntries.toMutableList().apply { removeAt(index) }
                        dailyLogStorage.write(dailyEntries)
                    }
                },
                onMarkConsumed = { entry ->
                    val index = dailyEntries.indexOfLast { it == entry }
                    if (index >= 0) {
                        dailyEntries = dailyEntries.toMutableList().apply { this[index] = entry.copy(planned = false) }
                        dailyLogStorage.write(dailyEntries)
                    }
                },
                onDeleteSport = { session ->
                    val sportIndex = sportSessions.indexOfLast { it == session }
                    if (sportIndex >= 0) {
                        sportSessions = sportSessions.toMutableList().apply { removeAt(sportIndex) }
                        sportStorage.write(sportSessions)
                    }
                    val logIndex = dailyEntries.indexOfLast {
                        it.date == session.date && it.type == "Sport" && it.title == session.activity && it.calories == session.calories
                    }
                    if (logIndex >= 0) {
                        dailyEntries = dailyEntries.toMutableList().apply { removeAt(logIndex) }
                        dailyLogStorage.write(dailyEntries)
                    }
                },
                onDeleteMeasurement = { measurement ->
                    measurements = measurements.filterNot { it == measurement }
                    measurementStorage.write(measurements)
                    measurement.weight?.let { weight ->
                        weightEntries = weightEntries.filterNot { it.date == measurement.date && it.kilograms == weight }
                        weightStorage.write(weightEntries)
                    }
                },
                onAdd = { planned ->
                    val title = logTitle.trim()
                    if (title.isNotEmpty() && nutritionAnalysisReady) {
                        val entry = DailyLogEntry(selectedDate, "Mahlzeit", title, logCalories.toIntOrNull() ?: 0, logProtein.replace(',', '.').toDoubleOrNull() ?: 0.0, logFat.replace(',', '.').toDoubleOrNull() ?: 0.0, logCarbs.replace(',', '.').toDoubleOrNull() ?: 0.0, planned)
                        dailyEntries = dailyEntries + entry
                        dailyLogStorage.write(dailyEntries)
                        mealDescription = ""; logTitle = ""; logCalories = ""; logProtein = ""; logFat = ""; logCarbs = ""; mealPhotoDataUrl = null; nutritionHint = null; nutritionAnalysisReady = false
                    }
                }
            )
        }
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Verlauf löschen?") },
                text = { Text("Nur die Nachrichten im aktuell geöffneten Gespräch werden entfernt. Andere Gespräche bleiben erhalten.") },
                confirmButton = {
                    TextButton(onClick = {
                        persistConversationMessages(activeConversationId, emptyList())
                        showDeleteDialog = false
                    }) { Text("Endgültig löschen") }
                },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Abbrechen") } }
            )
        }
        pendingConversationDeleteId?.let { conversationId ->
            val title = conversations.firstOrNull { it.id == conversationId }?.title ?: "Gespräch"
            AlertDialog(
                onDismissRequest = { pendingConversationDeleteId = null },
                title = { Text("Gespräch löschen?") },
                text = { Text("„$title“ und alle darin gespeicherten Nachrichten werden endgültig von diesem Gerät entfernt.") },
                confirmButton = {
                    TextButton(onClick = {
                        val remaining = conversations.filterNot { it.id == conversationId }
                        if (remaining.isNotEmpty()) {
                            conversations = remaining
                            storage.write(remaining)
                            if (activeConversationId == conversationId) {
                                activeConversationId = remaining.first().id
                                storage.writeActiveId(remaining.first().id)
                            }
                        }
                        pendingConversationDeleteId = null
                    }) { Text("Endgültig löschen") }
                },
                dismissButton = { TextButton(onClick = { pendingConversationDeleteId = null }) { Text("Abbrechen") } }
            )
        }
        if (showDeleteAllData) {
            AlertDialog(
                onDismissRequest = { showDeleteAllData = false },
                title = { Text("Alle lokalen Daten löschen?") },
                text = { Text("Tageslogs, Messwerte, Standards, Fotos, Dateien, Aufgaben, Erinnerungen, Chatverlauf und Serverzugang werden unwiderruflich von diesem Gerät entfernt. Exportiere vorher eine Kopie, falls du die Daten behalten möchtest.") },
                confirmButton = {
                    TextButton(onClick = {
                        (photos.map { it.uri } + privateFiles.map { it.uri }).distinct().forEach { value ->
                            runCatching { context.contentResolver.releasePersistableUriPermission(Uri.parse(value), Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                        }
                        DailyReminder.cancel(context)
                        Kf20LocalData.clear(context)
                        val freshConversation = ChatConversation("chat-${Instant.now().toEpochMilli()}", "Hauptchat", emptyList(), Instant.now().toEpochMilli())
                        conversations = listOf(freshConversation); activeConversationId = freshConversation.id
                        showConversationBrowser = false; conversationSearch = ""; conversationTitleDraft = ""
                        tasks = emptyList(); dailyEntries = emptyList(); routines = emptyList()
                        weightEntries = emptyList(); photos = emptyList(); memories = emptyList(); projects = emptyList(); privateFiles = emptyList()
                        sportSessions = emptyList(); measurements = emptyList(); apiSettings = ApiSettings("", "")
                        reminder = ReminderConfig(false, 20, 0); targets = NutritionTargets(2_000, 150.0, 70.0, 200.0)
                        healthProfile = HealthProfile(null, null, null, null)
                        appStyle = AppStyle.PERFORMANCE_DARK
                        serverUrlDraft = ""; serverTokenDraft = ""; selectedDate = todayKey()
                        dataStatus = "Alle lokalen KF20-Daten wurden gelöscht."
                        showDeleteAllData = false
                    }) { Text("Endgültig löschen") }
                },
                dismissButton = { TextButton(onClick = { showDeleteAllData = false }) { Text("Abbrechen") } }
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
        if (showProfileSettings) {
            AlertDialog(
                onDismissRequest = { showProfileSettings = false },
                title = { Text("Startwerte und Ziele") },
                text = {
                    Column {
                        Text("Die vorhandenen Startwerte stammen aus deinem Chat-Verlauf. Zielwerte bleiben leer, bis du sie selbst festlegst.")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = profileStartWeightDraft, onValueChange = { profileStartWeightDraft = decimalInput(it) }, label = { Text("Startgewicht kg") }, modifier = Modifier.weight(1f), singleLine = true)
                            OutlinedTextField(value = profileHeightDraft, onValueChange = { profileHeightDraft = decimalInput(it) }, label = { Text("Größe cm") }, modifier = Modifier.weight(1f), singleLine = true)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = profileGoalWeightDraft, onValueChange = { profileGoalWeightDraft = decimalInput(it) }, label = { Text("Zielgewicht optional") }, modifier = Modifier.weight(1f), singleLine = true)
                            OutlinedTextField(value = profileGoalBodyFatDraft, onValueChange = { profileGoalBodyFatDraft = decimalInput(it) }, label = { Text("Ziel-KF optional") }, modifier = Modifier.weight(1f), singleLine = true)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val start = profileStartWeightDraft.toLocalizedDouble()
                        val height = profileHeightDraft.toLocalizedDouble()
                        if (start != null && height != null && start in 25.0..500.0 && height in 120.0..230.0) {
                            healthProfile = HealthProfile(start, height, profileGoalWeightDraft.toLocalizedDouble(), profileGoalBodyFatDraft.toLocalizedDouble())
                            healthProfileStorage.write(healthProfile)
                            showProfileSettings = false
                        }
                    }) { Text("Speichern") }
                },
                dismissButton = { TextButton(onClick = { showProfileSettings = false }) { Text("Abbrechen") } }
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

@Composable private fun ConversationBrowser(
    modifier: Modifier,
    conversations: List<ChatConversation>,
    activeConversationId: String,
    search: String,
    titleDraft: String,
    onSearchChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onCreate: () -> Unit,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
    onBack: () -> Unit
) {
    val query = search.trim()
    val filtered = conversations.filter { conversation ->
        query.isBlank() || conversation.title.contains(query, ignoreCase = true) ||
            conversation.messages.any { it.content.contains(query, ignoreCase = true) }
    }.sortedByDescending { it.updatedAt }
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Gespräche", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Benannte, lokal verschlüsselte Verläufe", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onBack) { Text("Zurück") }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Neues Gespräch", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = titleDraft,
                        onValueChange = onTitleChange,
                        label = { Text("Titel, z. B. Wochenplanung") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(onClick = onCreate, modifier = Modifier.fillMaxWidth()) { Text("Gespräch anlegen") }
                }
            }
        }
        item {
            OutlinedTextField(
                value = search,
                onValueChange = onSearchChange,
                label = { Text("Alle Gespräche durchsuchen") },
                supportingText = { Text("Sucht in Titeln und Nachrichten auf diesem Gerät") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        if (filtered.isEmpty()) item {
            Text("Keine passende Nachricht gefunden.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 20.dp))
        }
        items(filtered) { conversation ->
            val match = if (query.isBlank()) conversation.messages.lastOrNull() else conversation.messages.lastOrNull { it.content.contains(query, ignoreCase = true) }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (conversation.id == activeConversationId) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (conversation.id == activeConversationId) "✓ ${conversation.title}" else conversation.title, fontWeight = FontWeight.Bold)
                            Text("${conversation.messages.size} Nachrichten", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { onOpen(conversation.id) }) { Text("Öffnen") }
                    }
                    match?.content?.replace("\n", " ")?.take(120)?.let { preview ->
                        Text(preview, maxLines = 2, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (conversations.size > 1) TextButton(onClick = { onDelete(conversation.id) }) { Text("Gespräch löschen") }
                }
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
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

@Composable private fun StatisticsScreen(
    modifier: Modifier,
    dailyEntries: List<DailyLogEntry>,
    sessions: List<SportSession>,
    measurements: List<BodyMeasurement>,
    targets: NutritionTargets,
    profile: HealthProfile,
    rangeDays: Int,
    onRangeChange: (Int) -> Unit,
    onOpenPhotos: () -> Unit
) {
    var useRollingAverage by remember { mutableStateOf(false) }
    val cutoff = LocalDate.now().minusDays(rangeDays.toLong() - 1)
    val inRange: (String) -> Boolean = { date -> runCatching { !LocalDate.parse(date).isBefore(cutoff) }.getOrDefault(false) }
    val todayEntries = dailyEntries.filter { it.date == todayKey() }
    val todayFoods = todayEntries.filter { it.type == "Mahlzeit" && !it.planned }
    val todayIntake = todayFoods.sumOf { it.calories }
    val todayBurned = todayEntries.filter { it.type == "Sport" }.sumOf { it.calories }
    val todayProtein = todayFoods.sumOf { it.protein }
    val todayFat = todayFoods.sumOf { it.fat }
    val todayCarbs = todayFoods.sumOf { it.carbs }
    val dates = (rangeDays - 1 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }
    val calculationDates = (rangeDays + 5 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }
    fun displayed(values: List<Double?>): List<Double?> = (if (useRollingAverage) rollingAverage(values) else values).takeLast(rangeDays)
    val rawCalories = calculationDates.map { date ->
        val day = dailyEntries.filter { it.date == date.toString() }
        val hasFood = day.any { it.type == "Mahlzeit" && !it.planned }
        val intake = day.filter { it.type == "Mahlzeit" && !it.planned }.sumOf { it.calories }
        val sport = day.filter { it.type == "Sport" }.sumOf { it.calories }
        if (hasFood) maxOf(0, intake - sport).toDouble() else null
    }
    fun rawMacro(selector: (DailyLogEntry) -> Double): List<Double?> = calculationDates.map { date ->
        val food = dailyEntries.filter { it.date == date.toString() && it.type == "Mahlzeit" && !it.planned }
        if (food.isEmpty()) null else food.sumOf(selector)
    }
    val dailyCalories = displayed(rawCalories).map { it ?: 0.0 }
    val dailyProtein = displayed(rawMacro { it.protein }).map { it ?: 0.0 }
    val dailyFat = displayed(rawMacro { it.fat }).map { it ?: 0.0 }
    val dailyCarbs = displayed(rawMacro { it.carbs }).map { it ?: 0.0 }
    val rangedMeasurements = measurements.filter { inRange(it.date) }
    fun measurementSeries(selector: (BodyMeasurement) -> Double?): List<Pair<String, Double>> = displayed(
        calculationDates.map { date -> measurements.lastOrNull { it.date == date.toString() }?.let(selector) }
    ).mapIndexedNotNull { index, value -> value?.let { dates[index].toString() to it } }
    val weights = measurementSeries { it.weight }
    val scaleKf = measurementSeries { it.scaleBodyFat }
    val navyKf = measurementSeries { navyBodyFat(it.neck, it.abdomen, profile.heightCm) }
    val rangedSessions = sessions.filter { inRange(it.date) }
    val trainingCalories = rangedSessions.sumOf { it.calories }
    val trackerReadings = rangedSessions.mapNotNull { it.trackerCalories }
    val latest = measurements.lastOrNull()
    val sportByDay = displayed(calculationDates.map { date -> sessions.filter { it.date == date.toString() }.sumOf { it.calories }.toDouble() }).map { it ?: 0.0 }
    val hungerValues = displayed(calculationDates.map { date -> measurements.lastOrNull { it.date == date.toString() }?.hunger?.toDouble() })
    val energyValues = displayed(calculationDates.map { date -> measurements.lastOrNull { it.date == date.toString() }?.energy?.toDouble() })
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Statistik", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(7, 14, 30).forEach { days ->
                    if (rangeDays == days) Button(onClick = { onRangeChange(days) }, modifier = Modifier.weight(1f)) { Text("$days Tage") }
                    else TextButton(onClick = { onRangeChange(days) }, modifier = Modifier.weight(1f)) { Text("$days Tage") }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!useRollingAverage) Button(onClick = { useRollingAverage = false }, modifier = Modifier.weight(1f)) { Text("Tageswerte") }
                else OutlinedButton(onClick = { useRollingAverage = false }, modifier = Modifier.weight(1f)) { Text("Tageswerte") }
                if (useRollingAverage) Button(onClick = { useRollingAverage = true }, modifier = Modifier.weight(1f)) { Text("7-Tage-Ø") }
                else OutlinedButton(onClick = { useRollingAverage = true }, modifier = Modifier.weight(1f)) { Text("7-Tage-Ø") }
            }
        }
        item { DailyGoalHero(intake = todayIntake, burned = todayBurned, target = targets.calories) }
        item {
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    MacroRing("Protein", todayProtein, targets.protein, "g", ProteinBlue, Modifier.weight(1f))
                    MacroRing("Fett", todayFat, targets.fat, "g", FatAmber, Modifier.weight(1f))
                    MacroRing("Carbs", todayCarbs, targets.carbs, "g", CarbsCoral, Modifier.weight(1f))
                }
            }
        }
        item {
            ChartCard(title = "Kalorienverlauf", subtitle = if (useRollingAverage) "Rollierender 7-Tage-Ø · Linie = Tagesziel" else "Netto pro Tag · Linie = Tagesziel") {
                VerticalBarChart(dailyCalories, targets.calories.toDouble(), CaloriesGreen)
                RangeDayLabels(dates)
            }
        }
        item {
            ChartCard(title = "Makro-Zielerreichung", subtitle = if (useRollingAverage) "Rollierender 7-Tage-Ø als Anteil des Tagesziels" else "Protein, Fett und Carbs als Anteil des Tagesziels") {
                NormalizedMacroChart(dailyProtein, dailyFat, dailyCarbs, targets)
                ChartLegend(listOf("Protein" to ProteinBlue, "Fett" to FatAmber, "Carbs" to CarbsCoral))
            }
        }
        item {
            Text("Gewicht", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(latest?.weight?.let { "${it.de1()} kg" } ?: "Noch kein Wert", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        profile.goalWeight?.let { Text("Ziel ${it.de1()} kg", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    ValueChart(weights, color = ProteinBlue, target = profile.goalWeight)
                    if (weights.size < 2) Text("Mindestens zwei Werte ergeben eine Verlaufskurve.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Text("Körperfett", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Waage${latest?.scaleBodyFat?.let { ": ${it.de1()} %" } ?: ""}", color = CarbsCoral, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("Navy${navyBodyFat(latest?.neck, latest?.abdomen, profile.heightCm)?.let { ": ${it.de1()} %" } ?: ""}", color = HungerPurple, fontWeight = FontWeight.Bold)
                    }
                    DualValueChart(scaleKf, navyKf, CarbsCoral, HungerPurple, profile.goalBodyFat)
                    ChartLegend(listOf("Waage" to CarbsCoral, "Navy" to HungerPurple))
                }
            }
        }
        item {
            Text("Sport & Tracker", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            ChartCard(title = "Trainingsaktivität", subtitle = if (useRollingAverage) "Rollierender 7-Tage-Ø der Trainingskalorien" else "Verbrauchte Trainingskalorien pro Tag") {
                VerticalBarChart(sportByDay, maxOf(1.0, sportByDay.average().takeIf { !it.isNaN() } ?: 1.0), EnergyTeal)
                RangeDayLabels(dates)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Training", "$trainingCalories kcal", Modifier.weight(1f))
                StatCard("Trainingstage", "${rangedSessions.filter { it.calories > 0 }.map { it.date }.distinct().size}", Modifier.weight(1f))
                StatCard("Tracker Ø", trackerReadings.takeIf { it.isNotEmpty() }?.average()?.toInt()?.let { "$it kcal" } ?: "–", Modifier.weight(1f))
            }
        }
        item {
            val latestHunger = measurements.lastOrNull { it.hunger != null }?.hunger
            val latestEnergy = measurements.lastOrNull { it.energy != null }?.energy
            ChartCard(title = "Hunger & Energie", subtitle = if (useRollingAverage) "Rollierender 7-Tage-Ø auf der Skala von 0 bis 10" else "Persönlicher Verlauf auf der Skala von 0 bis 10") {
                ScoreTrendChart(hungerValues, energyValues)
                ChartLegend(listOf("Hunger" to HungerPurple, "Energie" to EnergyTeal))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Hunger", latestHunger?.let { "$it / 10" } ?: "–", Modifier.weight(1f))
                MetricCard("Energie", latestEnergy?.let { "$it / 10" } ?: "–", Modifier.weight(1f))
            }
            TextButton(onClick = onOpenPhotos, modifier = Modifier.fillMaxWidth()) { Text("Fortschrittsfotos ansehen") }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable private fun StandardsScreen(
    modifier: Modifier,
    routines: List<DailyRoutine>,
    targets: NutritionTargets,
    profile: HealthProfile,
    reminder: ReminderConfig,
    appStyle: AppStyle,
    onAppStyleChange: (AppStyle) -> Unit,
    title: String,
    calories: String,
    protein: String,
    fat: String,
    carbs: String,
    onTitleChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onCarbsChange: (String) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Int) -> Unit,
    onTargetSettings: () -> Unit,
    onProfileSettings: () -> Unit,
    onReminderSettings: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenFiles: () -> Unit,
    onExportData: () -> Unit,
    onDeleteAllData: () -> Unit,
    dataStatus: String?,
    onLoadChatDemoData: () -> Unit
) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Einstellungen", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp))
            Text("Wiederkehrende Mahlzeiten und persönliche Einstellungen.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Design", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Wähle den Styleguide für die gesamte App. Die Einstellung wird auf diesem Gerät gespeichert.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    AppStyle.entries.forEach { style ->
                        if (style == appStyle) {
                            Button(onClick = { onAppStyleChange(style) }, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("✓ ${style.label}", fontWeight = FontWeight.Bold)
                                    Text(style.description, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        } else {
                            OutlinedButton(onClick = { onAppStyleChange(style) }, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(style.label, fontWeight = FontWeight.Bold)
                                    Text(style.description, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tagesziele", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${targets.calories} kcal · Protein ${targets.protein.de1()} g · Fett ${targets.fat.de1()} g · Carbs ${targets.carbs.de1()} g")
                    TextButton(onClick = onTargetSettings) { Text("Ziele anpassen") }
                }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Startwerte & Profil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Start ${profile.startWeight?.let { "${it.de1()} kg" } ?: "nicht gesetzt"} · Größe ${profile.heightCm?.let { "${it.de1()} cm" } ?: "nicht gesetzt"}")
                    val goals = listOfNotNull(profile.goalWeight?.let { "Zielgewicht ${it.de1()} kg" }, profile.goalBodyFat?.let { "Ziel-KF ${it.de1()} %" })
                    Text(if (goals.isEmpty()) "Noch keine Gewichts-/KF-Ziele gesetzt." else goals.joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = onProfileSettings) { Text("Startwerte und Ziele bearbeiten") }
                }
            }
        }
        item {
            Text("Mahlzeiten-Standards", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Lege zum Beispiel dein Standardfrühstück einmal an und übernimm es im Tageslog mit einem Tipp.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(value = title, onValueChange = onTitleChange, label = { Text("Name, z. B. Standardfrühstück") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = calories, onValueChange = onCaloriesChange, label = { Text("Kalorien") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = protein, onValueChange = onProteinChange, label = { Text("Protein g") }, modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = fat, onValueChange = onFatChange, label = { Text("Fett g") }, modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = carbs, onValueChange = onCarbsChange, label = { Text("Carbs g") }, modifier = Modifier.weight(1f), singleLine = true)
            }
            Button(onClick = onAdd, enabled = title.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Standard speichern") }
        }
        if (routines.isEmpty()) item { Text("Noch kein Mahlzeiten-Standard angelegt.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(routines.indices.toList()) { index ->
            val routine = routines[index]
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(routine.title, fontWeight = FontWeight.Bold)
                        Text("${routine.calories} kcal · P ${routine.protein.de1()} · F ${routine.fat.de1()} · C ${routine.carbs.de1()} g", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = { onDelete(index) }) { Text("Entfernen") }
                }
            }
        }
        if (BuildConfig.DEBUG) item {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Testdaten aus dem Chat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Lädt die belegte 7-Tage-Reihe, Messwerte, Sportwerte und ein Standardfrühstück. Die Originaldaten werden für die Diagrammprüfung auf die letzten sieben Tage gelegt.", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Button(onClick = onLoadChatDemoData, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Chat-Testwoche laden") }
                }
            }
        }
        item {
            Text("Weitere Einstellungen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = onReminderSettings) { Text(if (reminder.enabled) "Tägliche Erinnerung: ${reminder.hour}:${reminder.minute.toString().padStart(2, '0')}" else "Tägliche Erinnerung einrichten") }
            Row {
                TextButton(onClick = onOpenTasks) { Text("Aufgaben") }
                TextButton(onClick = onOpenFiles) { Text("Private Dateien") }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Datenschutz & Daten", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Exportiere deine lokalen Daten oder entferne sie vollständig von diesem Gerät.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = onExportData, modifier = Modifier.fillMaxWidth()) { Text("Lokale Daten exportieren") }
                    OutlinedButton(onClick = onDeleteAllData, modifier = Modifier.fillMaxWidth()) { Text("Alle lokalen Daten löschen") }
                    Text("Der Export ist eine unverschlüsselte JSON-Datei mit sensiblen Gesundheitsdaten. Server-Token und Provider-Schlüssel werden nicht exportiert.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    dataStatus?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable private fun SportScreen(
    modifier: Modifier,
    sessions: List<SportSession>,
    activity: String,
    calories: String,
    trackerCalories: String,
    note: String,
    onActivityChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onTrackerCaloriesChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Int) -> Unit
) {
    val today = LocalDate.now()
    val weekStart = today.minusDays((today.dayOfWeek.value - 1).toLong())
    val thisWeek = sessions.filter { runCatching { !LocalDate.parse(it.date).isBefore(weekStart) }.getOrDefault(false) }
    val todaySessions = sessions.filter { it.date == todayKey() }
    val todayTrainingCalories = todaySessions.sumOf { it.calories }
    val todayTrackerCalories = todaySessions.lastOrNull { it.trackerCalories != null }?.trackerCalories
    val weekTrainingCalories = thisWeek.sumOf { it.calories }
    val trainingDays = thisWeek.filter { it.calories > 0 }.map { it.date }.distinct().size
    val trackerValues = thisWeek.mapNotNull { it.trackerCalories }
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Sport", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp))
            Text("Trainingsverbrauch und Tagesverbrauch aus deinem Tracker.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Heute", color = MaterialTheme.colorScheme.onPrimary)
                    Text("$todayTrainingCalories kcal Training", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(todayTrackerCalories?.let { "$it kcal Tagesverbrauch laut Tracker" } ?: "Tagesverbrauch noch nicht gemeldet", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Sportdaten eintragen", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Morgendliche Kalorienwerte zählen als Trainingsverbrauch.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SportActivitySelector(selected = activity, onSelect = onActivityChange)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = calories, onValueChange = onCaloriesChange, label = { Text("Training kcal") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = trackerCalories, onValueChange = onTrackerCaloriesChange, label = { Text("Tracker gesamt kcal") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    OutlinedTextField(value = note, onValueChange = onNoteChange, label = { Text("Notiz, z. B. Hinweg – Rückweg folgt") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Button(onClick = onAdd, enabled = calories.toIntOrNull() != null || trackerCalories.toIntOrNull() != null || activity == "Kein Training", modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Sportdaten speichern") }
                }
            }
        }
        item {
            Text("Diese Woche", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Training", "$weekTrainingCalories kcal", Modifier.weight(1f))
                StatCard("Trainingstage", "$trainingDays", Modifier.weight(1f))
                StatCard("Tracker Ø", trackerValues.takeIf { it.isNotEmpty() }?.average()?.toInt()?.let { "$it kcal" } ?: "–", Modifier.weight(1f))
            }
        }
        item { Text("Letzte Aktivitäten", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        if (sessions.isEmpty()) item { Text("Noch keine Einheit gespeichert.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(sessions.indices.reversed().toList()) { index ->
            val session = sessions[index]
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(session.activity, fontWeight = FontWeight.Bold)
                        val details = buildList {
                            if (session.calories > 0) add("Training ${session.calories} kcal")
                            session.trackerCalories?.let { add("Tracker gesamt $it kcal") }
                            if (session.note.isNotBlank()) add(session.note)
                        }.joinToString(" · ")
                        Text("${session.date}${if (details.isNotBlank()) " · $details" else ""}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = { onDelete(index) }) { Text("Entfernen") }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun QuickSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    enabled: Boolean = true,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Text(valueLabel, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SportActivitySelector(selected: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        listOf("Morgensport", "Laufen", "Fahrrad", "Kein Training").chunked(2).forEach { choices ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                choices.forEach { choice ->
                    TextButton(onClick = { onSelect(choice) }, modifier = Modifier.weight(1f)) {
                        Text(if (selected == choice) "● $choice" else choice, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable private fun MeasurementsScreen(
    modifier: Modifier,
    measurements: List<BodyMeasurement>,
    rangeDays: Int,
    weight: String,
    bodyFat: String,
    abdomen: String,
    neck: String,
    hunger: String,
    energy: String,
    onRangeChange: (Int) -> Unit,
    onWeightChange: (String) -> Unit,
    onBodyFatChange: (String) -> Unit,
    onAbdomenChange: (String) -> Unit,
    onNeckChange: (String) -> Unit,
    onHungerChange: (String) -> Unit,
    onEnergyChange: (String) -> Unit,
    onOpenPhotos: () -> Unit,
    onAdd: () -> Unit
) {
    val cutoff = LocalDate.now().minusDays(rangeDays.toLong() - 1)
    val visible = measurements.filter { runCatching { !LocalDate.parse(it.date).isBefore(cutoff) }.getOrDefault(false) }
    val weights = visible.mapNotNull { item -> item.weight?.let { item.date to it } }
    val scaleBodyFatValues = visible.mapNotNull { item -> item.scaleBodyFat?.let { item.date to it } }
    val navyValues = visible.mapNotNull { item -> navyBodyFat(item.neck, item.abdomen, null)?.let { item.date to it } }
    val allWeights = measurements.mapNotNull { it.weight }
    val currentWeight = measurements.lastOrNull { it.weight != null }?.weight
    val startWeight = allWeights.firstOrNull()
    val latestScaleBodyFat = measurements.lastOrNull { it.scaleBodyFat != null }?.scaleBodyFat
    val latestNeck = measurements.lastOrNull { it.neck != null }?.neck
    val latestAbdomen = measurements.lastOrNull { it.abdomen != null }?.abdomen
    val latestNavy = measurements.asReversed().firstNotNullOfOrNull { navyBodyFat(it.neck, it.abdomen, null) }
    val latestHunger = measurements.lastOrNull { it.hunger != null }?.hunger
    val latestEnergy = measurements.lastOrNull { it.energy != null }?.energy
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Messungen", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(7, 14, 30).forEach { days ->
                    if (rangeDays == days) Button(onClick = { onRangeChange(days) }, modifier = Modifier.weight(1f)) { Text("$days Tage") }
                    else TextButton(onClick = { onRangeChange(days) }, modifier = Modifier.weight(1f)) { Text("$days Tage") }
                }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(currentWeight?.let { "${it.de1()} kg" } ?: "Noch kein Gewicht", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            if (startWeight != null && currentWeight != null) Text("Seit Start ${signed1(currentWeight - startWeight)} kg", color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(latestScaleBodyFat?.let { "${it.de1()} % KF" } ?: "KF –", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            Text(latestNavy?.let { "${it.de1()} % Navy" } ?: "Navy –", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Gewichtsverlauf", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    ValueChart(weights)
                    if (weights.isEmpty()) Text("Für diesen Zeitraum gibt es noch keine Gewichtswerte.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Körperfettverlauf", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Waage", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                    ValueChart(scaleBodyFatValues)
                    Text("Navy-Methode (Hals/Bauch; Körpergröße im Profil erforderlich)", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    ValueChart(navyValues)
                    if (scaleBodyFatValues.isEmpty() && navyValues.isEmpty()) Text("Für diesen Zeitraum gibt es noch keine Körperfettwerte.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Hals", latestNeck?.let { "${it.de1()} cm" } ?: "–", Modifier.weight(1f))
                MetricCard("Bauch", latestAbdomen?.let { "${it.de1()} cm" } ?: "–", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Hunger", latestHunger?.let { "$it / 10" } ?: "–", Modifier.weight(1f))
                MetricCard("Energie", latestEnergy?.let { "$it / 10" } ?: "–", Modifier.weight(1f))
            }
        }
        item {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Heutige Messung", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = weight, onValueChange = onWeightChange, label = { Text("Gewicht kg") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = bodyFat, onValueChange = onBodyFatChange, label = { Text("KF Waage %") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = neck, onValueChange = onNeckChange, label = { Text("Hals cm") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = abdomen, onValueChange = onAbdomenChange, label = { Text("Bauch cm") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = hunger, onValueChange = onHungerChange, label = { Text("Hunger 0–10") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = energy, onValueChange = onEnergyChange, label = { Text("Energie 0–10") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    val previewNavy = navyBodyFat(neck.toLocalizedDouble(), abdomen.toLocalizedDouble(), null)
                    if (previewNavy != null) Text("Navy-KFA wird als ${previewNavy.de1()} % gespeichert.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Button(onClick = onAdd, enabled = listOf(weight, bodyFat, abdomen, neck, hunger, energy).any { it.isNotBlank() }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Messung speichern") }
                    TextButton(onClick = onOpenPhotos, modifier = Modifier.fillMaxWidth()) { Text("Fortschrittsfoto hinzufügen") }
                }
            }
        }
        item { Text("Die Werte dienen der persönlichen Übersicht und ersetzen keine medizinische Beurteilung.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp)) }
    }
}

@Composable private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = modifier) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable private fun ChartCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable private fun RangeDayLabels(dates: List<LocalDate>) {
    if (dates.isEmpty()) return
    val label: (LocalDate) -> String = { date -> "${date.dayOfMonth}. ${date.month.getDisplayName(TextStyle.SHORT, Locale.GERMAN)}" }
    if (dates.size <= 7) {
        Row(modifier = Modifier.fillMaxWidth()) {
            dates.forEach { date ->
                Text(date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.GERMAN).take(2), modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(label(dates.first()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text(label(dates[dates.size / 2]), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text(label(dates.last()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable private fun ChartLegend(items: List<Pair<String, Color>>) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items.forEach { (label, color) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = color, modifier = Modifier.width(9.dp).height(9.dp)) {}
                Spacer(Modifier.width(5.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable private fun NormalizedMacroChart(protein: List<Double>, fat: List<Double>, carbs: List<Double>, targets: NutritionTargets) {
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
    val series = listOf(
        protein.map { if (targets.protein > 0) (it / targets.protein).coerceIn(0.0, 1.4) else 0.0 } to ProteinBlue,
        fat.map { if (targets.fat > 0) (it / targets.fat).coerceIn(0.0, 1.4) else 0.0 } to FatAmber,
        carbs.map { if (targets.carbs > 0) (it / targets.carbs).coerceIn(0.0, 1.4) else 0.0 } to CarbsCoral
    )
    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
        repeat(4) { index ->
            val y = size.height * index / 3f
            drawLine(gridColor, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        val targetY = size.height * (1f - 1f / 1.4f)
        drawLine(CaloriesGreen.copy(alpha = 0.5f), androidx.compose.ui.geometry.Offset(0f, targetY), androidx.compose.ui.geometry.Offset(size.width, targetY), strokeWidth = 2.dp.toPx())
        series.forEach { (values, color) ->
            if (values.isNotEmpty()) {
                val step = if (values.size > 1) size.width / (values.size - 1) else size.width
                val path = Path()
                values.forEachIndexed { index, value ->
                    val point = androidx.compose.ui.geometry.Offset(index * step, size.height - (value / 1.4).toFloat() * size.height)
                    if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                }
                drawPath(path, color, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
            }
        }
    }
}

@Composable private fun DualValueChart(first: List<Pair<String, Double>>, second: List<Pair<String, Double>>, firstColor: Color, secondColor: Color, target: Double?) {
    val all = first.map { it.second } + second.map { it.second } + listOfNotNull(target)
    if (all.size < 2) {
        Text("Noch nicht genug Werte für einen Verlauf.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 20.dp))
        return
    }
    val minValue = all.minOrNull() ?: return
    val maxValue = all.maxOrNull() ?: return
    val span = (maxValue - minValue).coerceAtLeast(1.0)
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
    Canvas(modifier = Modifier.fillMaxWidth().height(170.dp).padding(top = 12.dp)) {
        repeat(4) { index ->
            val y = size.height * index / 3f
            drawLine(gridColor, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        target?.let {
            val y = size.height - ((it - minValue) / span).toFloat() * size.height
            drawLine(CaloriesGreen.copy(alpha = 0.5f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = 2.dp.toPx())
        }
        listOf(first to firstColor, second to secondColor).forEach { (points, color) ->
            if (points.size >= 2) {
                val step = size.width / (points.size - 1)
                val path = Path()
                points.forEachIndexed { index, point ->
                    val y = size.height - ((point.second - minValue) / span).toFloat() * size.height
                    if (index == 0) path.moveTo(0f, y) else path.lineTo(index * step, y)
                    drawCircle(color, radius = 5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(index * step, y))
                }
                drawPath(path, color, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
            }
        }
    }
}

@Composable private fun ScoreTrendChart(hunger: List<Double?>, energy: List<Double?>) {
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
        repeat(3) { index ->
            val y = size.height * index / 2f
            drawLine(gridColor, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        listOf(hunger to HungerPurple, energy to EnergyTeal).forEach { (values, color) ->
            val step = if (values.size > 1) size.width / (values.size - 1) else size.width
            var previous: androidx.compose.ui.geometry.Offset? = null
            values.forEachIndexed { index, value ->
                if (value == null) {
                    previous = null
                } else {
                    val point = androidx.compose.ui.geometry.Offset(index * step, size.height - (value / 10.0).toFloat() * size.height)
                    previous?.let { drawLine(color, it, point, strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round) }
                    drawCircle(color, radius = 5.dp.toPx(), center = point)
                    previous = point
                }
            }
        }
    }
}

@Composable private fun ValueChart(weights: List<Pair<String, Double>>, color: Color = CaloriesGreen, target: Double? = null) {
    if (weights.size < 2) return
    val values = weights.map { it.second } + listOfNotNull(target)
    val minValue = values.minOrNull() ?: return
    val maxValue = values.maxOrNull() ?: return
    val span = (maxValue - minValue).coerceAtLeast(1.0)
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 12.dp)) {
        repeat(4) { index ->
            val y = size.height * index / 3f
            drawLine(gridColor, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        target?.let {
            val y = size.height - (((it - minValue) / span).toFloat() * size.height * 0.75f + size.height * 0.1f)
            drawLine(CaloriesGreen.copy(alpha = 0.5f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = 2.dp.toPx())
        }
        val sourceValues = weights.map { it.second }
        val stepX = if (sourceValues.size > 1) size.width / (sourceValues.size - 1) else size.width
        val path = Path()
        sourceValues.forEachIndexed { index, value ->
            val x = stepX * index
            val y = size.height - (((value - minValue) / span).toFloat() * size.height * 0.75f + size.height * 0.1f)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(color, radius = 6.dp.toPx(), center = androidx.compose.ui.geometry.Offset(x, y))
        }
        drawPath(path, color, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun DailyLogScreen(
    modifier: Modifier,
    entries: List<DailyLogEntry>,
    sportSessions: List<SportSession>,
    measurements: List<BodyMeasurement>,
    measurementDefaults: BodyMeasurement?,
    startWeight: Double?,
    routines: List<DailyRoutine>,
    targets: NutritionTargets,
    selectedDate: String,
    description: String,
    title: String,
    calories: String,
    protein: String,
    fat: String,
    carbs: String,
    onDescriptionChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onCarbsChange: (String) -> Unit,
    hasPhoto: Boolean,
    isEstimating: Boolean,
    analysisReady: Boolean,
    nutritionHint: String?,
    onPhotoCaptured: (String) -> Unit,
    onVoiceCaptured: (String) -> Unit,
    onEstimate: () -> Unit,
    onAddSport: (String, Int, Int?) -> Unit,
    onSaveDayClose: (Int?, String) -> Unit,
    onAddMeasurement: (BodyMeasurement) -> Unit,
    onPreviousDay: () -> Unit,
    onToday: () -> Unit,
    onNextDay: () -> Unit,
    onUseRoutine: (DailyRoutine, Boolean) -> Unit,
    onSaveRoutine: () -> Unit,
    onDeleteEntry: (DailyLogEntry) -> Unit,
    onMarkConsumed: (DailyLogEntry) -> Unit,
    onDeleteSport: (SportSession) -> Unit,
    onDeleteMeasurement: (BodyMeasurement) -> Unit,
    onAdd: (Boolean) -> Unit
) {
    var captureError by remember { mutableStateOf<String?>(null) }
    var showMealComposer by remember { mutableStateOf(false) }
    var showMorningCheck by remember { mutableStateOf(false) }
    var showCaptureMenu by remember { mutableStateOf(false) }
    var showDaySummary by remember { mutableStateOf(false) }
    var dayTrackerCalories by remember { mutableStateOf("") }
    var dayCloseNote by remember { mutableStateOf("") }
    var morningSport by remember { mutableStateOf("0") }
    var morningEnergy by remember { mutableStateOf("7") }
    var morningHunger by remember { mutableStateOf("4") }
    var morningWeight by remember { mutableStateOf("") }
    var morningBodyFat by remember { mutableStateOf("") }
    var morningNeck by remember { mutableStateOf("") }
    var morningWaist by remember { mutableStateOf("") }
    var showMorningDetails by remember { mutableStateOf(false) }
    val morningSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val mealCamera = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            captureError = null
            runCatching { bitmapDataUrl(bitmap) }.onSuccess(onPhotoCaptured).onFailure { captureError = "Das Foto konnte nicht verarbeitet werden." }
        }
    }
    val speechRecognizer = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.trim()
            if (!spoken.isNullOrEmpty()) {
                captureError = null
                onVoiceCaptured(spoken)
            }
        }
    }
    val consumedFoods = entries.filter { it.type == "Mahlzeit" && !it.planned }
    val plannedFoods = entries.filter { it.type == "Mahlzeit" && it.planned }
    val intake = consumedFoods.sumOf { it.calories }
    val burned = entries.filter { it.type == "Sport" }.sumOf { it.calories }
    val proteinTotal = consumedFoods.sumOf { it.protein }
    val fatTotal = consumedFoods.sumOf { it.fat }
    val carbsTotal = consumedFoods.sumOf { it.carbs }
    val isToday = selectedDate == todayKey()
    val composerVisible = showMealComposer || description.isNotBlank() || isEstimating || analysisReady || hasPhoto
    val foodEntries = entries.filter { it.type == "Mahlzeit" }
    val currentMeasurement = measurements.lastOrNull()
    val currentSport = sportSessions.sumOf { it.calories }
    val dayTargets = adaptiveTargets(targets, currentSport, currentMeasurement?.energy)
    val morningCheckComplete = currentMeasurement?.energy != null || currentMeasurement?.hunger != null || sportSessions.any { it.activity == "Morgen-Check" }
    fun openMorningCheck() {
        val defaults = currentMeasurement ?: measurementDefaults
        morningSport = currentSport.toString()
        morningEnergy = (defaults?.energy ?: 7).toString()
        morningHunger = (defaults?.hunger ?: 4).toString()
        morningWeight = defaults?.weight?.toString() ?: startWeight?.toString() ?: ""
        morningBodyFat = defaults?.scaleBodyFat?.toString() ?: ""
        morningNeck = defaults?.neck?.toString() ?: ""
        morningWaist = defaults?.abdomen?.toString() ?: ""
        showMorningDetails = morningNeck.isNotBlank() || morningWaist.isNotBlank()
        showMealComposer = false
        showMorningCheck = true
    }
    if (showCaptureMenu) {
        ModalBottomSheet(onDismissRequest = { showCaptureMenu = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Erfassen", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Was möchtest du für $selectedDate hinzufügen?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(
                    onClick = { showCaptureMenu = false; showMorningCheck = false; showMealComposer = true },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(Icons.Filled.Restaurant, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Nahrung")
                }
                OutlinedButton(
                    onClick = { showCaptureMenu = false; openMorningCheck() },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(Icons.Filled.MonitorWeight, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Morgenwerte")
                }
                OutlinedButton(
                    onClick = {
                        val existing = sportSessions.lastOrNull()
                        dayTrackerCalories = existing?.trackerCalories?.toString() ?: ""
                        dayCloseNote = existing?.note ?: ""
                        showCaptureMenu = false
                        showDaySummary = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Tagesabschluss")
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }
    if (composerVisible) {
        ModalBottomSheet(onDismissRequest = { showMealComposer = false }) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Nahrung erfassen", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Text, Foto oder Sprache · KI-gestützt", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (routines.isNotEmpty()) item {
                    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Deine Standards", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            routines.takeLast(3).forEach { routine ->
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(routine.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                                    TextButton(onClick = { onUseRoutine(routine, false); showMealComposer = false }) { Text("Jetzt") }
                                    TextButton(onClick = { onUseRoutine(routine, true); showMealComposer = false }) { Text("Planen") }
                                }
                            }
                        }
                    }
                }
                item {
                    Text("Schreib einfach, was du gegessen hast – KF20 schätzt die Nährwerte.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = description,
                        onValueChange = onDescriptionChange,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        placeholder = { Text("z. B. zwei Brötchen mit Ei und Käse") },
                        minLines = 2,
                        maxLines = 4
                    )
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { mealCamera.launch(null) }, enabled = !isEstimating, modifier = Modifier.weight(1f)) { Text("Foto") }
                        OutlinedButton(onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Beschreibe deine Mahlzeit")
                            }
                            runCatching { speechRecognizer.launch(intent) }.onFailure { captureError = "Auf diesem Gerät ist keine Spracheingabe verfügbar." }
                        }, enabled = !isEstimating, modifier = Modifier.weight(1f)) { Text("Mikrofon") }
                    }
                }
                item {
                    Button(onClick = onEstimate, enabled = !isEstimating && (description.isNotBlank() || hasPhoto), modifier = Modifier.fillMaxWidth()) {
                        Text(if (isEstimating) "KI wertet aus …" else "Mit KI auswerten")
                    }
                    if (hasPhoto) Text("Das Foto wird nur für diese Schätzung übertragen und nicht gespeichert.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    captureError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    nutritionHint?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
                }
                if (analysisReady) item {
                    Text("KI-Schätzung prüfen", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = title, onValueChange = onTitleChange, label = { Text("Mahlzeit") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = calories, onValueChange = onCaloriesChange, label = { Text("Kalorien") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = protein, onValueChange = onProteinChange, label = { Text("Protein g") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = fat, onValueChange = onFatChange, label = { Text("Fett g") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = carbs, onValueChange = onCarbsChange, label = { Text("Carbs g") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onAdd(false); showMealComposer = false }, enabled = title.isNotBlank(), modifier = Modifier.weight(1f)) { Text("Jetzt gegessen") }
                        OutlinedButton(onClick = { onAdd(true); showMealComposer = false }, enabled = title.isNotBlank(), modifier = Modifier.weight(1f)) { Text("Später planen") }
                    }
                    TextButton(onClick = onSaveRoutine, enabled = title.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Auch als Standard speichern") }
                }
                item { OutlinedButton(onClick = { showMealComposer = false }, modifier = Modifier.fillMaxWidth()) { Text("Abbrechen") } }
            }
        }
    }
    if (showDaySummary) {
        ModalBottomSheet(onDismissRequest = { showDaySummary = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Tagesabschluss", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(selectedDate, color = MaterialTheme.colorScheme.onSurfaceVariant)
                DailyGoalHero(intake = intake, burned = burned, target = dayTargets.calories)
                Text(
                    "${proteinTotal.de1()} g Protein · ${fatTotal.de1()} g Fett · ${carbsTotal.de1()} g Carbs",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = dayTrackerCalories,
                    onValueChange = { dayTrackerCalories = it.filter(Char::isDigit).take(4) },
                    label = { Text("Tracker-Gesamtverbrauch kcal") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dayCloseNote,
                    onValueChange = { dayCloseNote = it.take(240) },
                    label = { Text("Tagesnotiz (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                Text("Der Tracker-Gesamtverbrauch bleibt getrennt vom Sportverbrauch und wird nicht doppelt verrechnet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(
                    onClick = {
                        onSaveDayClose(dayTrackerCalories.toIntOrNull(), dayCloseNote.trim())
                        showDaySummary = false
                    },
                    enabled = dayTrackerCalories.toIntOrNull() != null || dayCloseNote.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Tagesabschluss speichern") }
                OutlinedButton(onClick = { showDaySummary = false }, modifier = Modifier.fillMaxWidth()) { Text("Abbrechen") }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
    if (showMorningCheck) {
        val sportValue = morningSport.toIntOrNull()?.coerceIn(0, 1_200) ?: 0
        val energyValue = morningEnergy.toIntOrNull()?.coerceIn(1, 10) ?: 7
        val hungerValue = morningHunger.toIntOrNull()?.coerceIn(0, 10) ?: 4
        val factor = refeedFactor(energyValue)
        val addOn = (sportValue * factor).roundToInt()
        val previewTargets = adaptiveTargets(targets, sportValue, energyValue)
        ModalBottomSheet(onDismissRequest = { showMorningCheck = false }, sheetState = morningSheetState) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Morgen-Check", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(selectedDate, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item {
                    QuickSlider(
                        label = "Sport-Verbrauch laut Tracker",
                        value = sportValue.toFloat(),
                        valueRange = 0f..1_200f,
                        valueLabel = "$sportValue kcal",
                        onValueChange = { morningSport = (it / 10f).roundToInt().times(10).toString() }
                    )
                }
                item {
                    QuickSlider(
                        label = "Energie",
                        value = energyValue.toFloat(),
                        valueRange = 1f..10f,
                        valueLabel = "$energyValue / 10",
                        onValueChange = { morningEnergy = it.roundToInt().coerceIn(1, 10).toString() }
                    )
                }
                item {
                    QuickSlider(
                        label = "Hunger",
                        value = hungerValue.toFloat(),
                        valueRange = 0f..10f,
                        valueLabel = "$hungerValue / 10",
                        onValueChange = { morningHunger = it.roundToInt().coerceIn(0, 10).toString() }
                    )
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = morningWeight, onValueChange = { morningWeight = decimalInput(it) }, label = { Text("Gewicht kg") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = morningBodyFat, onValueChange = { morningBodyFat = decimalInput(it) }, label = { Text("KFA Waage %") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                }
                item {
                    TextButton(onClick = { showMorningDetails = !showMorningDetails }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (showMorningDetails) "Hals & Bauch ausblenden" else "Hals & Bauch ergänzen")
                    }
                    if (showMorningDetails) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(value = morningNeck, onValueChange = { morningNeck = decimalInput(it) }, label = { Text("Hals cm") }, modifier = Modifier.weight(1f), singleLine = true)
                            OutlinedTextField(value = morningWaist, onValueChange = { morningWaist = decimalInput(it) }, label = { Text("Bauch cm") }, modifier = Modifier.weight(1f), singleLine = true)
                        }
                    }
                }
                item {
                    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("Energie $energyValue = ${refeedLabel(energyValue)} → Refeed ${(factor * 100).roundToInt()} %", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("${targets.calories} + $sportValue × ${factor.de1()} = ${previewTargets.calories} kcal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("${previewTargets.protein.de1()} g Protein · ${previewTargets.fat.de1()} g Fett · ${previewTargets.carbs.de1()} g Carbs", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            if (addOn > 0) Text("+$addOn kcal aus dem Sport-Refeed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
                item {
                    Button(onClick = {
                        onAddSport("Morgen-Check", sportValue, null)
                        onAddMeasurement(
                            BodyMeasurement(
                                selectedDate,
                                morningWeight.toLocalizedDouble(),
                                morningBodyFat.toLocalizedDouble(),
                                morningNeck.toLocalizedDouble(),
                                morningWaist.toLocalizedDouble(),
                                hungerValue,
                                energyValue
                            )
                        )
                        showMorningCheck = false
                    }, modifier = Modifier.fillMaxWidth()) { Text("Tag starten") }
                }
                item { OutlinedButton(onClick = { showMorningCheck = false }, modifier = Modifier.fillMaxWidth()) { Text("Später") } }
            }
        }
    }
    Box(modifier = modifier) {
      LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(if (isToday) "Heute" else "Tageslog", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(selectedDate, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onPreviousDay) { Text("‹") }
                TextButton(onClick = onToday, enabled = !isToday) { Text("Heute") }
                TextButton(onClick = onNextDay) { Text("›") }
            }
        }
        item { DailyGoalHero(intake = intake, burned = burned, target = dayTargets.calories) }
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp)) {
                    Text("Makros heute", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        MacroRing("Protein", proteinTotal, dayTargets.protein, "g", ProteinBlue, Modifier.weight(1f))
                        MacroRing("Fett", fatTotal, dayTargets.fat, "g", FatAmber, Modifier.weight(1f))
                        MacroRing("Carbs", carbsTotal, dayTargets.carbs, "g", CarbsCoral, Modifier.weight(1f))
                    }
                }
            }
        }
        item {
            MorningCheckCard(completed = morningCheckComplete, energy = currentMeasurement?.energy, hunger = currentMeasurement?.hunger, sport = currentSport, onOpen = { openMorningCheck() })
        }
        if (plannedFoods.isNotEmpty()) item {
            PlannedNutritionCard(plannedFoods = plannedFoods, intake = intake, targets = dayTargets)
        }
        item {
            Column {
                Text(if (isToday) "Heute erfasst" else "Am $selectedDate erfasst", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Nahrung, Sport und Messwerte in einer Liste", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (foodEntries.isEmpty() && sportSessions.isEmpty() && measurements.isEmpty()) {
            item {
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                    Text("Noch nichts erfasst. Nutze das Plus für Nahrung, Morgenwerte oder den Tagesabschluss.", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
        items(measurements.reversed()) { measurement ->
            val details = listOfNotNull(
                measurement.weight?.let { "${it.de1()} kg" },
                measurement.scaleBodyFat?.let { "KF ${it.de1()} %" },
                measurement.neck?.let { "Hals ${it.de1()} cm" },
                measurement.abdomen?.let { "Bauch ${it.de1()} cm" },
                measurement.hunger?.let { "Hunger $it/10" },
                measurement.energy?.let { "Energie $it/10" }
            ).joinToString(" · ")
            DailyEntryCard(
                type = "Messwerte",
                title = "Körper & Befinden",
                details = details,
                onDelete = { onDeleteMeasurement(measurement) }
            )
        }
        items(sportSessions.reversed()) { session ->
            val details = listOfNotNull(
                if (session.activity == "Morgen-Check") "${session.calories} kcal laut Tracker" else session.calories.takeIf { it > 0 }?.let { "$it kcal Training" },
                session.trackerCalories?.let { "$it kcal Tracker gesamt" },
                session.note.takeIf { it.isNotBlank() }
            ).joinToString(" · ")
            DailyEntryCard(type = "Sport", title = session.activity, details = details, onDelete = { onDeleteSport(session) })
        }
        items(foodEntries.reversed()) { entry ->
            DailyEntryCard(
                type = if (entry.planned) "Nahrung · geplant" else "Nahrung",
                title = entry.title,
                details = "${entry.calories} kcal · P ${"%.0f".format(entry.protein)} g · F ${"%.0f".format(entry.fat)} g · C ${"%.0f".format(entry.carbs)} g",
                actionLabel = if (entry.planned) "Als gegessen" else null,
                onAction = if (entry.planned) ({ onMarkConsumed(entry) }) else null,
                onDelete = { onDeleteEntry(entry) }
            )
        }
        item { Spacer(Modifier.height(96.dp)) }
      }
      FloatingActionButton(
          onClick = { showCaptureMenu = true },
          modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
      ) {
          Icon(Icons.Filled.Add, contentDescription = "Erfassen")
      }
    }
}

@Composable private fun MorningCheckCard(completed: Boolean, energy: Int?, hunger: Int?, sport: Int, onOpen: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (completed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                if (completed) "MORGEN-CHECK ERLEDIGT" else "MORGEN-CHECK OFFEN",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (completed) listOfNotNull(
                    energy?.let { "Energie $it/10" },
                    hunger?.let { "Hunger $it/10" },
                    "$sport kcal Sport"
                ).joinToString(" · ")
                else "Sport, Energie, Hunger und Gewicht bestimmen dein Tagesziel.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onOpen, modifier = Modifier.fillMaxWidth()) { Text(if (completed) "Morgen-Check ändern" else "Jetzt eintragen") }
        }
    }
}

@Composable private fun DailyEntryCard(type: String, title: String, details: String, actionLabel: String? = null, onAction: (() -> Unit)? = null, onDelete: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (details.isNotBlank()) Text(details, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Column(horizontalAlignment = Alignment.End) {
                if (actionLabel != null && onAction != null) TextButton(onClick = onAction) { Text(actionLabel) }
                TextButton(onClick = onDelete) { Text("Entfernen") }
            }
        }
    }
}

@Composable private fun PlannedNutritionCard(plannedFoods: List<DailyLogEntry>, intake: Int, targets: NutritionTargets) {
    val plannedCalories = plannedFoods.sumOf { it.calories }
    val plannedProtein = plannedFoods.sumOf { it.protein }
    val plannedFat = plannedFoods.sumOf { it.fat }
    val plannedCarbs = plannedFoods.sumOf { it.carbs }
    val forecastRemaining = targets.calories - intake - plannedCalories
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Für später geplant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("${plannedFoods.size} ${if (plannedFoods.size == 1) "Eintrag" else "Einträge"} · $plannedCalories kcal", color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Text(if (forecastRemaining >= 0) "$forecastRemaining kcal frei" else "${-forecastRemaining} kcal darüber", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Text("Prognose mit Planung · P ${plannedProtein.de1()} g · F ${plannedFat.de1()} g · C ${plannedCarbs.de1()} g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable private fun DailyGoalHero(intake: Int, burned: Int, target: Int) {
    val remaining = maxOf(0, target - intake)
    val progress = if (target > 0) (intake.toFloat() / target).coerceIn(0f, 1f) else 0f
    Surface(shape = RoundedCornerShape(28.dp), color = CaloriesGreen, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(132.dp).height(132.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 13.dp.toPx()
                    drawArc(Color.White.copy(alpha = 0.18f), -90f, 360f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                    drawArc(Color.White, -90f, progress * 360f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$remaining", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("kcal übrig", color = Color.White.copy(alpha = 0.86f), style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Dein Tagesziel", color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.labelLarge)
                Text("${(progress * 100).toInt()} % erreicht", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                HeroMetric("Tagesziel", "$target kcal")
                HeroMetric("Gegessen", "$intake kcal")
                HeroMetric("Sport", "$burned kcal")
            }
        }
    }
}

@Composable private fun HeroMetric(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = Color.White.copy(alpha = 0.72f), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable private fun MacroRing(label: String, value: Double, target: Double, unit: String, color: Color, modifier: Modifier = Modifier) {
    val progress = if (target > 0) (value / target).coerceIn(0.0, 1.0).toFloat() else 0f
    Column(modifier = modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.width(86.dp).height(86.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 9.dp.toPx()
                drawArc(color.copy(alpha = 0.14f), -90f, 360f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                drawArc(color, -90f, progress * 360f, false, style = Stroke(stroke, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = color)
                Text("${"%.0f".format(value)} $unit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        Text("Ziel ${"%.0f".format(target)} $unit", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable private fun VerticalBarChart(values: List<Double>, target: Double, color: Color, modifier: Modifier = Modifier) {
    val highest = maxOf(target, values.maxOrNull() ?: 0.0, 1.0)
    val targetColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    Canvas(modifier = modifier.fillMaxWidth().height(112.dp)) {
        val gap = (if (values.size > 14) 3.dp else 8.dp).toPx()
        val barWidth = ((size.width - gap * (values.size - 1)) / values.size).coerceAtLeast(4.dp.toPx())
        val chartTop = 8.dp.toPx()
        val chartBottom = size.height - 6.dp.toPx()
        val chartHeight = chartBottom - chartTop
        val targetY = chartBottom - (target / highest).toFloat() * chartHeight
        drawLine(targetColor, androidx.compose.ui.geometry.Offset(0f, targetY), androidx.compose.ui.geometry.Offset(size.width, targetY), strokeWidth = 2.dp.toPx())
        values.forEachIndexed { index, value ->
            val left = index * (barWidth + gap)
            val barHeight = ((value / highest).toFloat() * chartHeight).coerceAtLeast(3.dp.toPx())
            drawRoundRect(color.copy(alpha = if (index == values.lastIndex) 1f else 0.62f), topLeft = androidx.compose.ui.geometry.Offset(left, chartBottom - barHeight), size = androidx.compose.ui.geometry.Size(barWidth, barHeight), cornerRadius = androidx.compose.ui.geometry.CornerRadius(7.dp.toPx(), 7.dp.toPx()))
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
    val uriHandler = LocalUriHandler.current
    val linkColor = if (isUser) Color(0xFFD7E7B5) else MaterialTheme.colorScheme.primary
    val annotated = buildAnnotatedString {
        append(message.content)
        Regex("https?://[^\\s)\\]}>،,]+", RegexOption.IGNORE_CASE).findAll(message.content).forEach { match ->
            addStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline), match.range.first, match.range.last + 1)
            addStringAnnotation("URL", match.value.trimEnd('.', ';', ':'), match.range.first, match.range.last + 1)
        }
    }
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
            shape = RoundedCornerShape(18.dp)
        ) {
            ClickableText(
                text = annotated,
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.bodyLarge.copy(color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer),
                onClick = { offset -> annotated.getStringAnnotations("URL", offset, offset).firstOrNull()?.let { runCatching { uriHandler.openUri(it.item) } } }
            )
        }
    }
}

@Composable private fun TypingIndicator() = Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
    CircularProgressIndicator(modifier = Modifier.width(18.dp).height(18.dp), strokeWidth = 2.dp)
    Spacer(Modifier.width(8.dp)); Text("KF20 denkt nach …", color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private fun kf20Colors(style: AppStyle): ColorScheme = when (style) {
    AppStyle.HEALTH_LIGHT -> androidx.compose.material3.lightColorScheme(
        primary = CaloriesGreen,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD8F2E5),
        onPrimaryContainer = Color(0xFF002116),
        secondary = ProteinBlue,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE5ECFA),
        onSecondaryContainer = Color(0xFF172A54),
        tertiary = CarbsCoral,
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFE2DC),
        onTertiaryContainer = Color(0xFF4D1711),
        surface = Color(0xFFFFFBF5),
        onSurface = Color(0xFF1A1C1B),
        surfaceVariant = Color(0xFFF1EFE9),
        onSurfaceVariant = Color(0xFF5D625F),
        outline = Color(0xFF7A817D),
        background = Color(0xFFF8F6F1),
        onBackground = Color(0xFF1A1C1B)
    )
    AppStyle.PERFORMANCE_DARK -> androidx.compose.material3.darkColorScheme(
        primary = Color(0xFF4ADE80),
        onPrimary = Color(0xFF00210D),
        primaryContainer = Color(0xFF123D28),
        onPrimaryContainer = Color(0xFFC2F5D0),
        secondary = Color(0xFF8EAAFF),
        onSecondary = Color(0xFF10245B),
        secondaryContainer = Color(0xFF22345C),
        onSecondaryContainer = Color(0xFFDCE3FF),
        tertiary = Color(0xFFFF8B7D),
        onTertiary = Color(0xFF53140E),
        tertiaryContainer = Color(0xFF5D2924),
        onTertiaryContainer = Color(0xFFFFDAD5),
        surface = Color(0xFF151A1F),
        onSurface = Color(0xFFE8ECE9),
        surfaceVariant = Color(0xFF20272D),
        onSurfaceVariant = Color(0xFFBEC8C2),
        outline = Color(0xFF85918A),
        background = Color(0xFF0D1115),
        onBackground = Color(0xFFE8ECE9)
    )
    AppStyle.DATA_ATHLETE -> androidx.compose.material3.darkColorScheme(
        primary = Color(0xFF54D6E8),
        onPrimary = Color(0xFF00363D),
        primaryContainer = Color(0xFF10414B),
        onPrimaryContainer = Color(0xFFB6F2FA),
        secondary = Color(0xFFA8CBFF),
        onSecondary = Color(0xFF07315B),
        secondaryContainer = Color(0xFF193C60),
        onSecondaryContainer = Color(0xFFD5E5FF),
        tertiary = Color(0xFFFFB86B),
        onTertiary = Color(0xFF4A2800),
        tertiaryContainer = Color(0xFF5B3610),
        onTertiaryContainer = Color(0xFFFFDDB9),
        surface = Color(0xFF101E2A),
        onSurface = Color(0xFFE1ECF5),
        surfaceVariant = Color(0xFF182B3B),
        onSurfaceVariant = Color(0xFFBAC9D5),
        outline = Color(0xFF7C909F),
        background = Color(0xFF07131E),
        onBackground = Color(0xFFE1ECF5)
    )
}


internal fun todayKey(): String = LocalDate.now().toString()

private fun decimalInput(value: String): String {
    val filtered = value.filter { it.isDigit() || it == ',' || it == '.' }.take(7)
    val separator = filtered.indexOfFirst { it == ',' || it == '.' }
    return if (separator < 0) filtered else filtered.take(separator + 1) + filtered.drop(separator + 1).filter(Char::isDigit)
}

private fun String.toLocalizedDouble(): Double? = replace(',', '.').toDoubleOrNull()
private fun Double.de1(): String = "%.1f".format(this).replace('.', ',')
private fun signed1(value: Double): String = "${if (value > 0) "+" else ""}${value.de1()}"
private fun refeedLabel(energy: Int): String = when (energy) {
    in 1..4 -> "niedrig"
    in 8..10 -> "hoch"
    else -> "normal"
}
private fun bitmapDataUrl(bitmap: Bitmap): String {
    val output = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 82, output)
    val bytes = output.toByteArray()
    require(bytes.size <= 1_000_000) { "Das Bild ist zu groß." }
    return "data:image/jpeg;base64,${Base64.encodeToString(bytes, Base64.NO_WRAP)}"
}

private fun displayName(context: Context, uri: Uri): String = runCatching {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)) else uri.lastPathSegment
    }
}.getOrNull() ?: uri.lastPathSegment ?: "Datei"
