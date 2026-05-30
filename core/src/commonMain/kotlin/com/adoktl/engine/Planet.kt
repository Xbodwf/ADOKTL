package com.adoktl.engine

import com.adoktl.math.Vector2
import com.adoktl.math.Vector3

data class PlanetState(
    var position: Vector3 = Vector3.ZERO,
    var radius: Double = 0.25,
    var colorRed: Vector3 = Vector3(1.0, 0.3, 0.3),
    var colorBlue: Vector3 = Vector3(0.3, 0.3, 1.0),
    var rotation: Double = 0.0,
    var isRedPlanetActive: Boolean = true
)

class PlanetEngine {
    private var trailPoints = mutableListOf<Vector2>()
    private val maxTrailPoints = 200

    fun updatePlanetPosition(
        planet: PlanetState,
        position: Vector3,
        deltaTime: Double
    ) {
        planet.position = position
        planet.rotation += deltaTime * 2.0

        if (trailPoints.isEmpty() || (position.toVector2() - trailPoints.last()).length() > 0.01) {
            trailPoints.add(position.toVector2())
            if (trailPoints.size > maxTrailPoints) {
                trailPoints.removeAt(0)
            }
        }
    }

    fun getTrailPoints(): List<Vector2> = trailPoints.toList()

    fun clearTrail() {
        trailPoints.clear()
    }

    fun toggleActivePlanet(state: PlanetState) {
        state.isRedPlanetActive = !state.isRedPlanetActive
    }
}