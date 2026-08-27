package com.keen4e.kf20

import kotlin.math.log10
import kotlin.math.roundToInt

internal fun refeedFactor(energy: Int?): Double = when (energy) {
    in 1..4 -> 0.3
    in 8..10 -> 0.7
    else -> 0.5
}

internal fun adaptiveTargets(
    base: NutritionTargets,
    sportCalories: Int,
    energy: Int?,
): NutritionTargets {
    val addOn = (sportCalories.coerceAtLeast(0) * refeedFactor(energy)).roundToInt()
    return base.copy(
        calories = base.calories + addOn,
        carbs = base.carbs + addOn / 4.0,
    )
}

internal fun navyBodyFat(
    neck: Double?,
    abdomen: Double?,
    heightCm: Double?,
): Double? {
    if (neck == null || abdomen == null || abdomen <= neck || heightCm == null || heightCm <= 0) return null
    val result = 495.0 / (1.0324 - 0.19077 * log10(abdomen - neck) + 0.15456 * log10(heightCm)) - 450.0
    return result.takeIf { it in 2.0..70.0 }
}

internal fun rollingAverage(values: List<Double?>, window: Int = 7): List<Double?> {
    require(window > 0) { "window must be positive" }
    return values.indices.map { index ->
        values.subList(maxOf(0, index - window + 1), index + 1)
            .filterNotNull()
            .takeIf { it.isNotEmpty() }
            ?.average()
    }
}
