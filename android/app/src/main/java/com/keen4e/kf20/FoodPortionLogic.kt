package com.keen4e.kf20

import java.util.Locale
import kotlin.math.roundToInt

internal val supportedFoodUnits = listOf("Portion", "g", "Stück", "EL", "TL")
internal val supportedPreparations = listOf("Nicht angegeben", "Roh", "Zubereitet")

internal fun portionMultiplier(details: FoodPortionDetails): Double? {
    if (!details.amount.isFinite() || details.amount <= 0 || details.unit !in supportedFoodUnits) return null
    if (details.unit == "Portion") return details.amount
    val basis = details.basisGrams?.takeIf { it.isFinite() && it > 0 } ?: return null
    val totalGrams = if (details.unit == "g") details.amount else {
        val gramsPerUnit = details.gramsPerUnit?.takeIf { it.isFinite() && it > 0 } ?: return null
        details.amount * gramsPerUnit
    }
    return totalGrams / basis
}

internal fun scaledNutrition(
    calories: Double,
    protein: Double,
    fat: Double,
    carbs: Double,
    details: FoodPortionDetails,
): NutritionEstimate? {
    val multiplier = portionMultiplier(details) ?: return null
    if (listOf(calories, protein, fat, carbs).any { !it.isFinite() || it < 0 }) return null
    return NutritionEstimate(
        name = "",
        calories = (calories * multiplier).roundToInt(),
        protein = protein * multiplier,
        fat = fat * multiplier,
        carbs = carbs * multiplier,
        confidence = "",
        note = "",
    )
}

internal fun FoodPortionDetails.summary(): String = buildString {
    append(if (amount % 1.0 == 0.0) amount.toInt() else "%.1f".format(Locale.GERMAN, amount))
    append(' ').append(unit)
    if (unit !in listOf("Portion", "g")) gramsPerUnit?.let { append(" à ").append("%.1f".format(Locale.GERMAN, it)).append(" g") }
    if (preparation != "Nicht angegeben") append(" · ").append(preparation.lowercase(Locale.GERMAN))
}
