package com.keen4e.kf20

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.KeyStore
import java.time.Instant
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

internal class UiPreferencesStorage(context: Context) {
    private val preferences = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)
    fun read(): AppStyle = runCatching {
        AppStyle.valueOf(preferences.getString("app_style", AppStyle.PERFORMANCE_DARK.name) ?: AppStyle.PERFORMANCE_DARK.name)
    }.getOrDefault(AppStyle.PERFORMANCE_DARK)
    fun write(style: AppStyle) {
        preferences.edit().putString("app_style", style.name).apply()
    }
}

internal class ChatStorage(context: Context) {
    private val preferences = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)
    fun read(): List<ChatConversation> {
        val stored = runCatching {
            val array = JSONArray(SecureStore.decrypt(preferences.getString("conversations", null)) ?: "[]")
            List(array.length()) { index ->
                array.getJSONObject(index).let { item ->
                    ChatConversation(
                        id = item.getString("id"),
                        title = item.optString("title", "Gespräch"),
                        messages = decodeMessages(item.optJSONArray("messages") ?: JSONArray()),
                        updatedAt = item.optLong("updatedAt", 0L)
                    )
                }
            }
        }.getOrDefault(emptyList())
        if (stored.isNotEmpty()) return stored.sortedByDescending { it.updatedAt }

        val legacyMessages = runCatching {
            decodeMessages(JSONArray(SecureStore.decrypt(preferences.getString("messages", null)) ?: "[]"))
        }.getOrDefault(emptyList())
        val now = Instant.now().toEpochMilli()
        val migrated = listOf(ChatConversation("chat-$now", "Hauptchat", legacyMessages, now))
        write(migrated)
        preferences.edit().remove("messages").putString("active_conversation_id", migrated.first().id).apply()
        return migrated
    }

    fun write(conversations: List<ChatConversation>) {
        val array = JSONArray()
        conversations.sortedByDescending { it.updatedAt }.take(50).forEach { conversation ->
            array.put(
                JSONObject()
                    .put("id", conversation.id)
                    .put("title", conversation.title)
                    .put("updatedAt", conversation.updatedAt)
                    .put("messages", encodeMessages(conversation.messages))
            )
        }
        preferences.edit().putString("conversations", SecureStore.encrypt(array.toString())).remove("messages").apply()
    }

    fun readActiveId(): String? = preferences.getString("active_conversation_id", null)
    fun writeActiveId(id: String) = preferences.edit().putString("active_conversation_id", id).apply()
    fun clear() = preferences.edit().remove("conversations").remove("messages").remove("active_conversation_id").apply()

    private fun decodeMessages(array: JSONArray): List<ChatMessage> = List(array.length()) { index ->
        array.getJSONObject(index).let { ChatMessage(it.getString("role"), it.getString("content")) }
    }

    private fun encodeMessages(messages: List<ChatMessage>): JSONArray = JSONArray().apply {
        messages.takeLast(500).forEach { message -> put(JSONObject().put("role", message.role).put("content", message.content)) }
    }
}

internal class DailyLogStorage(context: Context) {
    private val preferences = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)
    fun read(): List<DailyLogEntry> = runCatching {
        val array = JSONArray(SecureStore.decrypt(preferences.getString("daily_entries", null)) ?: "[]")
        List(array.length()) { index ->
            array.getJSONObject(index).let {
                DailyLogEntry(
                    it.optString("date", todayKey()), it.getString("type"), it.getString("title"), it.getInt("calories"),
                    it.getDouble("protein"), it.optDouble("fat", 0.0), it.optDouble("carbs", 0.0), it.optBoolean("planned", false),
                    decodeFoodPortion(it.optJSONObject("portion"))
                )
            }
        }
    }.getOrDefault(emptyList())
    fun write(entries: List<DailyLogEntry>) {
        val array = JSONArray(); entries.takeLast(500).forEach { entry ->
            array.put(JSONObject().put("date", entry.date).put("type", entry.type).put("title", entry.title).put("calories", entry.calories).put("protein", entry.protein).put("fat", entry.fat).put("carbs", entry.carbs).put("planned", entry.planned).put("portion", encodeFoodPortion(entry.portion)))
        }
        preferences.edit().putString("daily_entries", SecureStore.encrypt(array.toString())).apply()
    }
}

