package com.adoktl.engine

import com.adoktl.math.AdoktlColor
import kotlin.math.roundToInt

class ColorEngine {

    data class TileColorConfig(
        val trackColorType: String = "Single",
        val trackColor: AdoktlColor = AdoktlColor.WHITE,
        val secondaryTrackColor: AdoktlColor = AdoktlColor.WHITE,
        val trackColorAnimDuration: Double = 2.0,
        val trackStyle: String = "Standard",
        val trackGlowIntensity: Int = 100
    )

    private val hexMap = mapOf(
        '0' to 0, '1' to 1, '2' to 2, '3' to 3, '4' to 4,
        '5' to 5, '6' to 6, '7' to 7, '8' to 8, '9' to 9,
        'A' to 10, 'B' to 11, 'C' to 12, 'D' to 13, 'E' to 14, 'F' to 15,
        'a' to 10, 'b' to 11, 'c' to 12, 'd' to 13, 'e' to 14, 'f' to 15
    )

    private val sixth = listOf(1.0 / 6.0, 1.0 / 3.0, 1.0 / 2.0, 2.0 / 3.0, 5.0 / 6.0)

    private val rainbowProcess = mapOf(
        "RB" to Triple('G', 1, 0.16666666f),
        "GB" to Triple('R', -1, 0.16666666f),
        "GR" to Triple('B', 1, 0.3333333333f),
        "BR" to Triple('G', -1, 0.5f),
        "BG" to Triple('R', 1, 0.66666666666f),
        "RG" to Triple('B', -1, 0.83333333333f)
    )

    fun computeTileColors(
        config: TileColorConfig,
        index: Int,
        totalTiles: Int,
        elapsedTime: Double
    ): Pair<AdoktlColor, AdoktlColor> {
        val color = config.trackColor
        val secColor = config.secondaryTrackColor

        return when (config.trackStyle) {
            "Standard" -> Pair(color, addBlack(0.7, color))
            "Neon" -> Pair(AdoktlColor.BLACK, color)
            "NeonLight" -> Pair(halfColor(color), color)
            "Basic" -> Pair(color, AdoktlColor.BLACK)
            "Gems" -> Pair(color, addBlack(0.7, color))
            "Minimal" -> Pair(color, color)
            else -> Pair(color, addBlack(0.7, color))
        }
    }

    fun formatHexColor(hex: String): String {
        val h = hex.removePrefix("#")
        return if (h.length <= 6) "#$h"
        else "#${h.take(6)}"
    }

    private fun addBlack(opacity: Double, color: AdoktlColor): AdoktlColor {
        return AdoktlColor(
            r = color.r * (1f - opacity.toFloat()),
            g = color.g * (1f - opacity.toFloat()),
            b = color.b * (1f - opacity.toFloat()),
            a = color.a
        )
    }

    private fun halfColor(color: AdoktlColor): AdoktlColor {
        return AdoktlColor(
            r = color.r * 0.5f,
            g = color.g * 0.5f,
            b = color.b * 0.5f,
            a = color.a
        )
    }

    fun rainbowColor(baseColor: AdoktlColor, percent: Double): AdoktlColor {
        val r = baseColor.r
        val g = baseColor.g
        val b = baseColor.b

        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        if (max == min) return baseColor

        val maxKey = when (max) {
            r -> 'R'; g -> 'G'; else -> 'B'
        }
        val minKey = when (min) {
            r -> 'R'; g -> 'G'; else -> 'B'
        }

        val key = "${maxKey}${minKey}"
        val process = rainbowProcess[key] ?: return baseColor

        val range = max - min
        val midVal = when (min) {
            r -> r; g -> g; else -> b
        }
        val dealVal = when (process.first) {
            'R' -> r; 'G' -> g; else -> b
        }

        val per = if (process.second == 1) {
            (process.third + (dealVal - midVal) / range / 6.0f).toDouble()
        } else {
            (process.third + (max - dealVal) / range / 6.0f).toDouble()
        }

        val finalPer = fmod(per + percent, 1.0)
        val base = min

        fun rChange(p: Double): Double = when {
            p in 0.0..sixth[0] -> 1.0
            p < sixth[1] -> 1.0 - (p - sixth[0]) / sixth[0]
            p < sixth[3] -> 0.0
            p < sixth[4] -> (p - sixth[3]) / sixth[0]
            else -> 1.0
        }

        fun gChange(p: Double): Double = when {
            p in 0.0..sixth[0] -> p / sixth[0]
            p < sixth[2] -> 1.0
            p < sixth[3] -> 1.0 - (p - sixth[2]) / sixth[0]
            else -> 0.0
        }

        fun bChange(p: Double): Double = when {
            p in 0.0..sixth[1] -> 0.0
            p < sixth[2] -> (p - sixth[1]) / sixth[0]
            p < sixth[4] -> 1.0
            else -> 1.0 - (p - sixth[4]) / sixth[0]
        }

        return AdoktlColor(
            r = (base + range * rChange(finalPer)).toFloat().coerceIn(0f, 1f),
            g = (base + range * gChange(finalPer)).toFloat().coerceIn(0f, 1f),
            b = (base + range * bChange(finalPer)).toFloat().coerceIn(0f, 1f),
            a = baseColor.a
        )
    }

    private fun fmod(a: Double, b: Double): Double = a - b * kotlin.math.floor(a / b)
}