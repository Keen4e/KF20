package com.keen4e.kf20

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun primaryNavigationAndCaptureChoicesAreVisible() {
        listOf("Tag", "Statistik", "Chat", "Einstellungen").forEach {
            composeRule.onNodeWithText(it).assertIsDisplayed()
        }

        composeRule.onNodeWithContentDescription("Erfassen").performClick()

        listOf("Nahrung", "Morgenwerte", "Tagesabschluss").forEach {
            composeRule.onNodeWithText(it).assertIsDisplayed()
        }
    }

    @Test
    fun foodCaptureOpensTheSteppedAiFlow() {
        composeRule.onNodeWithContentDescription("Erfassen").performClick()
        composeRule.onNodeWithText("Nahrung").performClick()

        composeRule.onNodeWithText("Nahrung erfassen").assertIsDisplayed()
        composeRule.onNodeWithText("Text, Foto oder Sprache · KI-gestützt").assertIsDisplayed()
        composeRule.onNodeWithText("Mit KI auswerten").assertIsDisplayed()
    }

    @Test
    fun morningValuesOpenAsOneCombinedFlow() {
        composeRule.onNodeWithContentDescription("Erfassen").performClick()
        composeRule.onNodeWithText("Morgenwerte").performClick()

        composeRule.onNodeWithText("Morgen-Check").assertIsDisplayed()
        composeRule.onNodeWithText("Sport-Verbrauch laut Tracker").assertIsDisplayed()
        composeRule.onNodeWithText("Energie").assertIsDisplayed()
        composeRule.onNodeWithText("Hunger").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun dayCloseKeepsTrackerTotalSeparate() {
        composeRule.onNodeWithContentDescription("Erfassen").performClick()
        composeRule.onNodeWithText("Tagesabschluss").performClick()

        composeRule.onNodeWithText("Tracker-Gesamtverbrauch kcal").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Der Tracker-Gesamtverbrauch bleibt getrennt vom Sportverbrauch und wird nicht doppelt verrechnet.").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun statisticsCanSwitchToDailyValues() {
        composeRule.onNodeWithText("Statistik").performClick()
        composeRule.onNodeWithText("Tageswerte").performClick()

        composeRule.onNodeWithText("Netto pro Tag · Linie = Tagesziel").performScrollTo().assertIsDisplayed()
    }
}
