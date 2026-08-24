package com.keen4e.kf20

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyTargetLogicTest {
    @Test
    fun `refeed factor follows the documented energy bands`() {
        assertEquals(0.3, refeedFactor(1), 0.0)
        assertEquals(0.3, refeedFactor(4), 0.0)
        assertEquals(0.5, refeedFactor(5), 0.0)
        assertEquals(0.5, refeedFactor(7), 0.0)
        assertEquals(0.7, refeedFactor(8), 0.0)
        assertEquals(0.7, refeedFactor(10), 0.0)
    }

    @Test
    fun `adaptive targets add sport refeed only to calories and carbs`() {
        val base = NutritionTargets(calories = 2_000, protein = 150.0, fat = 70.0, carbs = 200.0)

        val result = adaptiveTargets(base, sportCalories = 400, energy = 6)

        assertEquals(2_200, result.calories)
        assertEquals(150.0, result.protein, 0.0)
        assertEquals(70.0, result.fat, 0.0)
        assertEquals(250.0, result.carbs, 0.0)
    }

    @Test
    fun `navy body fat requires complete plausible inputs`() {
        assertNull(navyBodyFat(neck = null, abdomen = 90.0, heightCm = 180.0))
        assertNull(navyBodyFat(neck = 95.0, abdomen = 90.0, heightCm = 180.0))
        val estimate = requireNotNull(navyBodyFat(neck = 40.0, abdomen = 90.0, heightCm = 180.0))
        assertTrue(estimate in 2.0..70.0)
    }
}