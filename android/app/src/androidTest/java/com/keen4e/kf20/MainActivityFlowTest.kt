package com.keen4e.kf20

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
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
        composeRule.onNodeWithText("Mit KI auswerten").assertExists()
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

        composeRule.onNodeWithText("Tracker-Gesamtverbrauch kcal").assertExists()
        composeRule.onNodeWithText("Der Tracker-Gesamtverbrauch bleibt getrennt vom Sportverbrauch und wird nicht doppelt verrechnet.").assertExists()
    }

    @Test
    fun statisticsCanSwitchBetweenRollingAverageAndDailyValues() {
        composeRule.onNodeWithText("Statistik").performClick()
        composeRule.onNodeWithText("7-Tage-Ø").performClick()
        composeRule.onNode(hasScrollAction()).performScrollToIndex(3)
        composeRule.onNodeWithText("Rollierender 7-Tage-Ø · Linie = Tagesziel").assertIsDisplayed()

        composeRule.onNode(hasScrollAction()).performScrollToIndex(0)
        composeRule.onNodeWithText("Tageswerte").performClick()
        composeRule.onNode(hasScrollAction()).performScrollToIndex(3)
        composeRule.onNodeWithText("Netto pro Tag · Linie = Tagesziel").assertExists()
    }
}
