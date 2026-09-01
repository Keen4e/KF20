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
    fun `adaptive targets ignore negative sport calories`() {
        val base = NutritionTargets(calories = 2_000, protein = 150.0, fat = 70.0, carbs = 200.0)

        assertEquals(base, adaptiveTargets(base, sportCalories = -100, energy = 10))
    }

    @Test
    fun `missing energy uses the normal refeed band`() {
        assertEquals(0.5, refeedFactor(null), 0.0)
    }

    @Test
    fun `navy body fat requires complete plausible inputs`() {
        assertNull(navyBodyFat(neck = null, abdomen = 90.0, heightCm = 180.0))
        assertNull(navyBodyFat(neck = 95.0, abdomen = 90.0, heightCm = 180.0))
        assertNull(navyBodyFat(neck = 40.0, abdomen = 90.0, heightCm = 0.0))
        val estimate = requireNotNull(navyBodyFat(neck = 40.0, abdomen = 90.0, heightCm = 180.0))
        assertTrue(estimate in 2.0..70.0)
    }

    @Test
    fun `rolling average uses current and six previous values`() {
        val result = rollingAverage((1..8).map(Int::toDouble))

        assertEquals(1.0, result.first()!!, 0.0)
        assertEquals(5.0, result.last()!!, 0.0)
    }

    @Test
    fun `rolling average ignores missing values without inventing zeroes`() {
        val result = rollingAverage(listOf(null, 2.0, null, 4.0), window = 3)

        assertNull(result.first())
        assertEquals(2.0, result[1]!!, 0.0)
        assertEquals(2.0, result[2]!!, 0.0)
        assertEquals(3.0, result[3]!!, 0.0)
    }
}
