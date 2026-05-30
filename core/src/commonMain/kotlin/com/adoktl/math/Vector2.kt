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

data class Vector3(val x: Double = 0.0, val y: Double = 0.0, val z: Double = 0.0) {
    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Double) = Vector3(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Double) = Vector3(x / scalar, y / scalar, z / scalar)

    fun length(): Double = kotlin.math.sqrt(x * x + y * y + z * z)
    fun lengthSq(): Double = x * x + y * y + z * z
    fun normalized(): Vector3 {
        val len = length()
        return if (len > 0.0) this / len else Vector3()
    }
    fun dot(other: Vector3): Double = x * other.x + y * other.y + z * other.z
    fun cross(other: Vector3): Vector3 = Vector3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )
    fun toVector2() = Vector2(x, y)
    fun clone() = Vector3(x, y, z)

    companion object {
        val ZERO = Vector3(0.0, 0.0, 0.0)
        val UNIT_X = Vector3(1.0, 0.0, 0.0)
        val UNIT_Y = Vector3(0.0, 1.0, 0.0)
        val UNIT_Z = Vector3(0.0, 0.0, 1.0)
    }
}

fun degreesToRadians(degrees: Double): Double = degrees * kotlin.math.PI / 180.0
fun radiansToDegrees(radians: Double): Double = radians * 180.0 / kotlin.math.PI

fun lerp(a: Double, b: Double, t: Double): Double = a + (b - a) * t
fun clamp(value: Double, min: Double, max: Double): Double = value.coerceIn(min, max)
fun fmod(a: Double, b: Double): Double = a - b * kotlin.math.floor(a / b)