package com.adoktl.math

data class Vector2(val x: Double = 0.0, val y: Double = 0.0) {
    operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)
    operator fun times(scalar: Double) = Vector2(x * scalar, y * scalar)
    operator fun div(scalar: Double) = Vector2(x / scalar, y / scalar)

    fun length(): Double = kotlin.math.sqrt(x * x + y * y)
    fun lengthSq(): Double = x * x + y * y
    fun normalized(): Vector2 {
        val len = length()
        return if (len > 0.0) this / len else Vector2()
    }
    fun dot(other: Vector2): Double = x * other.x + y * other.y
    fun cross(other: Vector2): Double = x * other.y - y * other.x
    fun clone() = Vector2(x, y)
    fun toFloatArray() = floatArrayOf(x.toFloat(), y.toFloat())

    companion object {
        val ZERO = Vector2(0.0, 0.0)
        val UNIT_X = Vector2(1.0, 0.0)
        val UNIT_Y = Vector2(0.0, 1.0)
    }
}

fun degreesToRadians(degrees: Double): Double = degrees * kotlin.math.PI / 180.0
fun radiansToDegrees(radians: Double): Double = radians * 180.0 / kotlin.math.PI

fun lerp(a: Double, b: Double, t: Double): Double = a + (b - a) * t
fun clamp(value: Double, min: Double, max: Double): Double = value.coerceIn(min, max)
fun fmod(a: Double, b: Double): Double = a - b * kotlin.math.floor(a / b)