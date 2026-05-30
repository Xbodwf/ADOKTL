package com.adoktl.math

data class AdoktlColor(val r: Float = 0f, val g: Float = 0f, val b: Float = 0f, val a: Float = 1f) {

    constructor(hex: String) : this(
        r = fromHex(hex).r,
        g = fromHex(hex).g,
        b = fromHex(hex).b,
        a = fromHex(hex).a
    )

    fun toHexRGB(): String {
        val ir = (r * 255).toInt().coerceIn(0, 255)
        val ig = (g * 255).toInt().coerceIn(0, 255)
        val ib = (b * 255).toInt().coerceIn(0, 255)
        return "${ir.toString(16).padStart(2, '0')}${ig.toString(16).padStart(2, '0')}${ib.toString(16).padStart(2, '0')}"
    }

    fun toHexRGBA(): String {
        return toHexRGB() + (a * 255).toInt().coerceIn(0, 255).toString(16).padStart(2, '0')
    }

    fun withAlpha(alpha: Float) = AdoktlColor(r, g, b, alpha)
    fun toFloatArray() = floatArrayOf(r, g, b, a)
    fun toRGBFloatArray() = floatArrayOf(r, g, b)

    operator fun times(scalar: Float) = AdoktlColor(r * scalar, g * scalar, b * scalar, a * scalar)
    operator fun plus(other: AdoktlColor) = AdoktlColor(r + other.r, g + other.g, b + other.b, a + other.a)
    operator fun minus(other: AdoktlColor) = AdoktlColor(r - other.r, g - other.g, b - other.b, a - other.a)

    fun lerpTo(other: AdoktlColor, t: Float): AdoktlColor {
        return AdoktlColor(
            r + (other.r - r) * t,
            g + (other.g - g) * t,
            b + (other.b - b) * t,
            a + (other.a - a) * t
        )
    }

    companion object {
        val WHITE = AdoktlColor(1f, 1f, 1f)
        val BLACK = AdoktlColor(0f, 0f, 0f)
        val RED = AdoktlColor(1f, 0f, 0f)
        val GREEN = AdoktlColor(0f, 1f, 0f)
        val BLUE = AdoktlColor(0f, 0f, 1f)
        val TRANSPARENT = AdoktlColor(0f, 0f, 0f, 0f)

        fun fromHex(hex: String): AdoktlColor {
            val h = hex.removePrefix("#")
            return when (h.length) {
                3 -> AdoktlColor(
                    r = h[0].toString().toInt(16).toFloat() / 15f,
                    g = h[1].toString().toInt(16).toFloat() / 15f,
                    b = h[2].toString().toInt(16).toFloat() / 15f
                )
                4 -> AdoktlColor(
                    r = h[0].toString().toInt(16).toFloat() / 15f,
                    g = h[1].toString().toInt(16).toFloat() / 15f,
                    b = h[2].toString().toInt(16).toFloat() / 15f,
                    a = h[3].toString().toInt(16).toFloat() / 15f
                )
                6 -> AdoktlColor(
                    r = h.substring(0, 2).toInt(16).toFloat() / 255f,
                    g = h.substring(2, 4).toInt(16).toFloat() / 255f,
                    b = h.substring(4, 6).toInt(16).toFloat() / 255f
                )
                8 -> AdoktlColor(
                    r = h.substring(0, 2).toInt(16).toFloat() / 255f,
                    g = h.substring(2, 4).toInt(16).toFloat() / 255f,
                    b = h.substring(4, 6).toInt(16).toFloat() / 255f,
                    a = h.substring(6, 8).toInt(16).toFloat() / 255f
                )
                else -> WHITE
            }
        }
    }
}