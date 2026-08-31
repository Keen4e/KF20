package com.keen4e.kf20

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Kf20DataCodecTest {
    @Test
    fun `current daily log round trips all portion metadata`() {
        val entry = DailyLogEntry(
            date = "2030-01-02",
            type = "Mahlzeit",
            title = "Testmahlzeit",
            calories = 420,
            protein = 31.0,
            fat = 12.5,
            carbs = 44.0,
            planned = true,
            portion = FoodPortionDetails(2.0, "Stück", 150.0, 75.0, "Zubereitet", "Gewicht bestätigt"),
        )

        val decoded = Kf20DataCodec.decodeDailyEntries(Kf20DataCodec.encodeDailyEntries(listOf(entry)).toString())

        assertEquals(listOf(entry), decoded)
    }

    @Test
    fun `legacy daily log without portion remains readable`() {
        val legacy = JSONArray().put(
            JSONObject()
                .put("date", "2029-12-31")
                .put("type", "Mahlzeit")
                .put("title", "Legacy")
                .put("calories", 300)
                .put("protein", 20.0)
        )

        val decoded = Kf20DataCodec.decodeDailyEntries(legacy.toString()).single()

        assertEquals(0.0, decoded.fat, 0.0)
        assertEquals(0.0, decoded.carbs, 0.0)
        assertFalse(decoded.planned)
        assertEquals(FoodPortionDetails(), decoded.portion)
    }

    @Test
    fun `one malformed daily record does not discard valid records`() {
        val payload = JSONArray()
            .put(JSONObject().put("title", "unvollständig"))
            .put(JSONObject().put("date", "2030-01-01").put("type", "Mahlzeit").put("title", "gültig").put("calories", 100).put("protein", 10.0))

        val decoded = Kf20DataCodec.decodeDailyEntries(payload.toString())

        assertEquals(1, decoded.size)
        assertEquals("gültig", decoded.single().title)
    }

    @Test
    fun `legacy measurement aliases migrate to current fields`() {
        val legacy = JSONArray().put(
            JSONObject()
                .put("date", "2030-01-01")
                .put("bodyFat", 21.5)
                .put("waist", 88.0)
        )

        val decoded = Kf20DataCodec.decodeMeasurements(legacy.toString()).single()

        assertEquals(21.5, decoded.scaleBodyFat ?: 0.0, 0.0)
        assertEquals(88.0, decoded.abdomen ?: 0.0, 0.0)
        assertNull(decoded.weight)
    }

    @Test
    fun `legacy conversation migration is idempotent`() {
        val legacy = JSONArray().put(JSONObject().put("role", "user").put("content", "Testnachricht")).toString()
        val first = Kf20DataCodec.planConversationMigration(null, legacy, 1234L)
        val persisted = Kf20DataCodec.encodeConversations(first.conversations).toString()
        val second = Kf20DataCodec.planConversationMigration(persisted, legacy, 9999L)

        assertTrue(first.needsWrite)
        assertEquals("Hauptchat", first.conversations.single().title)
        assertEquals("Testnachricht", first.conversations.single().messages.single().content)
        assertFalse(second.needsWrite)
        assertEquals(first.conversations, second.conversations)
    }

    @Test
    fun `schema four export contains all domains and no api credentials`() {
        val json = LocalDataExport.createJson(
            conversations = listOf(ChatConversation("c1", "Test", listOf(ChatMessage("user", "Hallo")), 1L)),
            activeConversationId = "c1",
            tasks = listOf(AgentTask("Aufgabe", false)),
            dailyEntries = listOf(DailyLogEntry("2030-01-01", "Mahlzeit", "Essen", 100, 10.0, 2.0, 5.0)),
            routines = listOf(DailyRoutine("Standard", 100, 10.0, 2.0, 5.0)),
            weights = listOf(WeightEntry("2030-01-01", 80.0)),
            photos = listOf(ProgressPhoto("2030-01-01", "content://local/photo")),
            reminder = ReminderConfig(true, 20, 0),
            memories = listOf(UserMemory("Präferenz")),
            projects = listOf(ProjectEntry("Projekt", "Notiz", "Aktiv")),
            files = listOf(PrivateFile("2030-01-01", "content://local/file", "Datei", "text/plain")),
            targets = NutritionTargets(2_000, 150.0, 70.0, 200.0),
            sessions = listOf(SportSession("2030-01-01", "Laufen", 200, 2_400, "")),
            measurements = listOf(BodyMeasurement("2030-01-01", 80.0, 20.0, null, null, 4, 7)),
            profile = HealthProfile(82.0, 180.0, 78.0, 18.0),
            appStyle = AppStyle.PERFORMANCE_DARK,
        )
        val root = JSONObject(json)

        assertEquals(4, root.getInt("schemaVersion"))
        assertEquals("c1", root.getString("activeConversationId"))
        assertEquals(1, root.getJSONArray("dailyEntries").length())
        assertTrue(root.getJSONArray("dailyEntries").getJSONObject(0).has("portion"))
        assertTrue(root.has("sportSessions"))
        assertTrue(root.has("measurements"))
        assertTrue(root.has("uiPreferences"))
        assertFalse(root.has("apiSettings"))
        assertFalse(json.contains("token", ignoreCase = true))
    }
}