internal class RoutineStorage(context: Context) {
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

internal class WeightStorage(context: Context) {
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

internal class PhotoStorage(context: Context) {
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

internal class ApiSettingsStorage(context: Context) {
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

internal class ReminderStorage(context: Context) {
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

internal class MemoryStorage(context: Context) {
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

internal class ProjectStorage(context: Context) {
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

internal class FileStorage(context: Context) {
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

internal class TargetsStorage(context: Context) {
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

internal class SportStorage(context: Context) {
    private val preferences = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)
    fun read(): List<SportSession> = runCatching {
        val array = JSONArray(SecureStore.decrypt(preferences.getString("sport_sessions", null)) ?: "[]")
        List(array.length()) { index ->
            array.getJSONObject(index).let {
                SportSession(it.getString("date"), it.getString("activity"), it.optInt("calories", 0), it.optionalInt("trackerCalories"), it.optString("note", ""))
            }
        }
    }.getOrDefault(emptyList())
    fun write(sessions: List<SportSession>) {
        val array = JSONArray(); sessions.takeLast(500).forEach { session ->
            array.put(JSONObject().put("date", session.date).put("activity", session.activity).put("calories", session.calories).put("trackerCalories", session.trackerCalories ?: JSONObject.NULL).put("note", session.note))
        }
        preferences.edit().putString("sport_sessions", SecureStore.encrypt(array.toString())).apply()
    }
}

internal class MeasurementStorage(context: Context) {
    private val preferences = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)
    fun read(): List<BodyMeasurement> = runCatching {
        val array = JSONArray(SecureStore.decrypt(preferences.getString("body_measurements", null)) ?: "[]")
        List(array.length()) { index ->
            array.getJSONObject(index).let {
                BodyMeasurement(
                    date = it.getString("date"),
                    weight = it.optionalDouble("weight"),
                    scaleBodyFat = it.optionalDouble("scaleBodyFat") ?: it.optionalDouble("bodyFat"),
                    neck = it.optionalDouble("neck"),
                    abdomen = it.optionalDouble("abdomen") ?: it.optionalDouble("waist"),
                    hunger = it.optionalInt("hunger"),
                    energy = it.optionalInt("energy")
                )
            }
        }
    }.getOrDefault(emptyList())
    fun write(measurements: List<BodyMeasurement>) {
        val array = JSONArray(); measurements.takeLast(1000).forEach { item ->
            array.put(
                JSONObject().put("date", item.date)
                    .put("weight", item.weight ?: JSONObject.NULL)
                    .put("scaleBodyFat", item.scaleBodyFat ?: JSONObject.NULL)
                    .put("neck", item.neck ?: JSONObject.NULL)
                    .put("abdomen", item.abdomen ?: JSONObject.NULL)
                    .put("hunger", item.hunger ?: JSONObject.NULL)
                    .put("energy", item.energy ?: JSONObject.NULL)
            )
        }
        preferences.edit().putString("body_measurements", SecureStore.encrypt(array.toString())).apply()
    }
}

internal class HealthProfileStorage(context: Context) {
    private val preferences = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)
    fun read(): HealthProfile = runCatching {
        val data = JSONObject(SecureStore.decrypt(preferences.getString("health_profile", null)) ?: "{}")
        HealthProfile(
            startWeight = data.optionalDouble("startWeight"),
            heightCm = data.optionalDouble("heightCm"),
            goalWeight = data.optionalDouble("goalWeight"),
            goalBodyFat = data.optionalDouble("goalBodyFat")
        )
    }.getOrDefault(HealthProfile(null, null, null, null))
    fun write(profile: HealthProfile) {
        val data = JSONObject()
            .put("startWeight", profile.startWeight ?: JSONObject.NULL)
            .put("heightCm", profile.heightCm ?: JSONObject.NULL)
            .put("goalWeight", profile.goalWeight ?: JSONObject.NULL)
            .put("goalBodyFat", profile.goalBodyFat ?: JSONObject.NULL)
        preferences.edit().putString("health_profile", SecureStore.encrypt(data.toString())).apply()
    }
}

internal fun JSONObject.optionalDouble(key: String): Double? = if (has(key) && !isNull(key)) optDouble(key).takeIf { !it.isNaN() } else null
internal fun JSONObject.optionalInt(key: String): Int? = if (has(key) && !isNull(key)) optInt(key) else null

internal fun decodeFoodPortion(value: JSONObject?): FoodPortionDetails = if (value == null) FoodPortionDetails() else FoodPortionDetails(
    amount = value.optDouble("amount", 1.0).takeIf { it.isFinite() && it > 0 } ?: 1.0,
    unit = value.optString("unit", "Portion").takeIf { it in supportedFoodUnits } ?: "Portion",
    basisGrams = value.optionalDouble("basisGrams")?.takeIf { it > 0 },
    gramsPerUnit = value.optionalDouble("gramsPerUnit")?.takeIf { it > 0 },
    preparation = value.optString("preparation", "Nicht angegeben").takeIf { it in supportedPreparations } ?: "Nicht angegeben",
    assumption = value.optString("assumption", "").take(500),
)

internal fun encodeFoodPortion(value: FoodPortionDetails): JSONObject = JSONObject()
    .put("amount", value.amount)
    .put("unit", value.unit)
    .put("basisGrams", value.basisGrams ?: JSONObject.NULL)
    .put("gramsPerUnit", value.gramsPerUnit ?: JSONObject.NULL)
    .put("preparation", value.preparation)
    .put("assumption", value.assumption)

internal class TaskStorage(context: Context) {
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

internal object LocalDataExport {
    fun createJson(
        conversations: List<ChatConversation>,
        activeConversationId: String,
        tasks: List<AgentTask>,
        dailyEntries: List<DailyLogEntry>,
        routines: List<DailyRoutine>,
        weights: List<WeightEntry>,
        photos: List<ProgressPhoto>,
        reminder: ReminderConfig,
        memories: List<UserMemory>,
        projects: List<ProjectEntry>,
        files: List<PrivateFile>,
        targets: NutritionTargets,
        sessions: List<SportSession>,
        measurements: List<BodyMeasurement>,
        profile: HealthProfile,
        appStyle: AppStyle
    ): String {
        val root = JSONObject()
            .put("schemaVersion", 4)
            .put("exportedAt", Instant.now().toString())
            .put("notice", "Sensible KF20-Gesundheitsdaten. Serverzugang und Provider-Schlüssel sind ausgeschlossen.")
        root.put("activeConversationId", activeConversationId)
        root.put("conversations", JSONArray().apply {
            conversations.forEach { conversation ->
                put(
                    JSONObject()
                        .put("id", conversation.id)
                        .put("title", conversation.title)
                        .put("updatedAt", conversation.updatedAt)
                        .put("messages", JSONArray().apply {
                            conversation.messages.forEach { message -> put(JSONObject().put("role", message.role).put("content", message.content)) }
                        })
                )
            }
        })
        root.put("tasks", JSONArray().apply { tasks.forEach { put(JSONObject().put("title", it.title).put("done", it.done)) } })
        root.put("dailyEntries", JSONArray().apply {
            dailyEntries.forEach {
                put(JSONObject().put("date", it.date).put("type", it.type).put("title", it.title).put("calories", it.calories)
                    .put("protein", it.protein).put("fat", it.fat).put("carbs", it.carbs).put("planned", it.planned)
                    .put("portion", encodeFoodPortion(it.portion)))
            }
        })
        root.put("routines", JSONArray().apply {
            routines.forEach { put(JSONObject().put("title", it.title).put("calories", it.calories).put("protein", it.protein).put("fat", it.fat).put("carbs", it.carbs)) }
        })
        root.put("weights", JSONArray().apply { weights.forEach { put(JSONObject().put("date", it.date).put("kilograms", it.kilograms)) } })
        root.put("photos", JSONArray().apply { photos.forEach { put(JSONObject().put("date", it.date).put("uri", it.uri)) } })
        root.put("reminder", JSONObject().put("enabled", reminder.enabled).put("hour", reminder.hour).put("minute", reminder.minute))
        root.put("memories", JSONArray().apply { memories.forEach { put(it.text) } })
        root.put("projects", JSONArray().apply { projects.forEach { put(JSONObject().put("name", it.name).put("notes", it.notes).put("status", it.status)) } })
        root.put("privateFiles", JSONArray().apply {
            files.forEach { put(JSONObject().put("date", it.date).put("uri", it.uri).put("name", it.name).put("mimeType", it.mimeType)) }
        })
        root.put("targets", JSONObject().put("calories", targets.calories).put("protein", targets.protein).put("fat", targets.fat).put("carbs", targets.carbs))
        root.put("sportSessions", JSONArray().apply {
            sessions.forEach { put(JSONObject().put("date", it.date).put("activity", it.activity).put("calories", it.calories).put("trackerCalories", it.trackerCalories ?: JSONObject.NULL).put("note", it.note)) }
        })
        root.put("measurements", JSONArray().apply {
            measurements.forEach {
                put(JSONObject().put("date", it.date).put("weight", it.weight ?: JSONObject.NULL).put("scaleBodyFat", it.scaleBodyFat ?: JSONObject.NULL)
                    .put("neck", it.neck ?: JSONObject.NULL).put("abdomen", it.abdomen ?: JSONObject.NULL)
                    .put("hunger", it.hunger ?: JSONObject.NULL).put("energy", it.energy ?: JSONObject.NULL))
            }
        })
        root.put("profile", JSONObject().put("startWeight", profile.startWeight ?: JSONObject.NULL).put("heightCm", profile.heightCm ?: JSONObject.NULL)
            .put("goalWeight", profile.goalWeight ?: JSONObject.NULL).put("goalBodyFat", profile.goalBodyFat ?: JSONObject.NULL))
        root.put("uiPreferences", JSONObject().put("appStyle", appStyle.name))
        return root.toString(2)
    }
}

internal object SecureStore {
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

    fun deleteKey() {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(ALIAS)) keyStore.deleteEntry(ALIAS)
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

internal object Kf20LocalData {
    fun clear(context: Context) {
        context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE).edit().clear().commit()
        SecureStore.deleteKey()
    }
}
