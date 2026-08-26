package com.keen4e.kf20

internal data class ChatMessage(val role: String, val content: String)
internal data class ChatConversation(val id: String, val title: String, val messages: List<ChatMessage>, val updatedAt: Long)
internal data class AgentTask(val title: String, val done: Boolean)
internal data class DailyLogEntry(
    val date: String,
    val type: String,
    val title: String,
    val calories: Int,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val planned: Boolean = false,
)
internal data class DailyRoutine(val title: String, val calories: Int, val protein: Double, val fat: Double, val carbs: Double)
internal data class WeightEntry(val date: String, val kilograms: Double)
internal data class ProgressPhoto(val date: String, val uri: String)
internal data class ApiSettings(val baseUrl: String, val token: String)
internal data class ReminderConfig(val enabled: Boolean, val hour: Int, val minute: Int)
internal data class UserMemory(val text: String)
internal data class ProjectEntry(val name: String, val notes: String, val status: String)
internal data class PrivateFile(val date: String, val uri: String, val name: String, val mimeType: String)
internal data class NutritionEstimate(
    val name: String,
    val calories: Int,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val confidence: String,
    val note: String,
)
internal data class NutritionTargets(val calories: Int, val protein: Double, val fat: Double, val carbs: Double)
internal data class SportSession(
    val date: String,
    val activity: String,
    val calories: Int,
    val trackerCalories: Int?,
    val note: String,
)
internal data class BodyMeasurement(
    val date: String,
    val weight: Double?,
    val scaleBodyFat: Double?,
    val neck: Double?,
    val abdomen: Double?,
    val hunger: Int?,
    val energy: Int?,
)
internal data class HealthProfile(
    val startWeight: Double?,
    val heightCm: Double?,
    val goalWeight: Double?,
    val goalBodyFat: Double?,
)

