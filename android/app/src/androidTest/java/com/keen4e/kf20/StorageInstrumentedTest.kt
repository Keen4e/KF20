package com.keen4e.kf20

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StorageInstrumentedTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val preferences get() = context.getSharedPreferences("kf20_private", Context.MODE_PRIVATE)

    @Before
    fun clearBeforeTest() {
        preferences.edit().clear().commit()
        SecureStore.deleteKey()
    }

    @After
    fun clearAfterTest() {
        preferences.edit().clear().commit()
        SecureStore.deleteKey()
    }

    @Test
    fun encryptedStoreRoundTripsWithoutPlaintext() {
        val plaintext = "lokaler-testwert"
        val encrypted = SecureStore.encrypt(plaintext)

        assertNotEquals(plaintext, encrypted)
        assertFalse(encrypted.contains(plaintext))
        assertEquals(plaintext, SecureStore.decrypt(encrypted))
    }

    @Test
    fun dailyStorageRoundTripsCurrentSchema() {
        val entry = DailyLogEntry(
            "2030-01-02", "Mahlzeit", "Testmahlzeit", 500, 35.0, 18.0, 48.0, false,
            FoodPortionDetails(1.5, "Portion", preparation = "Zubereitet", assumption = "Testannahme"),
        )
        val storage = DailyLogStorage(context)

        storage.write(listOf(entry))

        assertEquals(listOf(entry), storage.read())
        assertFalse(preferences.getString("daily_entries", "").orEmpty().contains("Testmahlzeit"))
    }

    @Test
    fun legacyChatMigratesOnlyOnce() {
        val legacy = JSONArray().put(JSONObject().put("role", "user").put("content", "Testnachricht"))
        preferences.edit().putString("messages", SecureStore.encrypt(legacy.toString())).commit()
        val storage = ChatStorage(context)

        val first = storage.read()
        val second = storage.read()

        assertEquals(first, second)
        assertEquals("Hauptchat", first.single().title)
        assertEquals("Testnachricht", first.single().messages.single().content)
        assertEquals(first.single().id, storage.readActiveId())
        assertFalse(preferences.contains("messages"))
    }
}
