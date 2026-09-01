package com.keen4e.kf20

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FoodPortionLogicTest {
    @Test
    fun `portions scale directly`() {
        val result = scaledNutrition(500.0, 30.0, 20.0, 50.0, FoodPortionDetails(amount = 1.5))
        assertEquals(750, result?.calories)
        assertEquals(45.0, result?.protein ?: 0.0, 0.0)
    }

    @Test
    fun `grams require confirmed basis weight`() {
        assertNull(portionMultiplier(FoodPortionDetails(amount = 200.0, unit = "g")))
        assertEquals(2.0, portionMultiplier(FoodPortionDetails(amount = 200.0, unit = "g", basisGrams = 100.0)) ?: 0.0, 0.0)
    }

    @Test
    fun `household units never invent a weight`() {
        val withoutWeight = FoodPortionDetails(amount = 2.0, unit = "EL", basisGrams = 100.0)
        val confirmedWeight = withoutWeight.copy(gramsPerUnit = 13.0)
        assertNull(portionMultiplier(withoutWeight))
        assertEquals(0.26, portionMultiplier(confirmedWeight) ?: 0.0, 0.0)
    }

    @Test
    fun `invalid amounts do not produce nutrition`() {
        assertNull(scaledNutrition(100.0, 10.0, 2.0, 5.0, FoodPortionDetails(amount = 0.0)))
    }
}
