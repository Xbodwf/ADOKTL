package com.adoktl.math

data class Vector3(val x: Double = 0.0, val y: Double = 0.0, val z: Double = 0.0) {
    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Double) = Vector3(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Double) = Vector3(x / scalar, y / scalar, z / scalar)

    fun length(): Double = kotlin.math.sqrt(x * x + y * y + z * z)
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

    fun toVector2(): com.adoktl.math.Vector2 = com.adoktl.math.Vector2(x, y)

    fun lerp(target: Vector3, t: Double): Vector3 = this + (target - this) * t.coerceIn(0.0, 1.0)

    companion object {
        val ZERO = Vector3()
        val ONE = Vector3(1.0, 1.0, 1.0)
        val UP = Vector3(0.0, 1.0, 0.0)
        val DOWN = Vector3(0.0, -1.0, 0.0)
        val LEFT = Vector3(-1.0, 0.0, 0.0)
        val RIGHT = Vector3(1.0, 0.0, 0.0)
        val FORWARD = Vector3(0.0, 0.0, 1.0)
        val BACK = Vector3(0.0, 0.0, -1.0)
    }
}